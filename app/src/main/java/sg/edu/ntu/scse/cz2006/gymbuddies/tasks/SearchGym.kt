package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.location.Location
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.*
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 14/10/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 */
class SearchGym(activity: Activity, private val callback: OnComplete, private val searchParams: GymSearchBy, private val userLocaltion: LatLng) : AsyncTask<Void, Void, Void>() {

    private val actRef = WeakReference(activity)

    override fun doInBackground(vararg params: Void?): Void? {
        val activity = actRef.get() ?: return null

        // Get gym list
        val gymList = GymHelper.getGymList(activity)
        if (gymList == null) {
            activity.runOnUiThread { callback.onComplete(ArrayList()) }
            return null
        }

        // Get fav gym objects for gym list
        val favourites = HashMap<String, FavGymFirestore>()
        val ratings = HashMap<String, GymRatingStats>()
        FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).get().addOnSuccessListener { favQuerySnapshot ->
            favQuerySnapshot.documents.forEach { it1 -> if (it1.toObject(FavGymFirestore::class.java) != null) favourites[it1.id] = it1.toObject(FavGymFirestore::class.java)!! }

            // Get Ratings as well
            FirebaseFirestore.getInstance().collection(GymHelper.GYM_REVIEWS_COLLECTION).get().addOnSuccessListener {
                it.documents.forEach { it1 -> if (it1.toObject(GymRatingStats::class.java) != null) ratings[it1.id] = it1.toObject(GymRatingStats::class.java)!! }

                // Do the processing
                furtherProcessing(favourites, ratings, gymList, activity)
            }
        }

        return null
    }

    private fun furtherProcessing(fav: HashMap<String, FavGymFirestore>, ratings: HashMap<String, GymRatingStats>, gymList: GymList, activity: Activity) {
        val originalGyms = gymList.gyms
        var filteredGyms: ArrayList<FavGymObject> = ArrayList()
        if (searchParams.rating != -1) {
            // Filter based on rating (greater than)
            val keepGym = originalGyms.filter { s: GymList.GymShell -> ratings.containsKey(s.properties.INC_CRC) && ratings[s.properties.INC_CRC]!!.averageRating >= searchParams.rating }
            keepGym.forEach {
                val favObj = fav[it.properties.INC_CRC] ?: FavGymFirestore()
                filteredGyms.add(FavGymObject(it, favObj.count))
            }
        } else {
            // Keep all
            originalGyms.forEach {
                val favObj = fav[it.properties.INC_CRC] ?: FavGymFirestore()
                filteredGyms.add(FavGymObject(it, favObj.count))
            }
        }

        // Add relevant rating info
        filteredGyms.forEach {
            val rate = ratings[it.gym.properties.INC_CRC] ?: GymRatingStats()
            it.avgRating = rate.averageRating
            it.ratingCount = rate.count
        }

        // Filter out everything that is not user's location
        val finalFilteredGym = ArrayList<FavGymObject>()
        filteredGyms.forEach {
            val res = FloatArray(1)
            Location.distanceBetween(userLocaltion.latitude, userLocaltion.longitude, it.gym.geometry.getLat(), it.gym.geometry.getLng(), res)
            if (res[0] <= searchParams.distance * 1000) finalFilteredGym.add(it)
        }

        // Order by stuff
        when (searchParams.sort) {
            GymSearchBy.SORT_ABC_ASC -> finalFilteredGym.sortBy { it.gym.properties.Name }
            GymSearchBy.SORT_ABC_DSC -> {
                finalFilteredGym.sortBy { it.gym.properties.Name }
                finalFilteredGym.reverse()
            }
            GymSearchBy.SORT_FAV_ASC -> {
                finalFilteredGym.sortBy { it.favCount }
                finalFilteredGym.reverse()
            }
            GymSearchBy.SORT_FAV_DSC -> finalFilteredGym.sortBy { it.favCount }
        }

        // Send back to user as success
        activity.runOnUiThread { callback.onComplete(finalFilteredGym) }
    }

    interface OnComplete {
        fun onComplete(result: ArrayList<FavGymObject>)
    }
}