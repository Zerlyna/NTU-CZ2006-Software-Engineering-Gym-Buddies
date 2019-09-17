package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User

/**
 * Created by Kenneth on 16/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class UpdateFirebaseFirestoreDocument(var docRef: DocumentReference, var userObj: User, private var callback: Callback) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg p0: Void?): Boolean {
        docRef.set(userObj)
        return true
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (!result) {
            Log.w(TAG, "An error occurred")
            callback.onComplete(false)
        } else {
            callback.onComplete(true)
        }
    }

    interface Callback {
        fun onComplete(success: Boolean)
    }

    companion object {
        private const val TAG = "UpdateFbFsObj"
    }
}