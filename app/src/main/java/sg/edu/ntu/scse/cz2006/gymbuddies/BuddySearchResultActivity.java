package sg.edu.ntu.scse.cz2006.gymbuddies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.BuddyResultAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;

public class BuddySearchResultActivity extends AppCompatActivity {
    private String TAG = "GB.BuddySearchResult";
    private RecyclerView rvResult;
    ArrayList<User> listData;
    BuddyResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_search_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // TODO: remove testing log
        if (getIntent().getExtras() != null){
            Log.d(TAG, "has extras");
            Bundle data = getIntent().getExtras();
            for (String key : data.keySet()) {
                Log.d(TAG,  key+": "+data.get(key));
            }
        } else {
            // display error message!
        }

        listData = new ArrayList<>();
        adapter = new BuddyResultAdapter(listData);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);

        rvResult = findViewById(R.id.rv_buddies);
        rvResult.setAdapter(adapter);
        rvResult.setLayoutManager( mLayoutManager );
        adapter.notifyDataSetChanged();

        queryBuddy();
    }


    private void queryBuddy(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("loading");
        pd.show();

        // TODO: Add WHERE Clause on query
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userRef = db.collection("users");
        userRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                pd.dismiss();
                listData.clear();
                listData.addAll(queryDocumentSnapshots.toObjects(User.class));
                adapter.notifyDataSetChanged();

                Log.d(TAG, "size: "+listData.size());
                for (User u : listData){
                    Log.d(TAG, u.getName()+", pic: "+u.getProfilePicUri()+", days: "+u.getPrefDay());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                pd.dismiss();
            }
        });

    }


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
}
