package sg.edu.ntu.scse.cz2006.gymbuddies

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile_edit.*
import kotlinx.android.synthetic.main.row_pref_days.*
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromGoogle
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateFirebaseFirestoreDocument
import sg.edu.ntu.scse.cz2006.gymbuddies.util.InputHelper

class ProfileEditActivity : AppCompatActivity() {

    private var firstRun = false
    private var profileImage: Bitmap? = null
    private var profileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        firstRun = intent.getBooleanExtra("firstrun", false)
        Log.d(TAG, "FirstRun: $firstRun")

        if (firstRun) {
            val auth = FirebaseAuth.getInstance().currentUser!!
            if (!auth.displayName.isNullOrEmpty()) etName.setText(auth.displayName)
            if (auth.photoUrl != null) {
                // Set Uri of user
                profileUri = auth.photoUrl
                GetProfilePicFromGoogle(this, object: GetProfilePicFromGoogle.Callback {
                    override fun onComplete(bitmap: Bitmap?) {
                        // Update image drawable
                        profileImage = bitmap
                        val roundBitmap = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        roundBitmap.isCircular = true
                        profile_pic.setImageDrawable(roundBitmap)
                    }
                }).execute(profileUri)
            }
        } else {
            // TODO: Retrieve existing data if not first run from database
        }
        fab.setOnClickListener {
            InputHelper.hideSoftKeyboard(this)
            if (validate()) {
                addOrUpdate()
            } else {
                Snackbar.make(coordinator, "Please complete your profile before continuing", Snackbar.LENGTH_LONG).show()
            }
        }
        profile_pic.setOnClickListener {
            // TODO: Allow user to take picture or upload picture
            Snackbar.make(coordinator, "Coming Soon", Snackbar.LENGTH_LONG).show()
            // TODO: Add profile pic into cloud storage for retrieval
        }
    }

    private fun validate(): Boolean {
        // Check all fields set up and filled
        til_etName.isErrorEnabled = false
        val name = etName.text.toString()
        val prefLocation = location.selectedItem.toString()
        val gender = findViewById<RadioButton>(radio_gender.checkedRadioButtonId).text
        val timeRange = findViewById<RadioButton>(radio_time.checkedRadioButtonId).text
        val selectedDays = getSelectedDays()
        Log.d(TAG, "Validating: \"$name\" | $prefLocation | $gender | $timeRange | Selected Days: ${selectedDays.joinToString(",")} (${selectedDays.size})")

        // Validation
        if (name.isEmpty()) {
            til_etName.error = "Please enter a valid name"
            til_etName.isErrorEnabled = true
            return false
        }
        if (selectedDays.isEmpty()) return false

        return true
    }

    private fun addOrUpdate() {
        Snackbar.make(coordinator, "Adding uh. IE to say its coming soon", Snackbar.LENGTH_LONG).show()
        val name = etName.text.toString()
        val prefLocation = location.selectedItem.toString()
        val gender = findViewById<RadioButton>(radio_gender.checkedRadioButtonId).text.toString()
        val timeRange = findViewById<RadioButton>(radio_time.checkedRadioButtonId).text.toString()
        val selectedDays = getSelectedDays()
        if (firstRun) {
            val user = User(name=name, prefLocation = prefLocation, gender = gender, prefTime = timeRange, prefDay = User.PrefDays(selectedDays), profilePicUri = profileUri.toString())
            user.flags.firstRun = false
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
            UpdateFirebaseFirestoreDocument(this, ref, user, object: UpdateFirebaseFirestoreDocument.Callback {
                override fun onComplete(success: Boolean) {
                    Log.i(TAG, "Insertion Status: $success")
                    if (success) {
                        val userProfileBuilder = UserProfileChangeRequest.Builder().apply {
                            setDisplayName(name)
                            if (user.profilePicUri.isNotEmpty()) setPhotoUri(Uri.parse(user.profilePicUri))
                        }
                        FirebaseAuth.getInstance().currentUser!!.updateProfile(userProfileBuilder.build())
                        startActivity(Intent(this@ProfileEditActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Snackbar.make(coordinator, "An error occurred updating profile", Snackbar.LENGTH_LONG).show()
                    }
                }
            } ).execute()
        } else {
            Snackbar.make(coordinator, "Updating of profile is coming soon", Snackbar.LENGTH_LONG).show()
            // TODO: Update data in firebase if update
        }
    }

    private fun getSelectedDays(): ArrayList<Int> {
        val list = ArrayList<Int>()
        // Add accordingly (1 - Mon, 2 - Tues ... 7 - Sun
        if (cb_day1.isChecked) list.add(1)
        if (cb_day2.isChecked) list.add(2)
        if (cb_day3.isChecked) list.add(3)
        if (cb_day4.isChecked) list.add(4)
        if (cb_day5.isChecked) list.add(5)
        if (cb_day6.isChecked) list.add(6)
        if (cb_day7.isChecked) list.add(7)
        return list
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
