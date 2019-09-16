package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Created by Kenneth on 16/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 */
data class User(var name: String = "", var flags: Flags = Flags()) {
    data class Flags(var firstRun: Boolean = true)
}