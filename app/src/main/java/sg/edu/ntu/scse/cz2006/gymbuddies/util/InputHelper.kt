package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.app.Activity
import android.view.inputmethod.InputMethodManager


/**
 * Created by Kenneth on 16/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 */
object InputHelper {

    @JvmStatic
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isAcceptingText) inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }
}