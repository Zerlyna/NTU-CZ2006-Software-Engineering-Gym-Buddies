package sg.edu.ntu.scse.cz2006.gymbuddies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.MessageAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.ChatMessage;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "gb.act.chat";
    private EditText etMessage;
    private ImageButton imgBtnSend;
    private TextView tvTitle;
    private ImageView imgBuddyPic;
    private ArrayList<ChatMessage> messages;
    RecyclerView rvMessages;
    MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        etMessage = findViewById(R.id.et_msg);
        tvTitle = findViewById(R.id.toolbar_title);
        imgBuddyPic = findViewById(R.id.img_bd_pic);
        imgBtnSend = findViewById(R.id.btn_send);
        imgBtnSend.setOnClickListener(this);

        rvMessages = findViewById(R.id.rv_messages);

        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, "1");
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvMessages.setLayoutManager(mLayoutManager);
        rvMessages.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // check for pass over data
        if (getIntent().getExtras() == null){
            throw new IllegalArgumentException("Required data is not pass over.");
        } else {
            Log.d(TAG, "has extras");
            // buddy_id
            // buddy_name
            // buddy_pic_url
            Bundle data = getIntent().getExtras();
            for (String key : data.keySet()) {
                Log.d(TAG,  key+": "+data.get(key));
            }
        }


        tvTitle.setText(getIntent().getStringExtra("buddy_name"));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getProfilePic();
        addDummyData();
    }

    private void addDummyData(){
        int min = 1000*60;
        long time = System.currentTimeMillis();

        messages.add( new ChatMessage("Hi", "2", time-10*min));
        messages.add( new ChatMessage("Hi", "1", time-9*min));
        messages.add( new ChatMessage("Test 1", "1", time-6*min));
        messages.add( new ChatMessage("Test 2", "2", time-5*min));
        messages.add( new ChatMessage("Test 3", "2", time-4*min));
        messages.add( new ChatMessage("Test 3", "1", time-3*min));
        adapter.notifyDataSetChanged();
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
            Snackbar.make(v, "send button clicked", Snackbar.LENGTH_SHORT).show();
            String message = etMessage.getText().toString();
            if (message.length()>0){
                Log.d(TAG, "msg -> "+message);

                // do something
                messages.add(new ChatMessage(message, "1"));
                adapter.notifyDataSetChanged();
                etMessage.setText("");
            }

        }

    }

    private void getProfilePic(){
        String imgUrl = getIntent().getStringExtra("buddy_pic_url");

            new GetProfilePicFromFirebaseAuth(this, new GetProfilePicFromFirebaseAuth.Callback() {
                @Override
                public void onComplete(@Nullable Bitmap bitmap) {
                    if (bitmap != null) {
                        RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        roundBitmap.setCircular(true);

                        imgBuddyPic.setImageDrawable(roundBitmap);
                    }
                }
            }).execute(Uri.parse(imgUrl));
    }

}
