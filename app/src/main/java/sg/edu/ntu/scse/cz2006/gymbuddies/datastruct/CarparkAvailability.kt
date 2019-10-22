package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Created by Kenneth on 22/10/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 */
data class CarparkAvailability(val CarParkID: String = "", val Area: String = "", val Development: String = "Nanyang Technological University Singapore", val Location: String = "1.3462 103.6820",
                               val AvailableLots: Int = 0, val LotType: String = TYPE_CAR, val Agency: String = AGENCY_HDB) {

    companion object {
        const val TYPE_CAR = "C"
        const val TYPE_HEAVY_VEH = "H"
        const val TYPE_MOTOCYCLE = "Y"

        const val AGENCY_HDB = "HDB"
        const val AGENCY_LTA = "LTA"
        const val AGENCY_URA = "URA"
    }
}