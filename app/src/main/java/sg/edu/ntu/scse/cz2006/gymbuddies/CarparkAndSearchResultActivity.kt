package sg.edu.ntu.scse.cz2006.gymbuddies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_carpark_and_search_result.*
import kotlinx.android.synthetic.main.fragment_gym_details.*
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.FavGymAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.StringRecyclerAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymSearchBy
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.SearchGym
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper

class CarparkAndSearchResultActivity : AppCompatActivity(), OnMapReadyCallback {

    private var state = STATE_SEARCH
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpark_and_search_result)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        results_list.setHasFixedSize(true)
        val llm = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        results_list.layoutManager = llm
        results_list.itemAnimator = DefaultItemAnimator()

        state = when {
            intent.getBooleanExtra("search", false) -> STATE_SEARCH
            intent.getBooleanExtra("carpark", false) -> STATE_CARPARK
            else -> STATE_UNKNOWN
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) gpsPerm = true

        map_view.onCreate(savedInstanceState)
        map_view.getMapAsync(this)
    }

    private fun errorAndExit(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun doSearch() {
        supportActionBar?.title = "Search Results"
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
            gpsPerm = true
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

    private var gpsPerm = false

    private fun callSearchTask(param: GymSearchBy) {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location == null) return@addOnSuccessListener
            val lastLocation = LatLng(location.latitude, location.longitude)
            zoomToMyLocation(lastLocation)
            SearchGym(this, object: SearchGym.OnComplete { override fun onComplete(result: ArrayList<FavGymObject>) { onSearchResult(result) } }, param, lastLocation).execute()
        }
    }

    private fun onSearchResult(result: ArrayList<FavGymObject>) {
        val noReviews = arrayOf("No results found from your search")

        if (result.size > 0) {
            val results = FavGymAdapter(result)
            results_list.adapter = results
            result.forEach {
                // Add markers for each as well
                mMap.addMarker(MarkerOptions().position(LatLng(it.gym.geometry.getLat(), it.gym.geometry.getLng())).title(it.gym.properties.Name).snippet(
                    GymHelper.generateAddress(it.gym.properties)))
            }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    // GMaps related issues

    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.isTrafficEnabled = true
        val settings = mMap.uiSettings
        settings.isMapToolbarEnabled = false
        settings.isMyLocationButtonEnabled = true

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(1.3413054, 103.8074233), 10f))
        val gymBottomSheetBehavior = BottomSheetBehavior.from<View>(gym_details_sheet)
        mMap.setOnInfoWindowClickListener { gymBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }
        mMap.isMyLocationEnabled = gpsPerm

        when (state) {
            STATE_SEARCH -> doSearch()
            STATE_CARPARK -> doCarpark()
            else -> errorAndExit("Unknown action, exiting activity")
        }
    }

    /**
     * Internal method to zoom the map to the user's current location
     */
    private fun zoomToMyLocation(lastLocation: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15f))
    }

    companion object {
        private const val TAG = "CarparkAndSearch"
        private const val RC_LOC_SEARCH = 1
        const val STATE_SEARCH = 0
        const val STATE_CARPARK = 1
        const val STATE_UNKNOWN = -1
    }
}
