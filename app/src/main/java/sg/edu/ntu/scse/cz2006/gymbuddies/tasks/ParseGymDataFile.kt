package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper
import sg.edu.ntu.scse.cz2006.gymbuddies.util.JsonHelper
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 17/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class ParseGymDataFile(activity: Activity, private val callback: Callback) : AsyncTask<Void, Void, Void>() {
    private val actRef = WeakReference(activity)

    interface Callback {
        fun onComplete(results: HashMap<MarkerOptions, GymList.GymShell>?)
    }

    override fun doInBackground(vararg p0: Void?): Void? {
        val activity = actRef.get() ?: return null
        val jsonString = JsonHelper.readFromRaw(activity, R.raw.gymlist)
        val gson = Gson()
        val gymlist = gson.fromJson(jsonString, GymList::class.java)
        if (gymlist == null) {
            activity.runOnUiThread { callback.onComplete(null) }
            return null
        }
        val markers = HashMap<MarkerOptions, GymList.GymShell>()
        gymlist.gyms.forEach { markers[MarkerOptions().position(LatLng(it.geometry.getLat(), it.geometry.getLng())).title(it.properties.Name).snippet(GymHelper.generateAddress(it.properties))] = it }
        activity.runOnUiThread { callback.onComplete(markers) }
        return null
    }
}