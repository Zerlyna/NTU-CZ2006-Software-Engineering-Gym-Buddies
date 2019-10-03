package sg.edu.ntu.scse.cz2006.gymbuddies.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import sg.edu.ntu.scse.cz2006.gymbuddies.MainActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.FavGymAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.StringRecyclerAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.ParseGymDataFile;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.TrimNearbyGyms;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateGymFavourites;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.SwipeDeleteCallback;
import sg.edu.ntu.scse.cz2006.gymbuddies.widget.FavButtonView;

/**
 * Fragment being used to display the Home activity of the application. Including nearby gyms, gym details, favourited gyms etc
 *
 * @author Kenneth Soh, Chia Yu
 * @since 2019-09-06
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback, SwipeDeleteCallback.ISwipeCallback {

    /**
     * The View Model
     */
    private HomeViewModel homeViewModel;
    /**
     * Google Maps View
     */
    private MapView mapView;
    /**
     * Google Maps Instance
     */
    private GoogleMap mMap;
    /**
     * The coordinator layout handling the fragment
     */
    private CoordinatorLayout coordinatorLayout;
    /**
     * The favourites list recyclerview
     */
    private RecyclerView favouritesList;
    /**
     * The application preference file
     */
    private SharedPreferences sp;

    // Favourites
    /**
     * Handles the behavior of the favourites list bottom sheet
     */
    private BottomSheetBehavior favBottomSheetBehavior;
    /**
     * The favourites list bottom sheet
     */
    private View favBottomSheet;

    /**
     * Handles the behavior of the gym details bottom sheet
     */
    private BottomSheetBehavior gymBottomSheetBehavior;
    /**
     * The gym details bottom sheet
     */
    private View gymBottomSheet;

    /**
     * The Request Code for Location Permission requests
     */
    private static final int RC_LOC = 1001;

    /**
     * Creates the fragment view
     * @param inflater The Layout Inflater Object
     * @param container The View Group
     * @param savedInstanceState Android saved instance state
     * @return The view of the fragment
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        coordinatorLayout = root.findViewById(R.id.coordinator);
        homeViewModel.getText().observe(this, textView::setText);

        sp = PreferenceManager.getDefaultSharedPreferences(root.getContext());


        // TODO: Move to after show gym detail activity, need to include some filtering for nearby only
        homeViewModel.getCarParks().observe(this, carparks -> Log.d("Cy.GymBuddies.HomeFrag", "size: " + carparks.size()));


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
            MainActivity activity = (MainActivity) getActivity();
            activity.fab.hide();
            activity.fab.setOnClickListener(view -> Snackbar.make(view, "Hello from the other side", Snackbar.LENGTH_LONG).show());
        }

        favBottomSheet = root.findViewById(R.id.bottom_sheet);
        favBottomSheetBehavior = BottomSheetBehavior.from(favBottomSheet);
        favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        favBottomSheet.setOnTouchListener((view, motionEvent) -> {
            view.performClick();
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && favBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            return false;
        });

        gymBottomSheet = root.findViewById(R.id.gym_details_sheet);
        gymBottomSheetBehavior = BottomSheetBehavior.from(gymBottomSheet);
        gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d("GymDetailsSheet", "State Changed: " + newState);
                bottomSheet.findViewById(R.id.drag_bar).setVisibility((newState == BottomSheetBehavior.STATE_EXPANDED) ? View.INVISIBLE : View.VISIBLE);
                backStack.setEnabled(newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED);
                if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED)
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("View Gym Detail");
                    else
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.menu_home);
                }
                gymTitle.setSingleLine(newState == BottomSheetBehavior.STATE_COLLAPSED);
                if (autoExpandFlag && newState != BottomSheetBehavior.STATE_SETTLING) {
                    autoExpandFlag = false;
                    gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Not used
            }
        };
        gymBottomSheetBehavior.setBottomSheetCallback(callback);
        setupGymDetailsControls();

        favouritesList = favBottomSheet.findViewById(R.id.favourite_list);
        if (favouritesList != null) {
            favouritesList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            favouritesList.setLayoutManager(llm);
            favouritesList.setItemAnimator(new DefaultItemAnimator());

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeDeleteCallback(this, favBottomSheet.getContext(), ItemTouchHelper.LEFT, R.drawable.ic_heart_off));
            itemTouchHelper.attachToRecyclerView(favouritesList);
        }
        emptyFavourites();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backStack);

        return root;
    }

    /**
     * Custom backstack handler
     */
    private OnBackPressedCallback backStack = new OnBackPressedCallback(false) {
        /**
         * Handles when the back button is pressed
         */
        @Override
        public void handleOnBackPressed() {
            if (gymBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Collapse gym details
            } else if (gymBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                unselectGymDetails();
            }
        }
    };

    /**
     * Method called when the view is created in the lifecycle
     * @param view Fragment View
     * @param savedInstanceState Android Saved Instance State
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    /**
     * Internal function to call when the user has no favourited gyms
     */
    private void emptyFavourites() {
        String[] toremove = {"No Favourited Gyms Saved"};
        StringRecyclerAdapter adapter = new StringRecyclerAdapter(Arrays.asList(toremove));
        favouritesList.setAdapter(adapter);
        favAdapter = null;
    }

    /**
     * Firebase Firestore Favourites List Real-time listener
     */
    private ListenerRegistration favListener;
    /**
     * A hashmap containing the realtime favourites list of the logged in user
     */
    private HashMap<String, Integer> currentUserFavList = new HashMap<>();

    /**
     * Method called when the activity is resumed from the lifecycle
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (!firstMarkerLoad) new TrimNearbyGyms(sp.getInt("nearby-gyms", 10), lastLocation, markerList.keySet(), this::updateNearbyMarkers).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        registerFavList();
    }

    /**
     * A flag to check if we have finished populating the favourites list
     */
    private boolean favListRegistered = false;

    /**
     * Registers the favourites list real time listener with Firebase Firestore
     */
    private void registerFavList() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        favListRegistered = false;
        if (user != null && favListener == null && markerList.size() > 0) {
            favListRegistered = true;
            Query userFavGymQuery = FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).whereArrayContains("userIds", user.getUid());

            userFavGymQuery.get().addOnSuccessListener(this::processFavListUpdates);
            favListener = userFavGymQuery.addSnapshotListener((querySnapshot, e) -> processFavListUpdates(querySnapshot));
        }
    }

    /**
     * Process real-time updates from Firebase Firestore
     * @param querySnapshot The database document snapshot at that current point in time
     */
    private void processFavListUpdates(QuerySnapshot querySnapshot) {
        Log.d(TAG, "processFavListUpdates()");
        // Update favourites
        if (querySnapshot != null && querySnapshot.size() > 0) {
            List<DocumentSnapshot> gyms = querySnapshot.getDocuments();
            currentUserFavList.clear();
            for (DocumentSnapshot docs : gyms) {
                currentUserFavList.put(docs.getId(), Integer.parseInt(Objects.requireNonNull(docs.get("count")).toString()));
            }

            if (currentUserFavList.size() == 0) emptyFavourites();
            else {
                ArrayList<FavGymObject> finalList = new ArrayList<>();
                HashMap<String, GymList.GymShell> gymDetailsList = new HashMap<>();
                for (GymList.GymShell shells : markerList.values()) { gymDetailsList.put(shells.getProperties().getINC_CRC(), shells); }
                for (String id : currentUserFavList.keySet()) {
                    if (gymDetailsList.containsKey(id)) finalList.add(new FavGymObject(gymDetailsList.get(id), currentUserFavList.get(id)));
                    else Log.e(TAG, "Unknown Gym (" + id + ")");
                }
                favAdapter = new FavGymAdapter(finalList);
                favAdapter.setOnClickListener(v -> {
                    if (v.getTag() instanceof FavGymAdapter.FavViewHolder) {
                        showGymDetails();
                        updateGymDetails(((FavGymAdapter.FavViewHolder) v.getTag()).getGymObj());
                        autoExpandFlag = true;
                    }
                });
                favouritesList.setAdapter(favAdapter);
                final float scale = getResources().getDisplayMetrics().density;
                int maxHeight = (int) (450 * scale + 0.5f);
                favBottomSheet.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int layoutHeight = favBottomSheet.getMeasuredHeight();
                Log.d(TAG, "FavListHeight: " + layoutHeight + " | Max Height Limit: " + maxHeight);
                ViewGroup.LayoutParams params = favBottomSheet.getLayoutParams();
                if (layoutHeight > maxHeight) params.height = maxHeight;
                else params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                favBottomSheet.setLayoutParams(params);
                favBottomSheet.requestLayout();
            }
        } else emptyFavourites();
    }

    /**
     * A flag to check if we should auto expand the gym details bottom sheet after it has settled
     */
    private boolean autoExpandFlag = false;
    /**
     * An adapter to store the favourites list for the RecyclerView
     */
    private FavGymAdapter favAdapter = null;

    /**
     * Lifecycle event called when the activity is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        favListener.remove();
        favListener = null;
    }

    /**
     * Internal function called to hide the gym details bottom sheet and redisplay the favourites list bottom sheet
     */
    private void unselectGymDetails() {
        favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        favBottomSheetBehavior.setHideable(false);
        gymBottomSheetBehavior.setHideable(true);
        gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        if (gymDetailFavListener != null) {
            gymDetailFavListener.remove();
            gymDetailFavListener = null;
        }
    }

    /**
     * Internal function called to hide the favourites list bottom sheet and display the gym details bottom sheet
     */
    private void showGymDetails() {
        gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        gymBottomSheetBehavior.setHideable(false);
        favBottomSheetBehavior.setHideable(true);
        favBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /**
     * Internal method called when the Google Map instance has finished loading and is ready for interation
     * @param googleMap Google Map instance
     */
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
            mMap.setOnInfoWindowClickListener(marker -> gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));
            mMap.setOnMapClickListener(latLng -> {
                Log.d("mMap", "mapClicked()");
                unselectGymDetails();
            });
            mMap.setOnMarkerClickListener(marker -> {
                // Hide and reshow gym
                Log.d("mMap", "markerClicked()");
                showGymDetails();
                if (marker.getTag() instanceof GymList.GymShell) updateGymDetails((GymList.GymShell) marker.getTag());
                return false; // We still want to show the info window right now
        });
        // Process and parse markers
        if (getActivity() != null) {
            new ParseGymDataFile(getActivity(), (markers) -> {
                if (markers == null) return;
                markerList = markers;
                if (!favListRegistered) registerFavList();

                if (!hasLocationPermission || lastLocation == null) for (MarkerOptions m : markers.keySet()) { mMap.addMarker(m); } // Show all gyms if you do not have location granted
                else {
                    new TrimNearbyGyms(sp.getInt("nearby-gyms", 10), lastLocation, markerList.keySet(), this::updateNearbyMarkers).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Checks if the application has ACCESS_FINE_LOCATION permission
     * @return true if present, false otherwise
     */
    private boolean hasGpsPermission() {
        return hasGps(false);
    }

    /**
     * Flag to determine if this is the first time we are initializing the Google map markers for the gym
     */
    private boolean firstMarkerLoad = true;

    /**
     * Internal function to update the markers near to the user's location. The number of markers is configurable in the app settings
     * @param results List of markers near the user's location
     */
    private void updateNearbyMarkers(ArrayList<MarkerOptions> results) {
        mMap.clear();
        for (MarkerOptions m : results) {
            Marker mark = mMap.addMarker(m);
            if (markerList.containsKey(m)) mark.setTag(markerList.get(m));
        }
        firstMarkerLoad = false;
    }

    /**
     * The user current location provider client
     */
    private FusedLocationProviderClient locationClient;
    /**
     * If the application has ACCESS_FINE_LOCATION permission flag
     */
    private boolean hasLocationPermission = false;
    /**
     * A map of the markers and their corresponding gym objects
     */
    private HashMap<MarkerOptions, GymList.GymShell> markerList = new LinkedHashMap<>();
    /**
     * Last known location of the user
     */
    private LatLng lastLocation = null;

    /**
     * Internal method to zoom the map to the user's current location
     */
    private void zoomToMyLocation() {
        if (getActivity() != null) {
            locationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            locationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if (location == null) return;

                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15f));

                if (markerList.size() > 0) {
                    new TrimNearbyGyms(sp.getInt("nearby-gyms", 10), lastLocation, markerList.keySet(), this::updateNearbyMarkers).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }
    }

    /**
     * Internal function to start the check for if the app has the ACCESS_FINE_LOCATION permission granted
     * @param startCheck If it is the first time the permission is being asked. This is just used to enable location on the map and zoom the user to the location
     * @return true if permission granted, false otherwise
     */
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

    /**
     * Internal function to handle permission request results
     * @param requestCode Permission Request Code
     * @param permissions Permissions requested by the app
     * @param grantResults The grant results of the permissions requested
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_LOC) {
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
    }

    /**
     * When a gym in the favourites list has been swiped to unfavourite
     * @param position The position of the item being unfavourited
     * @return false if no errors
     */
    @Override
    public boolean delete(@Nullable Integer position) {
        // Unfavourite selected listener
        if (favAdapter == null || position == null) return false;
        String gymId = favAdapter.getList().get(position).getGym().getProperties().getINC_CRC();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (getActivity() != null && user != null) {
            new UpdateGymFavourites(getActivity(), user.getUid(), gymId, false, success -> {
                if (success) Snackbar.make(coordinatorLayout, "Removed from favourites!", Snackbar.LENGTH_SHORT).setAction("UNDO", v -> {
                    // Restore from favourites
                    new UpdateGymFavourites(getActivity(), user.getUid(), gymId, true, success1 -> {
                        if (success1) Snackbar.make(coordinatorLayout, "Removal from favourites undone", Snackbar.LENGTH_SHORT).show();
                        else Snackbar.make(coordinatorLayout, "Failed to undo favourites removal. Please refavourite the gym manually", Snackbar.LENGTH_SHORT).show();
                    }).execute(); }).show();
                else {
                    Snackbar.make(coordinatorLayout, "Failed to remove from favourites. Try again later", Snackbar.LENGTH_SHORT).show();
                }
            }).execute();
        }
        return false;
    }

    /**
     * Internal Android function to create the application options menu on the ActionBar
     * @param menu The menu object instance
     * @param inflater The menu inflater object instance
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Used for logging purposes for this activity
     */
    private static final String TAG = "HomeFrag";

    // Gym Details
    /**
     * Gym Title TextView
     */
    private TextView gymTitle;
    /**
     * Gym Location TextView
     */
    private TextView gymLocation;
    /**
     * Gym Description TextView
     */
    private TextView gymDesc;
    /**
     * Gym Favourites Count TextView
     */
    private TextView favCount;
    /**
     * Gym Favourites Icon
     */
    private FavButtonView heartIcon;
    /**
     * View Gym's Nearby Carparks button
     */
    private Button carpark;
    /**
     * Rate and review gym button
     */
    private Button rate;
    /**
     * Gym Reviews recyclerview
     */
    private RecyclerView reviews;
    /**
     * The coordinates of the currently displayed gym
     */
    private LatLng coordinates = null;
    /**
     * The Gym Unique ID
     */
    private String selectedGymUid = null;

    /**
     * Initialize method for setting up the gym details bottom sheet
     */
    private void setupGymDetailsControls() {
        // Init Elements
        gymTitle = gymBottomSheet.findViewById(R.id.gym_details_title);
        gymDesc = gymBottomSheet.findViewById(R.id.gym_details_description);
        gymLocation = gymBottomSheet.findViewById(R.id.gym_details_location);
        favCount = gymBottomSheet.findViewById(R.id.gym_details_fav_count);
        LinearLayout favourite = gymBottomSheet.findViewById(R.id.gym_details_fav);
        heartIcon = gymBottomSheet.findViewById(R.id.gym_details_fav_icon);
        carpark = gymBottomSheet.findViewById(R.id.gym_details_nearby_carparks_btn);
        rate = gymBottomSheet.findViewById(R.id.gym_details_rate_btn);
        reviews = gymBottomSheet.findViewById(R.id.review_recycler);
        gymLocation.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?daddr=" + ((coordinates == null) ?
                gymLocation.getText().toString() : (coordinates.latitude + "," + coordinates.longitude))))));

        if (reviews != null) {
            reviews.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            reviews.setLayoutManager(llm);
            reviews.setItemAnimator(new DefaultItemAnimator());
        }

        // TODO: Get reviews list from firebase based on the gym key
        String[] toremove = {"No Reviews Found for this gym", "This feature is currently a WIP"};
        StringRecyclerAdapter adapter = new StringRecyclerAdapter(Arrays.asList(toremove));
        reviews.setAdapter(adapter);

        // On Click
        carpark.setOnClickListener(view -> Snackbar.make(coordinatorLayout, R.string.coming_soon_feature, Snackbar.LENGTH_LONG).show());
        rate.setOnClickListener(view -> Snackbar.make(coordinatorLayout, R.string.coming_soon_feature, Snackbar.LENGTH_LONG).show());
        favourite.setOnClickListener(v -> heartIcon.callOnClick());
        heartIcon.setOnClickListener(v -> {
            if (v instanceof FavButtonView) {
                FavButtonView heart = (FavButtonView) v;
                heart.onClick(v); // Execute existing view onclick listener
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (getActivity() != null && user != null) {
                    new UpdateGymFavourites(getActivity(), user.getUid(), selectedGymUid, heart.isChecked(), success -> {
                        if (success) Snackbar.make(coordinatorLayout, (heart.isChecked()) ? "Saved to favourites!" : "Removed from favourites!", Snackbar.LENGTH_SHORT).show();
                        else Snackbar.make(coordinatorLayout, (heart.isChecked()) ? "Failed to save to favourites. Try again later" : "Failed to remove from favourites. Try again later", Snackbar.LENGTH_SHORT).show();
                    }).execute();
                }
            }
        });
    }

    /**
     * The Firebase Firestore listener for favourites in the gym details bottom sheet
     * This is used to handle real time updates to the favourites count of the gym
     */
    private ListenerRegistration gymDetailFavListener = null;

    /**
     * Updates the data in the gym details bottom sheet
     * @param gym The gym whose data we are updating the sheet with
     */
    private void updateGymDetails(@Nullable GymList.GymShell gym) {
        if (gym == null) return;
        gymTitle.setText(gym.getProperties().getName());
        gymDesc.setText(gym.getProperties().getDescription());
        gymLocation.setText(GymHelper.generateAddress(gym.getProperties()));
        coordinates = new LatLng(gym.getGeometry().getLat(), gym.getGeometry().getLng());
        heartIcon.setChecked(false);
        selectedGymUid = gym.getProperties().getINC_CRC();
        // Initial update
        DocumentReference gymRef = FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).document(gym.getProperties().getINC_CRC());
        if (currentUserFavList.size() > 0 && currentUserFavList.containsKey(gym.getProperties().getINC_CRC())) {
            heartIcon.setChecked(true);
            Integer favCount = currentUserFavList.get(gym.getProperties().getINC_CRC());
            this.favCount.setText(getResources().getString(R.string.number_counter, favCount));
        } else
            gymRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) favCount.setText(getResources().getString(R.string.number_counter, Integer.parseInt(documentSnapshot.get("count").toString())));
                else favCount.setText("(0)");
            }).addOnFailureListener(e -> favCount.setText("(?)"));

        // Register update
        if (gymDetailFavListener != null) gymDetailFavListener.remove();
        gymDetailFavListener = gymRef.addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.exists()) favCount.setText(getResources().getString(R.string.number_counter, Integer.parseInt(documentSnapshot.get("count").toString())));
            else favCount.setText("(0)");
        });
    }
}