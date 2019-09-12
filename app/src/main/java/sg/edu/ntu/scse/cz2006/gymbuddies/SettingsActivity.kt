package sg.edu.ntu.scse.cz2006.gymbuddies

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.crashlytics.android.Crashlytics
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
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>("about")?.setOnPreferenceClickListener { Attribouter.from(context).show(); true }
            findPreference<Preference>("crash")?.setOnPreferenceClickListener { Crashlytics.getInstance().crash(); true }
        }
    }
}