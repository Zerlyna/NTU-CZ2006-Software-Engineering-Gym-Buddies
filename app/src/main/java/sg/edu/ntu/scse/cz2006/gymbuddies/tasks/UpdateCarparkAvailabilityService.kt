package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.CarparkAvailability
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.LtaObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Kenneth on 22/10/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class UpdateCarparkAvailabilityService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        onHandleWork(intent)
    }

    private fun onHandleWork(intent: Intent) {
        // TODO: Fetch all the data from LTA API
        // Check for API key
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val apikey = sp.getString("ltakey", "invalid")
        if (apikey == "invalid") {
            Log.e(TAG, "No LTA API Key, not continuing")
            return
        }

        var skip = 0
        val gson = Gson()
        val carparkList = ArrayList<CarparkAvailability>()
        Log.i(TAG, "Downloading latest data")
        while (true) {
            val url = "$LTA_URL$skip"
            val uri = URL(url)
            val conn = uri.openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT
            conn.readTimeout = TIMEOUT
            conn.requestMethod = "GET"
            conn.setRequestProperty("AccountKey", apikey)
            conn.connect()
            Log.d(TAG, "Connecting to $url")

            val data = conn.inputStream.bufferedReader().use { it.readLine() }
            try {
                val obj = gson.fromJson<LtaObject>(data, LtaObject::class.java)
                if (obj.value.size == 0) {
                    Log.i(TAG, "Finished parsing data, exiting loop")
                    break
                }
                carparkList.addAll(obj.value)
                skip += obj.value.size
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Failed to parse GSON from $data")
            }
        }

        // Write to file
        Log.i(TAG, "Writing carpark list to file, Availability Size: ${carparkList.size}")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Started Service Work")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Finished all work")
    }

    companion object {
        private const val TAG = "UpdateCarparkAvail"
        private const val LTA_URL = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2?\$skip="
        private const val TIMEOUT = 15000 // Timeout 15 seconds

        @JvmStatic
        fun updateCarpark(context: Context) {
            // Update carpark availability
            Log.i(TAG, "Updating carpark availability data")
            val intent = Intent(context, UpdateCarparkAvailabilityService::class.java)
            context.startService(intent)
        }
    }

}