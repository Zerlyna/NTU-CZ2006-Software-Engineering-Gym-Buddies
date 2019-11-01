package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdlist;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.AppConstants;
import sg.edu.ntu.scse.cz2006.gymbuddies.ChatActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.BuddyResultAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavBuddyRecord;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewInteractedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DialogHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;

/**
 * The fragment display list of buddies (favoured user) of current user
 * it allow user to view profile information, start chat and un-favour of user
 *
 * @author Chia Yu
 * @since 2019-09-06
 */
public class BuddyListFragment extends Fragment implements AppConstants, OnRecyclerViewInteractedListener<BuddyResultAdapter.ViewHolder> {
    private final String TAG = "GB.frag.BdList";
    private BuddyListViewModel buddyListViewModel;
    private RecyclerView rvResult;
    private BuddyResultAdapter adapter;
    SwipeRefreshLayout srlUpdateFav;

    private ArrayList<User> listFavUsers;
    private ArrayList<String> listFavUserIds;

    private FirebaseFirestore firestore;
    private DocumentReference favBuddiesRef;
    private FavBuddyRecord favRecord;
//    private ListenerRegistration favRecordChangeListener;


    /**
     * Internal lifecycle fragment for creating the fragment view
     * @param inflater Layout Inflater object
     * @param container View Group Container object
     * @param savedInstanceState Android Saved Instance State
     * @return The created fragment view
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        buddyListViewModel = ViewModelProviders.of(this).get(BuddyListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_buddy_list, container, false);

        srlUpdateFav = root.findViewById(R.id.srl_update_fav);
        rvResult = root.findViewById(R.id.rv_buddies);

//        final TextView textView = root.findViewById(R.id.text_gallery);
        buddyListViewModel.getText().observe(this, s -> {
//                textView.setText(s);
        });


        listFavUsers = new ArrayList<>();
        listFavUserIds = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        adapter = new BuddyResultAdapter(listFavUsers, listFavUserIds);
        adapter.setOnRecyclerViewClickedListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvResult.setAdapter(adapter);
        rvResult.setLayoutManager(mLayoutManager);


        srlUpdateFav.setColorSchemeResources(R.color.google_1, R.color.google_2, R.color.google_3, R.color.google_4);
        srlUpdateFav.setOnRefreshListener(() -> readData());
        return root;
    }

    private void readData() {
        Log.d(TAG, "do read data");
//        favBuddyHelper.getFavBuddyRecord();
        queryFavUserRecord();
        srlUpdateFav.setRefreshing(true);
    }

    private void queryFavUserRecord() {
        Log.d(TAG, "queryFavRecord");
        if (favBuddiesRef == null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            favBuddiesRef = firestore.collection(COLLECTION_FAV_BUDDY).document(uid);
        }

        favBuddiesRef.get().addOnSuccessListener((documentSnapshot) -> {
            Log.d(TAG, "favBuddiesRef.get() -> onSuccess " + documentSnapshot);
            readFavRecordDoc(documentSnapshot);
        }).addOnFailureListener((e) -> {
            Log.d(TAG, "favBuddiesRef.get() -> onFailed " + e.getMessage());
        });
    }

    private void readFavRecordDoc(DocumentSnapshot documentSnapshot) {
        favRecord = documentSnapshot.toObject(FavBuddyRecord.class);
        if (favRecord == null) {
            favRecord = new FavBuddyRecord();
        }
        listFavUserIds.clear();
        listFavUserIds.addAll(favRecord.getBuddiesId());

        queryBuddies();
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart()");
        readData();
    }

    @Override
    public void onResume() {
        super.onResume();

//        listenFavRecordChanges();
    }

    @Override
    public void onPause() {

//        stopListenFavRecordChanges();
        super.onPause();
    }
//    private void listenFavRecordChanges(){
//        favRecordChangeListener = favBuddiesRef.addSnapshotListener((doc, e)->{
//            Log.d(TAG, "favBuddiesRef.addSnapshotListener -> onEvent ");
//            if (e != null){
//                Log.w(TAG, "Listen failed", e);
//                return;
//            }
//            readFavRecordDoc(doc);
//        });
//    }
//    private void stopListenFavRecordChanges(){
//        if (favRecordChangeListener!=null){
//            favRecordChangeListener.remove();
//            favRecordChangeListener = null;
//        }
//    }


    private void queryBuddies() {
        Log.d(TAG, "do read buddies");
        CollectionReference userRef = firestore.collection(GymHelper.GYM_USERS_COLLECTION);
        userRef.get().addOnSuccessListener((snapshots) -> {
            Log.d(TAG, "userRef.get() success");
            readUserQuerySnapshot(snapshots);
        }).addOnFailureListener((e) -> {
            Log.d(TAG, "query all users failed");
        });
    }

    private void readUserQuerySnapshot(QuerySnapshot snapshots) {
        Log.d(TAG, "readUserQuerySnapshot -> " + snapshots);
        ArrayList<User> users = new ArrayList<>();
        users.addAll(snapshots.toObjects(User.class));
        Log.d(TAG, "user.size->" + users.size());
        Log.d(TAG, "favUserIds.size->" + listFavUserIds.size());

        // perform filtering
        listFavUsers.clear();
        String curUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (User user : users) {
            if (user.getUid().equals(curUserId)) {
                continue;
            }
            if (listFavUserIds.contains(user.getUid())) {
                listFavUsers.add(user);
            }
        }

        adapter.notifyDataSetChanged();
        Log.d(TAG, "fav user.size->" + listFavUsers.size());
        if (srlUpdateFav.isRefreshing()) {
            srlUpdateFav.setRefreshing(false);
        }
    }

    @Override
    public void onViewInteracted(View view, BuddyResultAdapter.ViewHolder holder, int action) {
        User user = listFavUsers.get(holder.getAdapterPosition());
        switch (action) {
            case BuddyResultAdapter.ACTION_CLICK_ON_FAV_ITEM:
                unfavUser(user);
                break;

            case BuddyResultAdapter.ACTION_CLICK_ON_ITEM_BODY:
                goChatActivity(user);
                break;

            case BuddyResultAdapter.ACTION_CLICK_ON_ITEM_PIC:
                DialogHelper.displayBuddyProfile(getContext(), user, ((ImageView) view).getDrawable());
                break;
            default:
        }
    }


    private void unfavUser(User other) {
        listFavUsers.remove(other);
        listFavUserIds.remove(other.getUid());
        favRecord.getBuddiesId().remove(other.getUid());
        commitFavRecord();
        adapter.notifyDataSetChanged();
        Snackbar snackbar = Snackbar.make(rvResult, R.string.txt_msg_removed_favourite, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.txt_undo, v -> {
            listFavUsers.add(other);
            listFavUserIds.add(other.getUid());
            favRecord.getBuddiesId().add(other.getUid());
            commitFavRecord();
            adapter.notifyDataSetChanged();
            Snackbar.make(rvResult, R.string.txt_msg_removed_favtorite_undone, Snackbar.LENGTH_SHORT).show();

        });
        snackbar.show();
    }

    private void commitFavRecord() {
        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.set(favBuddiesRef, favRecord);
            return null;
        }).addOnSuccessListener((v) -> {
            Log.d(TAG, "favRecord updated success");
        }).addOnFailureListener((e) -> {
            Log.d(TAG, "favRecord updated failed");
            e.printStackTrace();
        });
    }

    private void goChatActivity(User other) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        Bundle data = new Bundle();
        data.putString("buddy_id", other.getUid());
        data.putString("buddy_name", other.getName());
        data.putString("buddy_pic_url", other.getProfilePicUri());
        intent.putExtras(data);
        startActivity(intent);
    }


}