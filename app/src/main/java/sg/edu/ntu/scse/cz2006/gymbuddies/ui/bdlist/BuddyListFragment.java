package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdlist;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.BuddySearchResultActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.ChatActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.MainActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.BuddyResultAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavBuddyRecord;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.FavBuddyHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;

public class BuddyListFragment extends Fragment  implements  FavBuddyHelper.OnFavBuddiesUpdateListener, BuddyResultAdapter.OnBuddyClickedListener{
    private final String TAG = "GB.frag.BdList";
    private BuddyListViewModel buddyListViewModel;
    private ListenerRegistration userSnapshotsListener;
    private FavBuddyHelper favBuddyHelper;
    private ArrayList<User> listUsers;
    private ArrayList<User> listFavUsers;
    private RecyclerView rvResult;
    private BuddyResultAdapter adapter;
    SwipeRefreshLayout srlUpdateFav;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        buddyListViewModel = ViewModelProviders.of(this).get(BuddyListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_buddy_list, container, false);

        srlUpdateFav = root.findViewById(R.id.srl_update_fav);
        rvResult = root.findViewById(R.id.rv_buddies);

        if (getActivity() != null) {
            MainActivity activity = (MainActivity) getActivity();
            activity.fab.hide();
        }


//        final TextView textView = root.findViewById(R.id.text_gallery);
        buddyListViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });


        listUsers = new ArrayList<>();
        listFavUsers = new ArrayList<>();
        favBuddyHelper = new FavBuddyHelper();
//        favBuddyHelper.setUpdateListener(this);

        adapter = new BuddyResultAdapter(listFavUsers);
//        adapter.setOnBuddyClickedListener(this);
        adapter.addOnBuddyClickedListener(this);
        adapter.setFavBuddyHelper(favBuddyHelper);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvResult.setAdapter(adapter);
        rvResult.setLayoutManager( mLayoutManager );


        srlUpdateFav.setColorSchemeResources(R.color.google_1, R.color.google_2, R.color.google_3, R.color.google_4);
        srlUpdateFav.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryBuddies();
            }
        });
        return root;
    }


    @Override
    public void onResume() {
        super.onResume();

        favBuddyHelper.startListeningFirestore();
        queryBuddies();
        srlUpdateFav.setRefreshing(true);
    }

    @Override
    public void onPause() {
        favBuddyHelper.stopListeningFirestore();
        if (userSnapshotsListener != null){
            userSnapshotsListener.remove();
            userSnapshotsListener = null;
        }

        super.onPause();
    }



    @Override
    public void onFavBuddiesChanges(FavBuddyRecord record) {

    }

    @Override
    public void onFavBuddiesUpdate(boolean success) {

    }

    private void queryBuddies(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userRef = db.collection(GymHelper.GYM_USERS_COLLECTION);

        userRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                listUsers.clear();
                listUsers.addAll(snapshots.toObjects(User.class));
                performFiltering();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "query all users failed");
            }
        });
        userRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Log.w(TAG, "Listen failed", e);
                    if (srlUpdateFav.isRefreshing()){
                        srlUpdateFav.setRefreshing(false);
                    }
                    return;
                }

                listUsers.clear();
                listUsers.addAll(snapshots.toObjects(User.class));
                performFiltering();
            }
        });
    }

    private void performFiltering(){
        listFavUsers.clear();
        if (favBuddyHelper.getFavBuddyRecord()!=null){
            String curUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            for (User user:listUsers) {
                if (user.getUid().equals(curUserId)){
                    continue;
                }
                if (favBuddyHelper.getFavBuddyRecord().getBuddiesId().contains(user.getUid())){
                    listFavUsers.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();

        Log.d(TAG, "user.size->"+listUsers.size());
        Log.d(TAG, "buddies.size->"+listFavUsers.size());
        if (srlUpdateFav.isRefreshing()){
            srlUpdateFav.setRefreshing(false);
        }
    }

    @Override
    public void onBuddyItemClicked(View view, BuddyResultAdapter.ViewHolder holder, int action) {
        User user = listFavUsers.get(holder.getAdapterPosition());
        switch (action){
            case BuddyResultAdapter.ACTION_CLICK_ON_FAV_ITEM:
                unfavUser(user);
                break;

            case BuddyResultAdapter.ACTION_CLICK_ON_ITEM_BODY:
                Snackbar.make(rvResult, "to chat", Snackbar.LENGTH_SHORT).show();
                goChatActivity(user);
                break;

            case BuddyResultAdapter.ACTION_CLICK_ON_ITEM_PIC:
                displayBuddyProfile(user, ((ImageView)view).getDrawable() );
                break;
            default:

        }
    }

    private void displayBuddyProfile(User user, Drawable drawable){
        // inflate dialog layout
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View view = layoutInflater.inflate(R.layout.dialog_bd_profile, null);

        ImageView imgPic = view.findViewById(R.id.profile_pic);
        TextView tvName = view.findViewById(R.id.tv_bd_name);
        TextView tvLocation = view.findViewById(R.id.tv_pref_location);
        TextView tvTime = view.findViewById(R.id.tv_pref_time);
        LinearLayout llPrefDays = view.findViewById(R.id.ll_pref_days);

        imgPic.setImageDrawable(drawable);
        tvName.setText(user.getName());
        tvLocation.setText(user.getPrefLocation());
        tvTime.setText(user.getPrefTime());

        Drawable drawableLeft;
        if (user.getGender().equals("Male")) {
            drawableLeft = getResources().getDrawable(R.drawable.ic_human_male);
        } else {
            drawableLeft = getResources().getDrawable(R.drawable.ic_human_female);
        }
        tvName.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);

        for (int i =0; i<llPrefDays.getChildCount(); i++){
            CheckBox cb = (CheckBox) llPrefDays.getChildAt(i);
            cb.setEnabled(false);
            cb.setText(cb.getText().subSequence(0,1));
        }
        ((CheckBox) llPrefDays.getChildAt(0)).setChecked(user.getPrefDay().getMonday());
        ((CheckBox) llPrefDays.getChildAt(1)).setChecked(user.getPrefDay().getTuesday());
        ((CheckBox) llPrefDays.getChildAt(2)).setChecked(user.getPrefDay().getWednesday());
        ((CheckBox) llPrefDays.getChildAt(3)).setChecked(user.getPrefDay().getThursday());
        ((CheckBox) llPrefDays.getChildAt(4)).setChecked(user.getPrefDay().getFriday());
        ((CheckBox) llPrefDays.getChildAt(5)).setChecked(user.getPrefDay().getSaturday());
        ((CheckBox) llPrefDays.getChildAt(6)).setChecked(user.getPrefDay().getSunday());


        // build & display dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Profile")
                .setView(view)
                .setPositiveButton("Cancel",null)
                .show();
    }

    private void unfavUser(User other){
        listFavUsers.remove(other);
        favBuddyHelper.removeFavBuddy(other);
        adapter.notifyDataSetChanged();
        Snackbar snackbar = Snackbar.make(rvResult, R.string.txt_msg_removed_favourite, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.txt_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                favBuddyHelper.addFavBuddy(other);
                listFavUsers.add(other);
                adapter.notifyDataSetChanged();
                Snackbar.make(rvResult, R.string.txt_msg_removed_favtorite_undone, Snackbar.LENGTH_SHORT).show();

            }
        });
        snackbar.show();
    }

    private void goChatActivity(User other){
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        Bundle data = new Bundle();
        data.putString("buddy_id", other.getUid());
        data.putString("buddy_name", other.getName());
        data.putString("buddy_pic_url", other.getProfilePicUri());
        intent.putExtras(data);
        startActivity(intent);
    }
}