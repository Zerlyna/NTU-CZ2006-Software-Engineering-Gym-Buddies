package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList
import sg.edu.ntu.scse.cz2006.gymbuddies.util.JsonHelper
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 17/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class ParseGymDataFile(activity: Activity, private val callback: Callback) : AsyncTask<Void, Void, Void>() {
    private val actRef = WeakReference(activity)

    interface Callback {
        fun onComplete(results: ArrayList<MarkerOptions>?)
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
        val markers = ArrayList<MarkerOptions>()
        gymlist.gyms.forEach {
            markers.add(MarkerOptions().position(LatLng(it.geometry.getLat(), it.geometry.getLng())).title(it.properties.Name).snippet(generateAddress(it.properties)))
        }
        activity.runOnUiThread { callback.onComplete(markers) }
        return null
    }

    private fun generateAddress(prop: GymList.GymProperties): String {
        val sb = StringBuilder()
        prop.ADDRESSBLOCKHOUSENUMBER?.let { sb.append("$it ") }
        prop.ADDRESSBUILDINGNAME?.let { sb.append("$it ") }
        sb.append("${prop.ADDRESSSTREETNAME} ")
        if (prop.ADDRESSFLOORNUMBER != null && prop.ADDRESSUNITNUMBER != null) sb.append("#${prop.ADDRESSFLOORNUMBER}-${prop.ADDRESSUNITNUMBER} ")
        prop.ADDRESSPOSTALCODE.let { sb.append("S($it)")}
        return sb.toString()
    }
}