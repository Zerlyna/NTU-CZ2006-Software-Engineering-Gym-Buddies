package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Created by Kenneth on 25/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class TrimNearbyGyms(private var count: Int, private val location: LatLng, private val markers: Set<MarkerOptions>, private val callback: Callback) : AsyncTask<Void, Void, ArrayList<MarkerOptions>>() {

    interface Callback {
        fun onComplete(results: ArrayList<MarkerOptions>)
    }

    private data class Distance(var distance: Float = 0f, var marker: MarkerOptions? = null)

    override fun doInBackground(vararg p0: Void?): ArrayList<MarkerOptions> {
        val distances = ArrayList<Distance>()

        // Location.distanceBetween()
        // Get distance from the current location
        Log.i(TAG, "Converting markers to positional value based on current user location")
        markers.forEach {
            val result = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, it.position.latitude, it.position.longitude, result)
            distances.add(Distance(result[0], it))
        }

        // Sort the distances from smallest to largest
        Log.i(TAG, "Sorting distance")
        distances.sortBy { it.distance }

        // Select the first x amount determined by count
        Log.i(TAG, "Retrieving first $count gyms")
        if (count > distances.size) {
            Log.w(TAG, "Count from user preference ($count) is larger than the gym list (${distances.size}), defaulting to full list")
            count = distances.size
        }
        val resultSubList = distances.subList(0, count)
        val result = ArrayList<MarkerOptions>()
        resultSubList.forEach{ it.marker?.let{ marker -> result.add(marker) } }

        // Send intent
        return result
    }

    override fun onPostExecute(result: ArrayList<MarkerOptions>) {
        super.onPostExecute(result)
        callback.onComplete(result)
    }

    companion object {
        private const val TAG = "TrimNearbyGyms"
    }
}