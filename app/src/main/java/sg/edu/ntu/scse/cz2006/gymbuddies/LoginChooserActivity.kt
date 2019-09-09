package sg.edu.ntu.scse.cz2006.gymbuddies

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login_chooser.*
import java.util.*
import kotlin.math.log


class LoginChooserActivity : AppCompatActivity() {

    private val mAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
        Log.i("FirebaseAuth", "Auth State changed")
        auth.currentUser?.providerData?.forEach {
            if (it.providerId.contains("firebase")) return@forEach
            Log.i("FirebaseAuth", "Doing further checks with ${it.providerId}")
            furtherChecks(it.providerId, auth.currentUser!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_chooser)

        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        updateUI(firebaseUser)

        btnSigning.setOnClickListener{
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
            if (btnSigning.text == "Sign In") {
                login()
            } else {
                logout()
            }
        }
        val isLogout = intent.extras?.getBoolean("logout", false) ?: false
        if (isLogout) logout(true) else autoLogin(firebaseUser)
    }

    private fun autoLogin(fbUser: FirebaseUser?) {
        if (fbUser == null) {
            login()
        } else {
            // Launch main activity and finish this activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            // Not signed in
            txtStatus.text = "Not logged in"
            btnSigning.text = "Sign In"
        } else {
            // Signed in
            var provider = "Unknown"
            firebaseUser.providerData.forEach {
                if (it.providerId.contains("firebase")) return@forEach
                provider = it.providerId
            }
            txtStatus.text = "Signed in with $provider"
            btnSigning.text = "Sign Out"
        }
    }

    private fun furtherChecks(provider: String, userObj: FirebaseUser) {
        if (provider.toLowerCase(Locale.getDefault()) == "password") {
            // Check if user email is verified
            if (!userObj.isEmailVerified) {
                // Send verification email and log them out
                userObj.sendEmailVerification()
                AlertDialog.Builder(this).apply {
                    setTitle("Email Verification Sent")
                    setMessage("A verification email has been sent to ${userObj.email}. You have to verify your email before continuing")
                    setCancelable(false)
                    setPositiveButton(android.R.string.ok) { _,_ -> logout(true) }
                }.show()
            }
        }
    }

    private fun login() {
        // Testing authentication
        val providers = listOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setLogo(R.mipmap.ic_launcher)
            .setIsSmartLockEnabled(false).setAvailableProviders(providers).build(), RC_SIGN_IN)
    }

    private fun logout(silent: Boolean = false) {
        if (!silent) Toast.makeText(this, "Signing Out", Toast.LENGTH_LONG).show()
        AuthUI.getInstance().signOut(this).addOnCompleteListener{
            if (it.isComplete && it.isSuccessful && !silent) Toast.makeText(this, "Logged Out!", Toast.LENGTH_LONG).show()
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            updateUI(firebaseUser)
            FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
            autoLogin(firebaseUser)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                Log.i("FirebaseAuth", "Authenticated")
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                updateUI(firebaseUser)
                FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
                autoLogin(firebaseUser)
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }

                if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "An unknown error has occurred", Toast.LENGTH_LONG).show()
                    Log.e("Auth", "Sign-in error: ", response.error)
                }
                autoLogin(null) // Try logging in again
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val RC_SIGN_IN = 1
    }
}
