package sg.edu.ntu.scse.cz2006.gymbuddies.ui.chatlist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.AppConstants;
import sg.edu.ntu.scse.cz2006.gymbuddies.ChatActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.ChatAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Chat;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewClickedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;

public class ChatListFragment extends Fragment implements OnRecyclerViewClickedListener<ChatAdapter.ChatViewHolder> {
    private static final String TAG = "gb.frag.chatlist";
    private ChatListViewModel chatListViewModel;


    SwipeRefreshLayout srlUpdateChats;
    ArrayList<Chat> chats;
    ArrayList<String> favUserIds;
    RecyclerView rvChats;
    ChatAdapter adapter;

    FirebaseFirestore firestore;
    ListenerRegistration queryChatListener;
    Query queryChats;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatListViewModel = ViewModelProviders.of(this).get(ChatListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chat_list, container, false);
//        final TextView textView = root.findViewById(R.id.text_send);
        chatListViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) { /*textView.setText(s);*/
            }
        });



        rvChats = root.findViewById(R.id.rv_chats);
        srlUpdateChats = root.findViewById(R.id.srl_update_chats);


        srlUpdateChats.setColorSchemeResources(R.color.google_1, R.color.google_2, R.color.google_3, R.color.google_4);
        srlUpdateChats.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something
                srlUpdateChats.setRefreshing(false);
            }
        });


        chats = new ArrayList<>();
        favUserIds = new ArrayList<>();
        adapter = new ChatAdapter(chats, favUserIds);
        adapter.setOnRecyclerViewClickedListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager( mLayoutManager );


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
        CollectionReference chatRef = firestore.collection(AppConstants.COLLECTION_CHAT);
        //TODO update query
        queryChats = chatRef.whereEqualTo(FieldPath.of(  "participant", uid), true);


        return root;
    }


    @Override
    public void onStart() {
        super.onStart();
        listenSnapshotChanges();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopListenSnapshotChanges();
    }

    private void listenSnapshotChanges(){
        queryChatListener = queryChats.addSnapshotListener((queryDocumentSnapshots, e)->{
            Log.d(TAG, "chat query -> onEvent ("+queryDocumentSnapshots+", "+e+")");
            if (e != null){
                Log.d(TAG, "error: "+e.getMessage());
                e.printStackTrace();
                return;
            }

            // read doc snapshots
            if (queryDocumentSnapshots==null){
                Log.d(TAG, "chat query -> onEvent.snapshots are null");
                return;
            }
            readQuerySnapshot(queryDocumentSnapshots);
        });
    }

    private void stopListenSnapshotChanges(){
        if (queryChatListener!=null){
            queryChatListener.remove();
            queryChatListener = null;
        }
    }

    private void readQuerySnapshot(QuerySnapshot snapshots){
        Log.d(TAG, "reading chat data "+snapshots);
        chats.clear();
        Chat temp;
        for (DocumentSnapshot doc:snapshots){
            temp = doc.toObject(Chat.class);
            temp.setChatId(doc.getId());
            if (temp.getLastUpdate()>0){
                chats.add( temp );
            }
        }
        Log.d(TAG, "chat size -> "+chats.size());
        adapter.notifyDataSetChanged();
        queryUser();
    }

    private void queryUser(){
        Log.d(TAG, "query for user");
        CollectionReference userRef = firestore.collection(GymHelper.GYM_USERS_COLLECTION);
        userRef.get().addOnSuccessListener((snapshots)->{
            Log.d(TAG, "userRef.get() success");
            readUserAndUpdateChats(snapshots);
        }).addOnFailureListener((e)->{
            Log.d(TAG, "query all users failed");
        });
    }

    private void readUserAndUpdateChats(QuerySnapshot snapshots){
        Log.d(TAG, "readUserAndUpdateChats -> "+snapshots);
        ArrayList<User> users = new ArrayList<>();
        users.addAll(snapshots.toObjects(User.class));
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        for (Chat chat:chats){
            String otherUid = "";
            for (String userId:chat.getParticipant().keySet()) {
                if (!uid.equals(userId)){
                    otherUid = userId;
                    break;
                }
            }
            if (otherUid.length()==0){ continue; }
            for (User user:users){
                if (otherUid.equals(user.getUid())){
                    users.remove(user);
                    chat.setOtherUser(user);
                    break;
                }
            }
        }
        adapter.notifyDataSetChanged();
    }



    private void goChatActivity(Chat chat){
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            Bundle data = new Bundle();
            data.putString("chat_id", chat.getChatId());
//            data.putString("buddy_name", other.getName());
//            data.putString("buddy_pic_url", other.getProfilePicUri());
            intent.putExtras(data);
            startActivity(intent);

    }

    @Override
    public void onViewClicked(View view, ChatAdapter.ChatViewHolder holder, int action) {
        Chat chat = chats.get(holder.getAdapterPosition());
        switch (action){
            case ChatAdapter.ACTION_ITEM_VIEW_CLICKED:
                goChatActivity(chat);
                break;
            default:
                Log.d(TAG, "unknown action");
        }
    }
}