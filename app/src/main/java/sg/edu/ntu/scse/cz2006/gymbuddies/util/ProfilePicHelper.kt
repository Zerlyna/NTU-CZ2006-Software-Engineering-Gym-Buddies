package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL


/**
 * Created by Kenneth on 9/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 */
object ProfilePicHelper {

    @JvmStatic
    fun getImageBitmap(url: String): Bitmap? {
        var bm: Bitmap? = null
        try {
            val aURL = URL(url)
            val conn = aURL.openConnection()
            conn.connect()
            val `is` = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close()
        } catch (e: IOException) {
            Log.e("ProfilePicHelper", "Error getting bitmap", e)
        }

        return bm
    }
}