package sg.edu.ntu.scse.cz2006.gymbuddies.util

import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList

/**
 * Created by Kenneth on 1/10/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 */
object GymHelper {

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
}