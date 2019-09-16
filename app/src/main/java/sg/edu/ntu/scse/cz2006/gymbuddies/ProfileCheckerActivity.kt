package sg.edu.ntu.scse.cz2006.gymbuddies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login_chooser.*
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User

/**
 * This activity is just used to ensure that we have processed everything from our database that we need to prevent
 *
 * This checks that
 * - The user has completed their first run sequence (MUST BE AUTHENTICATED)
 */
class ProfileCheckerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_chooser)

        val auth = FirebaseAuth.getInstance().currentUser
        if (auth == null) {
            Log.e(TAG, "User not valid, exiting")
            startActivity(Intent(this, LoginChooserActivity::class.java))
            finish()
            return
        }

        message.text = "Updating database..."

        val firebaseDb = FirebaseFirestore.getInstance()
        firebaseDb.collection("users").document(auth.uid).get().addOnSuccessListener {
            if (it.exists()) {
                val user = it.toObject(User::class.java) // Default no
                if (user == null || user.flags.firstRun) {
                    // First Run
                    goEditProfile()
                } else {
                    // Go into main activity
                    startActivity(Intent(this, MainActivity::class.java))
                }
            } else {
                goEditProfile()
            }
        }.addOnFailureListener {
            Log.w(TAG, "Error getting Firebase Collection", it)
            startActivity(Intent(this, LoginChooserActivity::class.java))
        }
        finish()
        return
    }

    private fun goEditProfile() {
        val intent = Intent(this, ProfileEditActivity::class.java).apply {
            putExtra("firstrun", true)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "ProfileCheck"
    }
}
