package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper.GYM_COLLECTION
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 2/10/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class UpdateGymFavourites(activity: Activity, private val userid: String, private val gymId: String?, private val favStatus: Boolean, private val callback: Callback) : AsyncTask<Void, Void, Void>() {

    private val actRef = WeakReference(activity)

    interface Callback {
        fun onComplete(success: Boolean)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val activity = actRef.get() ?: return null
        if (gymId == null) {
            activity.runOnUiThread { callback.onComplete(false) }
            return null
        }
        val firebaseDb = FirebaseFirestore.getInstance()
        firebaseDb.collection(GYM_COLLECTION).document(gymId).get().addOnSuccessListener { snapshot ->
            Log.i(TAG, "Favourite Gym object retrieved, checking existance")
            val favGym: FavGymFirestore? = if (snapshot.exists()) {
                Log.i(TAG, "Favourite gym exists, updating favourites")
                snapshot.toObject(FavGymFirestore::class.java)
            } else {
                Log.i(TAG, "Gym was never favourited, creating new object")
                FavGymFirestore()
            }
            favGym?.let {
                val userList = it.userIds
                if (favStatus) {if (!userList.contains(userid)) userList.add(userid) } // Favourite if not inside already
                else userList.remove(userid) // Unfavourite
                it.count = userList.size
                Log.i(TAG, "Updating gym object. State: ${if (favStatus) "Added" else "Removed"}, Size: ${it.count}")
                firebaseDb.collection(GYM_COLLECTION).document(gymId).set(it).addOnSuccessListener { activity.runOnUiThread { callback.onComplete(true) } }
                    .addOnFailureListener { activity.runOnUiThread { callback.onComplete(false) } }
            } ?: activity.runOnUiThread { callback.onComplete(false) }
        }.addOnFailureListener { Log.e(TAG, "Error getting Firebase Collection", it); activity.runOnUiThread { callback.onComplete(false) } } // Error, Log it
        return null
    }

    companion object {
        private const val TAG = "UpdateGymFavourites"
    }

}