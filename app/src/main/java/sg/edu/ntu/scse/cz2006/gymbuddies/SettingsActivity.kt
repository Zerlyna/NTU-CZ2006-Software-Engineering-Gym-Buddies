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


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>("about")?.setOnPreferenceClickListener { Attribouter.from(context).show(); true }
            findPreference<Preference>("crash")?.setOnPreferenceClickListener { Crashlytics.getInstance().crash(); true }
            findPreference<Preference>("nearby-gyms")?.setOnPreferenceChangeListener { pref, newVal -> pref.summary = newVal.toString(); true}
            findPreference<Preference>("nearby-gyms")?.summary = preferenceManager.sharedPreferences.getInt("nearby-gyms", 10).toString()
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            // check if dialog is already showing
            if (fragmentManager!!.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) return
            val f: DialogFragment? = if (preference is NumberPickerPreferenceCompat) NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey())
            else null

            if (f != null) {
                f.setTargetFragment(this, 0)
                f.show(fragmentManager!!, DIALOG_FRAGMENT_TAG)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }
}