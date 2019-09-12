package sg.edu.ntu.scse.cz2006.gymbuddies

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseUser
import io.fabric.sdk.android.Fabric
import java.util.*


class LoginChooserActivity : AppCompatActivity() {

    private val mAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
        Log.i("FirebaseAuth", "Auth State changed")
        if (auth.currentUser == null) return@AuthStateListener
        auth.currentUser?.providerData?.forEach {
            if (it.providerId.contains("firebase")) return@forEach
            Log.i("FirebaseAuth", "Doing further checks with ${it.providerId}")
            furtherChecks(it.providerId, auth.currentUser)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_chooser)

        Log.i("AppInit", "Initializing Error Handling")
        val fabric = Fabric.Builder(this).kits(Crashlytics()).debuggable(BuildConfig.DEBUG).build()
        if (!BuildConfig.DEBUG) Fabric.with(fabric)


        val isLogout = intent.extras?.getBoolean("logout", false) ?: false
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (isLogout) logout() else autoLogin(firebaseUser)
    }

    private fun autoLogin(fbUser: FirebaseUser?) {
        if (checkingFurther) return
        Log.d("LoginChk", "al:check")
        if (fbUser == null) {
            Log.d("LoginChk", "al:fail")
            login()
        } else {
            Log.d("LoginChk", "al:pass")
            // Launch main activity and finish this activity
            signInFlow = false
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private var checkingFurther = false
    private var signInFlow = false

    private fun furtherChecks(provider: String, userObj: FirebaseUser?) {
        checkingFurther = true
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
        userObj?.let {
            when (provider.toLowerCase(Locale.getDefault())) {
                "password" -> {
                    // Check if user email is verified
                    Log.d("LoginChk", "PW: FC")
                    if (!it.isEmailVerified) {
                        // Send verification email and log them out
                        it.sendEmailVerification().addOnCompleteListener{ task->
                            if (task.isSuccessful) {
                                if (it.email != null) {
                                    val msg = "A verification email has been sent to ${it.email}. You have to verify your email before continuing"
                                    if (!(this as Activity).isFinishing) {
                                        AlertDialog.Builder(this).apply {
                                            setTitle("Email Verification Sent")
                                            setMessage(msg)
                                            setCancelable(false)
                                            setPositiveButton(android.R.string.ok) { _, _ -> logout(true) }
                                        }.show()
                                    } else {
                                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                                        val logout = Intent(this, LoginChooserActivity::class.java)
                                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        logout.putExtra("logout", true)
                                        startActivity(logout)
                                        finish()
                                    }
                                }
                            } else {
                                if (task.exception is FirebaseTooManyRequestsException || task.exception is FirebaseAuthEmailException)
                                    Toast.makeText(this, "Check your mailbox for an existing verification email or try again in a while", Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(this, "An error occurred sending verification email, check your email for an existing verification email or try again later", Toast.LENGTH_SHORT).show()
                                logout(true)
                            }
                        }
                    } else {
                        checkingFurther = false
                        autoLogin(it)
                    }
                }
                else -> {
                    checkingFurther = false
                    Log.d("LoginChk", "PW: Other")
                    autoLogin(it)
                }
            }
        }
        checkingFurther = false
    }

    private fun login() {
        // Testing authentication
        if (signInFlow) return
        signInFlow = true
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
        val providers = listOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setLogo(R.mipmap.ic_launcher)
            .setIsSmartLockEnabled(false).setAvailableProviders(providers).build(), RC_SIGN_IN)
    }

    private fun logout(silent: Boolean = false) {
        Log.d("LoginChk", "logout")
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
        AuthUI.getInstance().signOut(this).addOnCompleteListener{
            if (it.isComplete && it.isSuccessful) {
                if (!silent) Toast.makeText(this, "Logged Out!", Toast.LENGTH_LONG).show()
                Log.d("LoginChk", "LogOut:onComplete")
                autoLogin(FirebaseAuth.getInstance().currentUser)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            signInFlow = false
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                Log.i("FirebaseAuth", "Authenticated")
                FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "An unknown error has occurred", Toast.LENGTH_SHORT).show()
                    Log.e("Auth", "Sign-in error: ", response.error)
                }
                Log.w("LoginChk", "Auth Failed")
                autoLogin(FirebaseAuth.getInstance().currentUser) // Try logging in again
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val RC_SIGN_IN = 1
    }
}
