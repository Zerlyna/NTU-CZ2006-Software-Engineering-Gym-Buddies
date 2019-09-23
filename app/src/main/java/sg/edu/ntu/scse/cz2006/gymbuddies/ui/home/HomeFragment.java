package sg.edu.ntu.scse.cz2006.gymbuddies.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import sg.edu.ntu.scse.cz2006.gymbuddies.MainActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarPark;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;
    private MapView mapView;
    private GoogleMap mMap;

    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheet;

    private static final int RC_LOC = 1001, RC_LOC_BTN = 1002;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, s -> textView.setText(s));


        // TODO: Move to after show gym detail activity, need to include some filtering for nearby only
        homeViewModel.getCarParks().observe(this, new Observer<List<CarPark>>() {
            @Override
            public void onChanged(List<CarPark> carparks) {
                Log.d("Cy.GymBuddies.HomeFrag", "size: " +carparks.size());
            }
        });


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

        bottomSheet = root.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(100);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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

        //zoomToLocation();
    }

    private boolean hasGpsPermission() {
        return hasGps(false);
    }

    private boolean hasGps(boolean startCheck) {
        if (getContext() == null) return false;
        boolean permGranted = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permGranted && startCheck) {
            mMap.setMyLocationEnabled(true);
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