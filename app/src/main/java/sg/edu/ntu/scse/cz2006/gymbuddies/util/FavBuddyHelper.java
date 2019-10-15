package sg.edu.ntu.scse.cz2006.gymbuddies.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;

import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavBuddyRecord;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;


/**
 * FavBuddyHelper is helper class to assist retrieving data and saving data to firestore for fav buddies
 *
 * @author Chia Yu
 * @since 2019-10-04
 */
public class FavBuddyHelper {
    private static final String TAG = "GB.FS.FavBuddy";
    public static final String COLLECTION_FAV_BUDDY = "favbuddy";

    public interface OnFavBuddiesUpdateListener{
        void onFavBuddiesChanges(FavBuddyRecord record);
        void onFavBuddiesUpdate(boolean success);
    }

    private ListenerRegistration fsListener;
    private FirebaseFirestore firestore;
    private DocumentReference favBuddiesRef;
    private OnFavBuddiesUpdateListener listener;
    private FavBuddyRecord favBuddyRecord;




    public FavBuddyHelper(){
        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        favBuddiesRef = firestore.collection(COLLECTION_FAV_BUDDY).document(user.getUid());
    }

    public FavBuddyRecord getFavBuddyRecord(){
        return this.favBuddyRecord;
    }

    public void setUpdateListener(OnFavBuddiesUpdateListener listener) {
        this.listener = listener;
    }

    public void startListeningFirestore(){
        favBuddiesRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                readDocumentSnapshot(documentSnapshot);
            }
        });

        fsListener =  favBuddiesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Log.w(TAG, "Listen failed", e);
                    return;
                }
                readDocumentSnapshot(documentSnapshot);
            }
        });
    }
    public void stopListeningFirestore(){
        fsListener.remove();
        fsListener = null;
    }

    private void readDocumentSnapshot(DocumentSnapshot docSnapshot){
//        Log.d(TAG, "size: "+queryDocumentSnapshots.size());
//        for ( DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
//            Set keys = doc.getData().keySet();
//            for (Object keyObj: keys ) {
//                String key = (String) keyObj;
//                Log.d(TAG, key+": " +doc.get(key));
//            }
//        }

        Log.d(TAG, ""+docSnapshot.toString());
        favBuddyRecord = docSnapshot.toObject(FavBuddyRecord.class);
            if (listener!=null){
                listener.onFavBuddiesChanges(favBuddyRecord);
        }
    }



    OnSuccessListener updateSuccessListener = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.d(TAG, "update favBuddyRecord success" );
            if (listener!=null){
                listener.onFavBuddiesUpdate(true);
            }
        }
    };

    OnFailureListener updateFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d(TAG, "update favBuddyRecord failed" );
            e.printStackTrace();
            if (listener!=null){
                listener.onFavBuddiesUpdate(false);
            }
        }
    };

    public void addFavBuddy(User other){
        doAddFavBuddies(other).addOnSuccessListener( updateSuccessListener)
                .addOnFailureListener(updateFailureListener);
    }
    public void removeFavBuddy(User other){
        doRemoveFavBuddies(other).addOnSuccessListener( updateSuccessListener)
                .addOnFailureListener(updateFailureListener);
    }


    private Task<Void> doAddFavBuddies(User other) {
        return firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                FavBuddyRecord favRecord = transaction.get(favBuddiesRef).toObject(FavBuddyRecord.class);
                if (favRecord == null){
                    favRecord = new FavBuddyRecord();
                }

                if (!favRecord.getBuddiesId().contains(other.getUid())){
                    favRecord.getBuddiesId().add(other.getUid());
                }

                // Commit to Firestore
                transaction.set(favBuddiesRef, favRecord);
                return null;
            }
        });
    }

    private Task<Void> doRemoveFavBuddies(User other) {
        return firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                FavBuddyRecord favRecord = transaction.get(favBuddiesRef).toObject(FavBuddyRecord.class);
                if (favRecord == null){
                    favRecord = new FavBuddyRecord();
                }

                if (favRecord.getBuddiesId().contains(other.getUid())){
                    favRecord.getBuddiesId().remove(other.getUid());
                }

                // Commit to Firestore
                transaction.set(favBuddiesRef, favRecord);
                return null;
            }
        });
    }

}
