package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

import com.google.firebase.firestore.Exclude

/**
 * Created by Kenneth on 16/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 */
data class User(var name: String = "", var prefLocation: String = "West", var gender: String = "Male", var prefTime: String = "AM",
                var prefDay: PrefDays = PrefDays(), var profilePicUri: String = "", var flags: Flags = Flags(), var gymFavourites: ArrayList<String> = ArrayList()) {
    data class Flags(var firstRun: Boolean = true)
    data class PrefDays(var monday: Boolean = false, var tuesday: Boolean = false, var wednesday: Boolean = false,
                        var thursday: Boolean = false, var friday: Boolean = false, var saturday: Boolean = false, var sunday: Boolean = false) {
        constructor(list: ArrayList<Int>) : this() {
            list.forEach {
                when (it) {
                    1 -> monday = true
                    2 -> tuesday = true
                    3 -> wednesday = true
                    4 -> thursday = true
                    5 -> friday = true
                    6 -> saturday = true
                    7 -> sunday = true
                }
            }
        }
        @Exclude fun getDays(): ArrayList<Int> {
            val list = ArrayList<Int>()
            // Add accordingly (1 - Mon, 2 - Tues ... 7 - Sun
            if (monday) list.add(1)
            if (tuesday) list.add(2)
            if (wednesday) list.add(3)
            if (thursday) list.add(4)
            if (friday) list.add(5)
            if (saturday) list.add(6)
            if (sunday) list.add(7)
            return list
        }
        @Exclude fun getDaysCSV(): String {
            return getDays().joinToString(",")
        }
    }
}