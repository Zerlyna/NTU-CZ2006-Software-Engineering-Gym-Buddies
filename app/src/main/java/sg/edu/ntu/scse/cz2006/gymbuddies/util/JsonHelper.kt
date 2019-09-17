package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.content.Context
import androidx.annotation.RawRes
import java.io.BufferedReader


/**
 * Created by Kenneth on 17/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 */
object JsonHelper {

    @JvmStatic
    fun readFromRaw(context: Context, @RawRes resId: Int): String { return context.resources.openRawResource(resId).bufferedReader().use(BufferedReader::readText) }
}