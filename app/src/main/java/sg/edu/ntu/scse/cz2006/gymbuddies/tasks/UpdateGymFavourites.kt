package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User
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
        firebaseDb.collection("users").document(userid).get().addOnSuccessListener { snapshot ->
            Log.i(TAG, "User object retrieved, checking existance")
            if (snapshot.exists()) {
                Log.i(TAG, "User exists, updating favourites")
                val user = snapshot.toObject(User::class.java)
                user?.let {
                    val favArray = it.gymFavourites
                    if (favStatus) {if (!favArray.contains(gymId)) favArray.add(gymId) } // Favourite if not inside already
                    else favArray.remove(gymId) // Unfavourite
                    Log.i(TAG, "Saving new favourites list. State: ${if (favStatus) "Added" else "Removed"}, Size: ${favArray.size}")
                    firebaseDb.collection("users").document(userid).set(it).addOnSuccessListener { activity.runOnUiThread { callback.onComplete(true) } }
                        .addOnFailureListener { activity.runOnUiThread { callback.onComplete(false) } }
                }
            } else activity.runOnUiThread { callback.onComplete(false) }
        }.addOnFailureListener { Log.e(TAG, "Error getting Firebase Collection", it); activity.runOnUiThread { callback.onComplete(false) } } // Error, Log it
        return null
    }

    companion object {
        private const val TAG = "UpdateGymFavourites"
    }

}