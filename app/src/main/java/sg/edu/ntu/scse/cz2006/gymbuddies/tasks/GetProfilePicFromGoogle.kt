package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import sg.edu.ntu.scse.cz2006.gymbuddies.util.ProfilePicHelper
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 9/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class GetProfilePicFromGoogle(activity: Activity, var callback: Callback) : AsyncTask<Uri, Void, Void>() {
    interface Callback {
        fun onComplete(bitmap: Bitmap?)
    }

    private val actRef = WeakReference<Activity>(activity)


    override fun doInBackground(vararg imageUris: Uri?): Void? {
        if (imageUris.isEmpty()) return null
        val act = actRef.get() ?: return null

        val bmp = ProfilePicHelper.getImageBitmap(imageUris[0].toString())
        act.runOnUiThread { callback.onComplete(bitmap = bmp) }

        return null
    }
}