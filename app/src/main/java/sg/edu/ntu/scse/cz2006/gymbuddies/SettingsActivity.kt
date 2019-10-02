package sg.edu.ntu.scse.cz2006.gymbuddies

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.crashlytics.android.Crashlytics
import com.h6ah4i.android.preference.NumberPickerPreferenceCompat
import com.h6ah4i.android.preference.NumberPickerPreferenceDialogFragmentCompat
import me.jfenn.attribouter.Attribouter

/**
 * This activity is used to display the application settings page
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-12
 */
class SettingsActivity : AppCompatActivity() {

    /**
     * Function that is called when an activity is created
     * @param savedInstanceState Bundle? The Android saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * This function handles the selected menu [item]
     * @param item MenuItem The item that has been selected
     * @return Boolean true if completed, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    /**
     * An internal settings fragment for display the application settings
     * @property dialogFragmentTag String used to determine dialog fragment
     */
    class SettingsFragment : PreferenceFragmentCompat() {
        private val dialogFragmentTag = "androidx.preference.PreferenceFragment.DIALOG"
        /**
         * Function that is ran when the preference screen is being created
         * @param savedInstanceState Bundle The Android saved instance state
         * @param rootKey String? A root key if any that this preferences will derive from
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>("about")?.setOnPreferenceClickListener { Attribouter.from(context).show(); true }
            findPreference<Preference>("crash")?.setOnPreferenceClickListener { Crashlytics.getInstance().crash(); true }
            findPreference<Preference>("nearby-gyms")?.setOnPreferenceChangeListener { pref, newVal -> pref.summary = newVal.toString(); true}
            findPreference<Preference>("nearby-gyms")?.summary = preferenceManager.sharedPreferences.getInt("nearby-gyms", 10).toString()
        }

        /**
         * Internal function to handle custom [preference]
         * @param preference Preference Custom Preference Object selected
         */
        override fun onDisplayPreferenceDialog(preference: Preference) {
            // check if dialog is already showing
            if (fragmentManager!!.findFragmentByTag(dialogFragmentTag) != null) return
            val f: DialogFragment? = if (preference is NumberPickerPreferenceCompat) NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey())
            else null

            if (f != null) {
                f.setTargetFragment(this, 0)
                f.show(fragmentManager!!, dialogFragmentTag)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }
}