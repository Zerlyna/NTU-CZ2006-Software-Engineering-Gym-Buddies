package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.content.Context
import com.google.gson.Gson
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList

/**
 * Gym Helper Class Object
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-01
 */
object GymHelper {

    @JvmStatic private var gymList: GymList? = null

    @JvmStatic
    fun getGymList(context: Context): GymList? {
        if (gymList != null) return gymList
        val json = JsonHelper.readFromRaw(context, R.raw.gymlist)
        val gson = Gson()
        gymList = gson.fromJson(json, GymList::class.java)
        return gymList
    }

    /**
     * Generates the gym address based on the gym properties [prop] that you have passed in
     * @param prop GymProperties The Gym's Properties object
     * @return String Full Address of the gym
     */
    @JvmStatic
    fun generateAddress(prop: GymList.GymProperties): String {
        val sb = StringBuilder()
        prop.ADDRESSBLOCKHOUSENUMBER?.let { sb.append("$it ") }
        prop.ADDRESSBUILDINGNAME?.let { sb.append("$it ") }
        sb.append("${prop.ADDRESSSTREETNAME} ")
        if (prop.ADDRESSFLOORNUMBER != null && prop.ADDRESSUNITNUMBER != null) sb.append("#${prop.ADDRESSFLOORNUMBER}-${prop.ADDRESSUNITNUMBER} ")
        prop.ADDRESSPOSTALCODE.let { sb.append("S($it)")}
        return sb.toString()
    }

    /**
     * A constant for the gym collection in Firebase Firestore DB
     */
    const val GYM_COLLECTION = "favgym"
}