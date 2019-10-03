package sg.edu.ntu.scse.cz2006.gymbuddies.ui.gymlist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import sg.edu.ntu.scse.cz2006.gymbuddies.MainActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.FavGymAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.StringRecyclerAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateGymFavourites;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.SwipeDeleteCallback;
import sg.edu.ntu.scse.cz2006.gymbuddies.widget.FavButtonView;

/**
 * The fragment handling the viewing of just the favourited gyms of the user
 *
 * @author Kenneth Soh
 * @since 2019-10-03
 */
public class GymListFragment extends Fragment implements SwipeDeleteCallback.ISwipeCallback {

    /**
     * The Fragment View Model as per MVVM architecture
     */
    private GymListViewModel gymListViewModel;

    /**
     * The coordinates of the selected gym
     */
    private LatLng coordinates = null;

    /**
     * Debug tag for logging purposes
     */
    private static final String TAG = "GymListFrag";

    /**
     * The coordinator layout handling the fragment
     */
    private CoordinatorLayout coordinatorLayout;

    /**
     * An adapter to store the favourites list for the RecyclerView
     */
    private FavGymAdapter favAdapter = null;

    /**
     * Firebase Firestore Favourites List Real-time listener
     */
    private ListenerRegistration favListener;

    /**
     * The favourites list recyclerview
     */
    private RecyclerView favouritesList;

    /**
     * The Firebase Firestore listener for favourites in the gym details bottom sheet
     * This is used to handle real time updates to the favourites count of the gym
     */
    private ListenerRegistration gymDetailFavListener = null;

    /**
     * Flag to determine if we are initializing the details view when the gym details bottom sheet appears
     */
    private boolean detailsInit = false;

    /**
     * Internal lifecycle fragment for creating the fragment view
     * @param inflater Layout Inflater object
     * @param container View Group Container object
     * @param savedInstanceState Android Saved Instance State
     * @return The created fragment view
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        gymListViewModel = ViewModelProviders.of(this).get(GymListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gym_list, container, false);
        coordinatorLayout = root.findViewById(R.id.coordinator);

        // FAB Not needed here
        if (getActivity() != null) {
            MainActivity activity = (MainActivity) getActivity();
            activity.fab.hide();
            activity.fab.setOnClickListener(view -> Snackbar.make(view, "Hello from the other side", Snackbar.LENGTH_LONG).show());
        }

        // Setup main view
        favouritesList = root.findViewById(R.id.recycler_view);
        if (favouritesList != null) {
            favouritesList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            favouritesList.setLayoutManager(llm);
            favouritesList.setItemAnimator(new DefaultItemAnimator());

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeDeleteCallback(this, root.getContext(), ItemTouchHelper.LEFT, R.drawable.ic_heart_off));
            itemTouchHelper.attachToRecyclerView(favouritesList);
        }
        emptyFavourites();

        // Setup Gym Details View (sync with the gym section of the Home frag)
        TextView gymTitle = root.findViewById(R.id.gym_details_title);
        TextView gymLocation = root.findViewById(R.id.gym_details_location);
        TextView gymDesc = root.findViewById(R.id.gym_details_description);
        TextView favCount = root.findViewById(R.id.gym_details_fav_count);
        FavButtonView heartIcon = root.findViewById(R.id.gym_details_fav_icon);
        Button carpark = root.findViewById(R.id.gym_details_nearby_carparks_btn);
        Button rate = root.findViewById(R.id.gym_details_rate_btn);
        RecyclerView reviews = root.findViewById(R.id.review_recycler);

        View gymBottomSheet = root.findViewById(R.id.gym_details_sheet);
        LinearLayout favourite = gymBottomSheet.findViewById(R.id.gym_details_fav);
        BottomSheetBehavior gymBottomSheetBehavior = BottomSheetBehavior.from(gymBottomSheet);
        gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        gymBottomSheet.findViewById(R.id.drag_bar).setVisibility(View.INVISIBLE);
        BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d("GymDetailsSheet", "State Changed: " + newState);
                if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED)
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("View Gym Detail");
                    else
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.menu_gym_list);
                }
                gymTitle.setSingleLine(newState == BottomSheetBehavior.STATE_COLLAPSED);
                if (detailsInit && newState != BottomSheetBehavior.STATE_SETTLING) {
                    detailsInit = false;
                    gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Not used
            }
        };
        gymBottomSheetBehavior.setBottomSheetCallback(callback);

        // On Clicks
        gymLocation.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?daddr=" + ((coordinates == null) ?
                gymLocation.getText().toString() : (coordinates.latitude + "," + coordinates.longitude))))));
        carpark.setOnClickListener(view -> Snackbar.make(coordinatorLayout, R.string.coming_soon_feature, Snackbar.LENGTH_LONG).show());
        rate.setOnClickListener(view -> Snackbar.make(coordinatorLayout, R.string.coming_soon_feature, Snackbar.LENGTH_LONG).show());
        favourite.setOnClickListener(v -> heartIcon.callOnClick());
        heartIcon.setOnClickListener(v -> {
            if (v instanceof FavButtonView) {
                FavButtonView heart = (FavButtonView) v;
                heart.onClick(v); // Execute existing view onclick listener
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (getActivity() != null && user != null) {
                    new UpdateGymFavourites(getActivity(), user.getUid(), gymListViewModel.getSelectedGym().getValue().getProperties().getINC_CRC(), heart.isChecked(), success -> {
                        if (success) Snackbar.make(coordinatorLayout, (heart.isChecked()) ? "Saved to favourites!" : "Removed from favourites!", Snackbar.LENGTH_SHORT).show();
                        else Snackbar.make(coordinatorLayout, (heart.isChecked()) ? "Failed to save to favourites. Try again later" : "Failed to remove from favourites. Try again later", Snackbar.LENGTH_SHORT).show();
                    }).execute();
                }
            }
        });

        // Setup observers
        gymListViewModel.getFavCount().observe(this, integer -> favCount.setText(getResources().getString(R.string.number_counter, integer)));
        gymListViewModel.getSelectedGym().observe(this, gymShell -> {
            if (gymShell == null) {
                gymBottomSheetBehavior.setHideable(true);
                gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                backStack.setEnabled(false);
            } else {
                backStack.setEnabled(true);
                gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                gymBottomSheetBehavior.setHideable(false);
                gymTitle.setText(gymShell.getProperties().getName());
                gymDesc.setText(gymShell.getProperties().getDescription());
                if (gymDesc.getText().toString().trim().isEmpty()) gymDesc.setText("No description available");
                gymLocation.setText(GymHelper.generateAddress(gymShell.getProperties()));
                coordinates = new LatLng(gymShell.getGeometry().getLat(), gymShell.getGeometry().getLng());
                heartIcon.setChecked(false);

                DocumentReference gymRef = FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).document(gymShell.getProperties().getINC_CRC());
                HashMap<String, Integer> currentUserFavList = gymListViewModel.getCurrentUserFavourites().getValue();
                if (currentUserFavList.size() > 0 && currentUserFavList.containsKey(gymShell.getProperties().getINC_CRC())) {
                    heartIcon.setChecked(true);
                    gymListViewModel.updateFavCount(currentUserFavList.get(gymShell.getProperties().getINC_CRC()));
                } else
                    gymRef.get().addOnSuccessListener(documentSnapshot -> gymListViewModel.updateFavCount((documentSnapshot.exists()) ? Integer.parseInt(documentSnapshot.get("count").toString()) : 0))
                            .addOnFailureListener(e -> favCount.setText("(?)"));

                // Register update
                if (gymDetailFavListener != null) gymDetailFavListener.remove();
                gymDetailFavListener = gymRef.addSnapshotListener((documentSnapshot, e) -> gymListViewModel.updateFavCount((documentSnapshot != null && documentSnapshot.exists()) ?
                        Integer.parseInt(documentSnapshot.get("count").toString()) : 0));
                detailsInit = true;
            }
        });
        gymListViewModel.getCurrentUserFavourites().observe(this, currentUserFavList -> {
            if (currentUserFavList.size() == 0) emptyFavourites();
            else {
                ArrayList<FavGymObject> finalList = new ArrayList<>();
                HashMap<String, GymList.GymShell> gymDetailsList = new HashMap<>();
                GymList gymList = GymHelper.getGymList(getContext());
                if (gymList == null) {
                    Log.e(TAG, "Failed to get gym list");
                    emptyFavourites(); // Error occurred
                    return;
                }
                for (GymList.GymShell g : gymList.getGyms()) gymDetailsList.put(g.getProperties().getINC_CRC(), g);

                for (String id : currentUserFavList.keySet()) {
                    if (gymDetailsList.containsKey(id)) finalList.add(new FavGymObject(gymDetailsList.get(id), currentUserFavList.get(id)));
                    else Log.e(TAG, "Unknown Gym (" + id + ")");
                }
                favAdapter = new FavGymAdapter(finalList);
                favAdapter.setOnClickListener(v -> {
                    if (v.getTag() instanceof FavGymAdapter.FavViewHolder) {
                        gymListViewModel.setSelectedGym(favAdapter.getList().get(((FavGymAdapter.FavViewHolder) v.getTag()).getAdapterPosition()));
                    }
                });
                favouritesList.setAdapter(favAdapter);
            }
        });

        // Handle back press (if in gym details mode should revert)
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backStack);


        return root;
    }

    /**
     * Callback handler for Jetpack Navigation back override
     */
    private OnBackPressedCallback backStack = new OnBackPressedCallback(false) {
        /**
         * Function to execute when back button pressed
         */
        @Override
        public void handleOnBackPressed() {
            if (gymListViewModel.getSelectedGym().getValue() != null) gymListViewModel.setSelectedGym(null);
        }
    };

    /**
     * Lifecycle event called when the fragment is resumed
     */
    @Override
    public void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && favListener == null) {
            Query userFavGymQuery = FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).whereArrayContains("userIds", user.getUid());

            userFavGymQuery.get().addOnSuccessListener(this::processFavListUpdates);
            favListener = userFavGymQuery.addSnapshotListener((querySnapshot, e) -> processFavListUpdates(querySnapshot));
        }
    }

    /**
     * Lifecycle event called when the fragment is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        if (favListener != null) favListener.remove();
        favListener = null;
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
            HashMap<String, Integer> workingSet = new HashMap<>();
            for (DocumentSnapshot docs : gyms) {
                workingSet.put(docs.getId(), Integer.parseInt(Objects.requireNonNull(docs.get("count")).toString()));
            }
            gymListViewModel.updateCurrentUserFavourites(workingSet);
        } else gymListViewModel.updateCurrentUserFavourites(new HashMap<>());
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
}