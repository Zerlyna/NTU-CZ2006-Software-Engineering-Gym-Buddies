package sg.edu.ntu.scse.cz2006.gymbuddies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ProfileEditActivity : AppCompatActivity() {

    private var firstRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        firstRun = intent.getBooleanExtra("firstrun", false)
        Log.d(TAG, "FirstRun: $firstRun")
    }

    override fun onBackPressed() {
        if (firstRun) {
            // Log user out
            finish()
            val logout = Intent(this, LoginChooserActivity::class.java).apply { putExtra("logout", true) }
            startActivity(logout)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "ProfileEdit"
    }

}
