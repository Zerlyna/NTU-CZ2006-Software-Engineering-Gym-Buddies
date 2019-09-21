package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 16/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class UploadProfilePic(activity: Activity, private val ref: StorageReference, private val bitmap: Bitmap, private val callback: Callback) : AsyncTask<Void, Void, Void>() {

    private val actRef = WeakReference(activity)

    interface Callback {
        fun onSuccess(success: Boolean, imageUri: Uri? = null)
    }

    override fun doInBackground(vararg p0: Void?): Void? {
        val activity = actRef.get() ?: return null
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val task = ref.putBytes(data)

        task.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            Log.d(TAG, "Upload Progress: $progress%")
        }.addOnFailureListener {
            Log.e(TAG, "Upload Failed (${it.message})", it)
            activity.runOnUiThread { callback.onSuccess(false) }
        }.addOnSuccessListener {
            Log.i(TAG, "Upload Successful, getting Download URL")
            ref.downloadUrl.addOnFailureListener {
                Log.e(TAG, "Retrieving download url Failed (${it.message})", it)
                activity.runOnUiThread { callback.onSuccess(false) }
            }.addOnSuccessListener {
                Log.i(TAG, "Download URL Obtained")
                Log.d(TAG, "Download URL: $it")
                activity.runOnUiThread { callback.onSuccess(true, it) }
            }
        }
        return null
    }

    companion object {
        private const val TAG = "UploadProfilePic"
    }
}