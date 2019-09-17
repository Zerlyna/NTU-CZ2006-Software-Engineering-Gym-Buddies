package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Created by Kenneth on 17/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 */
data class GymList(val name: String = "", val gyms: ArrayList<GymShell> = ArrayList()) {
    data class GymShell(val properties: GymProperties = GymProperties(), val geometry: GymGeometry = GymGeometry())

    data class GymProperties(val Name: String = "", val description: String = "", val altitudeMode: String = "clampToGround", val INC_CRC: String = "PRIMARYKEY",
                             val ADDRESSPOSTALCODE: String = "111111", val ADDRESSUNITNUMBER: String? = null, val ADDRESSBUILDINGNAME: String? = null,
                             val ADDRESSFLOORNUMBER: String? = null, val ADDRESSSTREETNAME: String = "Knn Francis Rd", val ADDRESSBLOCKHOUSENUMBER: String? = null) {
        fun getPostalCodeInt(): Int { return try { ADDRESSPOSTALCODE.toInt() } catch (e: NumberFormatException) { 0 } }
    }

    data class GymGeometry(val coordinates: ArrayList<Double> = ArrayList()) {
        fun getLat(): Double { return if (coordinates.size > 1) coordinates[1] else 0.0 }
        fun getLng(): Double { return if (coordinates.size > 0) coordinates[0] else 0.0 }
    }
}