package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL


/**
 * Helper class for handling profile pictures in the application
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-09
 */
object ProfilePicHelper {

    /**
     * Generates a bitmap given a [url] to the image to download and generate
     * @param url String URL of the image to download and generate a bitmap of
     * @return Bitmap? The bitmap of the image you are downloading
     */
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