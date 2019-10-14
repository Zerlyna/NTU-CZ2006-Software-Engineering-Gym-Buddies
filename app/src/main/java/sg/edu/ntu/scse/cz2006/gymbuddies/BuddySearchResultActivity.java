package sg.edu.ntu.scse.cz2006.gymbuddies;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.BuddyResultAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;

public class BuddySearchResultActivity extends AppCompatActivity implements BuddyResultAdapter.OnBuddyClickedListener{
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
        adapter.setOnBuddyClickedListener(this);
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

        int[] arrPrefDays = getIntent().getExtras().getIntArray("pref_days");
        String gender = getIntent().getExtras().getString("gender");


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userRef = db.collection(GymHelper.GYM_USERS_COLLECTION);

        // step 1: limit to location
        Query q =  userRef.whereEqualTo("prefLocation", "East");

        // step 2: by gender
        if ( !gender.equalsIgnoreCase("Both")){
            q = q.whereEqualTo("gender", gender);
        }
        // TODO: step 3: by am/pm

        // step 4: by days
        if (arrPrefDays[0]==1){
            q = q.whereEqualTo( FieldPath.of(  "prefDay", "monday"), true);
        }
        if (arrPrefDays[1]==1){
            q = q.whereEqualTo( FieldPath.of(  "prefDay", "tuesday"), true);
        }
        if (arrPrefDays[2]==1){
            q = q.whereEqualTo( FieldPath.of(  "prefDay", "wednesday"), true);
        }
        if (arrPrefDays[3]==1){
            q = q.whereEqualTo( FieldPath.of(  "prefDay", "thursday"), true);
        }
        if (arrPrefDays[4]==1){
            q = q.whereEqualTo( FieldPath.of(  "prefDay", "friday"), true);
        }
        if (arrPrefDays[5]==1){
            q = q.whereEqualTo( FieldPath.of(  "prefDay", "saturday"), true);
        }
        if (arrPrefDays[6]==1){
            q = q.whereEqualTo( FieldPath.of(  "prefDay", "sunday"), true);
        }
        // step 4: by days
        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                pd.dismiss();
                listData.clear();
                listData.addAll(queryDocumentSnapshots.toObjects(User.class));
                adapter.notifyDataSetChanged();

                Log.d(TAG, "size: "+listData.size());
                for (User u : listData){
                    Log.d(TAG, u.getName()+", days: "+u.getPrefDay()+", "+u.getPrefLocation());
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

    @Override
    public void onBuddyItemClicked(BuddyResultAdapter.ViewHolder holder, int action, int position) {
        Log.d(TAG, "onBuddyItemClicked::action: "+action+", pos: "+position);
        // TODO: handle action event
    }

    @Override
    public void onBuddyItemCheckChanged(BuddyResultAdapter.ViewHolder holder, int action, int position, boolean checked) {
        Log.d(TAG, "onBuddyItemCheckChanged::action: "+action+", pos: "+position+", checked: "+checked);
        // TODO: handle action event
    }
}
