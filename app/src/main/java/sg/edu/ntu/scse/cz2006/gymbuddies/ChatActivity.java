package sg.edu.ntu.scse.cz2006.gymbuddies;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.MessageAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Chat;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.ChatMessage;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;

public class ChatActivity extends AppCompatActivity implements AppConstants, View.OnClickListener {
    private static final String TAG = "gb.act.chat";
    private EditText etMessage;
    private ImageButton imgBtnSend;
    private TextView tvTitle;
    private ImageView imgBuddyPic;
    private ArrayList<ChatMessage> messages;
    RecyclerView rvMessages;
    MessageAdapter adapter;

    FirebaseFirestore firestore;
    DocumentReference chatRef;
    ListenerRegistration msgListener;
    Chat curChat;
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // init views
        etMessage = findViewById(R.id.et_msg);
        tvTitle = findViewById(R.id.toolbar_title);
        imgBuddyPic = findViewById(R.id.img_bd_pic);
        imgBtnSend = findViewById(R.id.btn_send);
        rvMessages = findViewById(R.id.rv_messages);

        imgBtnSend.setOnClickListener(this);
        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, uid);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvMessages.setLayoutManager(mLayoutManager);
        rvMessages.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        firestore = FirebaseFirestore.getInstance();

        // check for pass over data
        init();
    }

    private void init(){
        if (getIntent().hasExtra("chat_id")){
            // load chat > load message
            queryChatByChatId();
        } else if (getIntent().hasExtra("buddy_id")){
            queryChatByParticipants();
            // find or create chat > load message;
        } else {
            showDialogInvalidArgs();
        }

        if (getIntent().hasExtra("buddy_name")){
            tvTitle.setText(getIntent().getStringExtra("buddy_name"));
        }

        if (getIntent().hasExtra("buddy_pic_url")){
            getProfilePic(getIntent().getStringExtra("buddy_pic_url"));
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        listenToMessages();
    }

    @Override
    protected void onStop() {
        stopListenToMessages();
        super.onStop();
    }


    private void queryChatByChatId(){
        Log.d(TAG, "query Chat by chat Id");
        String chatId = getIntent().getStringExtra("chat_id");
        chatRef = firestore.collection(AppConstants.COLLECTION_CHAT).document(chatId);
        chatRef.get().addOnSuccessListener((documentSnapshot)->{
            curChat = documentSnapshot.toObject(Chat.class);

            listenToMessages();
        }).addOnFailureListener((e)->{
            Log.d(TAG, "query chat failed, e-> "+e.getMessage());
            e.printStackTrace();
        });

    }

    public void queryChatByParticipants(){
        Log.d(TAG, "find existing chat by uid(self and other)");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUid = getIntent().getStringExtra("buddy_id");


        CollectionReference chatCollectionRef = firestore.collection(AppConstants.COLLECTION_CHAT);
        Query queryChats = chatCollectionRef.whereEqualTo(FieldPath.of(  "participant", uid), true)
                .whereEqualTo(FieldPath.of(  "participant", otherUid), true);
        ListenerRegistration listener = queryChats.addSnapshotListener((queryDocumentSnapshots, e)->{
            Log.d(TAG, "chat query -> onEvent ("+queryDocumentSnapshots+", "+e+")");
            if (e!=null){
                Log.d(TAG, "Error query chat by participant");
                e.printStackTrace();
            }

            Log.d(TAG, "chat.size"+queryDocumentSnapshots.size());
            for (DocumentSnapshot snapshot:queryDocumentSnapshots) {
                Log.d(TAG, "doc id->"+snapshot.getId()+", "+snapshot.toObject(Chat.class));
            }

            // read chat or create new chat
            if (queryDocumentSnapshots.size() == 0){
                queryCreateChat();
            } else {

                curChat = queryDocumentSnapshots.toObjects(Chat.class).get(0);
                curChat.setChatId( queryDocumentSnapshots.getDocuments().get(0).getId());
                chatRef = firestore.collection(COLLECTION_CHAT).document(curChat.getChatId());
                // load messages
                listenToMessages();
            }
            // remove listener?
        });
    }

    private void queryCreateChat(){
        Log.d(TAG, "Create new chat");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUid = getIntent().getStringExtra("buddy_id");
        curChat = new Chat();
        curChat.getParticipant().put(uid, true);
        curChat.getParticipant().put(otherUid, true);

        firestore.collection(AppConstants.COLLECTION_CHAT).add(curChat)
                .addOnSuccessListener((DocumentReference documentReference)->{
                    Log.d(TAG, "queryCreateChat -> success("+documentReference.getId()+"");
                    curChat.setChatId(documentReference.getId());
                    listenToMessages();

                }).addOnFailureListener((e)->{
                    Log.d(TAG, "queryCreateChat -> failed("+e.getMessage()+"");
                    e.printStackTrace();
        });

    }

    private void listenToMessages(){
        Log.d(TAG, "listen to message");
        if (chatRef == null || (curChat !=null && curChat.getChatId()==null)){
            Log.d(TAG, "Missing Chat ID, Cannot listen to messages");
            return;
        };

        msgListener = chatRef.collection(COLLECTION_MESSAGES).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((snapshots, e) -> {
            Log.d(TAG, "Listen to message changes");
            if (e != null){
                Log.d(TAG, "Listen failed -> "+e.getMessage());
                e.printStackTrace();
            }

            messages.clear();
            if (snapshots != null){
                messages.addAll( snapshots.toObjects(ChatMessage.class));

            }else {
                Log.d(TAG, "snapshots are null");
            }
            adapter.notifyDataSetChanged();
        });
    }
    private void stopListenToMessages(){
        if (msgListener != null){
            msgListener.remove();
            msgListener = null;
        }
    }

    /**
     * Handle option menu action
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == imgBtnSend){
            String message = etMessage.getText().toString();
            if (message.length()>0){
                Log.d(TAG, "msg -> "+message);

                // do something
                ChatMessage newMsg = new ChatMessage(message, uid);
                commitMessage(newMsg);
                messages.add(newMsg);
                adapter.notifyDataSetChanged();
                etMessage.setText("");
            }

        }

    }

    private void getProfilePic(String imgUrl){
        if (imgUrl.equals(imgBuddyPic.getTag())){
            return;
        }

        new GetProfilePicFromFirebaseAuth(this, new GetProfilePicFromFirebaseAuth.Callback() {
            @Override
            public void onComplete(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    roundBitmap.setCircular(true);

                    imgBuddyPic.setImageDrawable(roundBitmap);
                    imgBuddyPic.setTag(imgUrl);
                }
            }
        }).execute(Uri.parse(imgUrl));

    }

    private void showDialogInvalidArgs(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invalid argument")
                .setMessage("Argument missing")
                .setCancelable(false)
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }



    private void commitMessage(ChatMessage msg){
        if (chatRef == null){
            Log.d(TAG, "chatRef is null");
            return;
        }

        final DocumentReference messageRef = chatRef.collection(COLLECTION_MESSAGES).document();
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                curChat.setLastMessage(msg.getMessage());
                curChat.setLastUpdate(msg.getTimestamp());

                transaction.set(chatRef, curChat);
                transaction.set(messageRef, msg);
                return null;
            }
        }).addOnSuccessListener((v)->{
            Log.d(TAG, "favRecord updated success");
        }).addOnFailureListener((e)->{
            Log.d(TAG, "favRecord updated failed");
            e.printStackTrace();
        });
    }

}
