package sg.edu.ntu.scse.cz2006.gymbuddies.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;

import sg.edu.ntu.scse.cz2006.gymbuddies.MainActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.StringRecyclerAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.ParseGymDataFile;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.TrimNearbyGyms;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;
    private MapView mapView;
    private GoogleMap mMap;
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView favouritesList;
    private SharedPreferences sp;

    // Favourites
    private BottomSheetBehavior favBottomSheetBehavior;
    private View favBottomSheet;

    private BottomSheetBehavior gymBottomSheetBehavior;
    private View gymBottomSheet;

    private static final int RC_LOC = 1001, RC_LOC_BTN = 1002;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        coordinatorLayout = root.findViewById(R.id.coordinator);
        homeViewModel.getText().observe(this, s -> textView.setText(s));

        sp = PreferenceManager.getDefaultSharedPreferences(root.getContext());


        // TODO: Move to after show gym detail activity, need to include some filtering for nearby only
        homeViewModel.getCarParks().observe(this, carparks -> Log.d("Cy.GymBuddies.HomeFrag", "size: " +carparks.size()));


        mapView = root.findViewById(R.id.map_view);
        setHasOptionsMenu(true);

        // Request permission eh
        if (!hasGpsPermission() && getActivity() != null) {
            Log.i(TAG, "No permissions, requesting...");
            final String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity()).setTitle("Location Permission Required").setMessage("We require access to your location to view nearby gyms")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermissions(permissions, RC_LOC)).show();
            } else {
                requestPermissions(permissions, RC_LOC);
            }
        }

        if (getActivity() != null) {
            MainActivity activity = (MainActivity)getActivity();
            activity.fab.hide();
            activity.fab.setOnClickListener(view -> Snackbar.make(view, "Hello from the other side", Snackbar.LENGTH_LONG).show());
        }

        favBottomSheet = root.findViewById(R.id.bottom_sheet);
        favBottomSheetBehavior = BottomSheetBehavior.from(favBottomSheet);
        favBottomSheetBehavior.setPeekHeight(200);
        favBottomSheetBehavior.setHideable(false);
        favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        favBottomSheet.setOnTouchListener((view, motionEvent) -> {
            view.performClick();
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && favBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            return false;
        });

        gymBottomSheet = root.findViewById(R.id.gym_details_sheet);
        gymBottomSheetBehavior = BottomSheetBehavior.from(gymBottomSheet);
        gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        favouritesList = favBottomSheet.findViewById(R.id.favourite_list);
        if (favouritesList != null) {
            favouritesList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            favouritesList.setLayoutManager(llm);
            favouritesList.setItemAnimator(new DefaultItemAnimator());
        }

        // TODO: Get favourites list from firebase based on the key
        // TODO: Remove default 0 hahaha
        String[] toremove = {"No Favourites Found", "This feature is currently a WIP"};
        StringRecyclerAdapter adapter = new StringRecyclerAdapter(Arrays.asList(toremove));
        favouritesList.setAdapter(adapter);

        return root;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (!firstMarkerLoad) {
            new TrimNearbyGyms(sp.getInt("nearby-gyms", 10), lastLocation, markerList, this::updateNearbyMarkers).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Google Map Ready");
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
            hasGps(true);
            //checkGpsForCurrentLocation();
            UiSettings settings = mMap.getUiSettings();
            //settings.setZoomControlsEnabled(true);
            settings.setMapToolbarEnabled(false);

            //zoomToMyLocation();
            // Zoom to Singapore: 1.3413054,103.8074233, 12z
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1.3413054, 103.8074233), 10f));
            mMap.setOnInfoWindowClickListener(marker -> Snackbar.make(coordinatorLayout, "Feature Coming Soon! (Gym Details)", Snackbar.LENGTH_LONG).show());
            mMap.setOnMapClickListener(latLng -> {
                Log.d("mMap", "mapClicked()");
                gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                favBottomSheetBehavior.setHideable(false);
            });
            mMap.setOnMarkerClickListener(marker -> {
                // Hide and reshow gym
                Log.d("mMap", "markerClicked()");
                gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                favBottomSheetBehavior.setHideable(true);
                favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return false; // We still want to show the info window right now
        });
        // Process and parse markers
        if (getActivity() != null) {
            new ParseGymDataFile(getActivity(), (markers) -> {
                if (markers == null) return;
                markerList = markers;

                if (!hasLocationPermission || lastLocation == null) for (MarkerOptions m : markers) { mMap.addMarker(m); } // Show all gyms if you do not have location granted
                else {
                    new TrimNearbyGyms(sp.getInt("nearby-gyms", 10), lastLocation, markerList, this::updateNearbyMarkers).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private boolean hasGpsPermission() {
        return hasGps(false);
    }

    private boolean firstMarkerLoad = true;

    private void updateNearbyMarkers(ArrayList<MarkerOptions> results) {
        mMap.clear();
        for (MarkerOptions m : results) { mMap.addMarker(m); }
        firstMarkerLoad = false;
    }

    private FusedLocationProviderClient locationClient;
    private boolean hasLocationPermission = false;
    private ArrayList<MarkerOptions> markerList = new ArrayList<>();
    private LatLng lastLocation = null;

    private void zoomToMyLocation() {
        if (getActivity() != null) {
            locationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            locationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if (location == null) return;

                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15f));

                if (markerList.size() > 0) {
                    new TrimNearbyGyms(sp.getInt("nearby-gyms", 10), lastLocation, markerList, this::updateNearbyMarkers).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }
    }

    private boolean hasGps(boolean startCheck) {
        if (getContext() == null) return false;
        boolean permGranted = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permGranted && startCheck) {
            hasLocationPermission = true;
            mMap.setMyLocationEnabled(true);
            zoomToMyLocation();
        }
        return permGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_LOC && requestCode != RC_LOC_BTN) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission granted - initialize the gps source");
            // we have permission, check if map is enabled
            if (mMap != null) {
                hasGps(true);
            }
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length + " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        if (requestCode == RC_LOC_BTN && getActivity() != null) {
            new AlertDialog.Builder(getActivity()).setTitle("Location Permission not granted")
                    .setMessage("Unable to get location, permission not granted").setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton("App Settings", (dialog, which) -> {
                        Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri packageURI = Uri.parse("package:" + getActivity().getPackageName());
                        permIntent.setData(packageURI);
                        startActivity(permIntent);
                    }).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private static final String TAG = "HomeFrag";
}