package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 16/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class CheckFirstRun(activity: Activity, private val callback: Callback) : AsyncTask<String, Void, Void>() {

    private val actRef = WeakReference(activity)

    interface Callback {
        fun isFirstRun(success: Boolean)
        fun isError()
    }

    override fun doInBackground(vararg p0: String?): Void? {
        val activity = actRef.get() ?: return null
        if (p0.isEmpty()) {
            Log.e(TAG, "No UID passed")
            activity.runOnUiThread { callback.isError() }
            return null
        }

        val uid = p0[0] as String
        Log.d(TAG, "Checking uid $uid")
        Log.i(TAG, "Obtaining user object")

        val firebaseDb = FirebaseFirestore.getInstance()
        firebaseDb.collection("users").document(uid).get().addOnSuccessListener {
            Log.i(TAG, "User object retrieved, checking existance")
            if (it.exists()) {
                Log.i(TAG, "User exists, checking if firstRun")
                val user = it.toObject(User::class.java) // Default no
                if (user == null || user.flags.firstRun) {
                    // First Run (Success)
                    doCallback(true, activity)
                } else {
                    // Go into main activity
                    // Not First Run
                    doCallback(false, activity)
                }
            } else {
                Log.i(TAG, "User does not exist, creating new user")
                // First Run
                doCallback(true, activity)
            }
        }.addOnFailureListener {
            Log.w(TAG, "Error getting Firebase Collection", it)
            // Error, Fail it
            activity.runOnUiThread { callback.isError() }
        }
        return null
    }

    private fun doCallback(success: Boolean, activity: Activity) {
        Log.d(TAG, "callback:$success")
        activity.runOnUiThread { callback.isFirstRun(success) }
    }

    companion object {
        private const val TAG = "CheckFirstRun"
    }

}