package sg.edu.ntu.scse.cz2006.gymbuddies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_carpark_and_search_result.*
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.FavGymAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.StringRecyclerAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymSearchBy
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.SearchGym

class CarparkAndSearchResultActivity : AppCompatActivity() {

    private var state = STATE_SEARCH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpark_and_search_result)

        results_list.setHasFixedSize(true)
        val llm = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        results_list.layoutManager = llm
        results_list.itemAnimator = DefaultItemAnimator()

        state = when {
            intent.getBooleanExtra("search", false) -> STATE_SEARCH
            intent.getBooleanExtra("carpark", false) -> STATE_CARPARK
            else -> STATE_UNKNOWN
        }

        when (state) {
            STATE_SEARCH -> doSearch()
            STATE_CARPARK -> doCarpark()
            else -> errorAndExit("Unknown action, exiting activity")
        }
    }

    private fun errorAndExit(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun doSearch() {
        // Get filter results
        val paramJson = intent.getStringExtra("searchparam")
        if (paramJson == null) {
            errorAndExit("Search Paramters not found, no results, exiting activity")
            return
        }
        val gson = Gson()
        val param = gson.fromJson(paramJson, GymSearchBy::class.java)
        if (param == null) {
            errorAndExit("Failed to parse params")
            return
        }

        // Check user location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Enable after setting up map: if (mMap != null) { hasGps(true) }
            // TODO: Do the search in an async task
            callSearchTask(param)
        } else {
            // TODO: Request GPS Permissions
            Log.i(TAG, "No permissions, requesting...")
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this).setTitle("Location Permission Required").setMessage("We require access to your location to view nearby gyms")
                    .setPositiveButton(android.R.string.ok) { _, _ -> ActivityCompat.requestPermissions(this, permissions, RC_LOC_SEARCH) }.show()
            } else ActivityCompat.requestPermissions(this, permissions, RC_LOC_SEARCH)
        }
    }

    private fun callSearchTask(param: GymSearchBy) {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location == null) return@addOnSuccessListener
            val lastLocation = LatLng(location.latitude, location.longitude)
            SearchGym(this, object: SearchGym.OnComplete { override fun onComplete(result: ArrayList<FavGymObject>) { onSearchResult(result) } }, param, lastLocation).execute()
        }
    }

    private fun onSearchResult(result: ArrayList<FavGymObject>) {
        val noReviews = arrayOf("No results found from your search")

        if (result.size > 0) {
            val results = FavGymAdapter(result)
            results_list.adapter = results
        } else {
            val noResults = StringRecyclerAdapter(noReviews.toList(), false)
            results_list.adapter = noResults
        }
        updateResultsLayoutHeight()
        loading.visibility = View.GONE
    }

    private fun doCarpark() {
        // TODO: For carpark
    }

    private fun updateResultsLayoutHeight() {
        val scale = resources.displayMetrics.density
        val maxHeight = (450 * scale + 0.5f).toInt()
        bottom_sheet.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val layoutHeight = bottom_sheet.measuredHeight
        Log.d(TAG, "ResListHeight: $layoutHeight | Max Height Limit: $maxHeight")
        val params = bottom_sheet.layoutParams
        if (layoutHeight > maxHeight)
            params.height = maxHeight
        else
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        bottom_sheet.layoutParams = params
        bottom_sheet.requestLayout()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_LOC_SEARCH -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted - initialize the gps source")
                    doSearch()
                    return
                } else errorAndExit("Location Permission not granted, search cannot continue")
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val TAG = "CarparkAndSearch"
        private const val RC_LOC_SEARCH = 1
        const val STATE_SEARCH = 0
        const val STATE_CARPARK = 1
        const val STATE_UNKNOWN = -1
    }
}
