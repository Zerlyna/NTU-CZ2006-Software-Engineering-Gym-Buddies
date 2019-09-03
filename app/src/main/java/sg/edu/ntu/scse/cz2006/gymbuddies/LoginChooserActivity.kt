package sg.edu.ntu.scse.cz2006.gymbuddies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class LoginChooserActivity : AppCompatActivity() {

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_chooser)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }
}
