package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdsearch;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import sg.edu.ntu.scse.cz2006.gymbuddies.BuddySearchResultActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.MainActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;

public class BuddySearchFragment extends Fragment {
    private BuddySearchViewModel buddySearchViewModel;
    private Button btnTest;
    private Spinner spLiveRegion;
    private RadioGroup rgBuddyGender;
    private LinearLayout llPrefDays;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        buddySearchViewModel = ViewModelProviders.of(this).get(BuddySearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_buddy_search, container, false);
//        final TextView textView = root.findViewById(R.id.text_share);
        buddySearchViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });


        spLiveRegion = root.findViewById(R.id.spinner_live_region);
        rgBuddyGender = root.findViewById(R.id.rg_bd_gender);
        llPrefDays = root.findViewById(R.id.ll_pref_days);
        btnTest = root.findViewById(R.id.btn_search);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(getActivity(), BuddySearchResultActivity.class);
                Bundle data = new Bundle();
                data.putString("liveRegion", spLiveRegion.getSelectedItem().toString());
                data.putIntArray("pref_days", getPrefDays());
                data.putString("gender", getSelectedGender());
                intent.putExtras(data);
                startActivity(intent);
            }
        });

        MainActivity activity = (MainActivity)getActivity();
        activity.fab.hide();
        return root;
    }

    private String getSelectedGender(){
        switch (rgBuddyGender.getCheckedRadioButtonId()){
            case R.id.rb_female:
                return "Female";
            case R.id.rb_male:
                return "Male";
            default:
                return "Both";
        }
    }

    private int[] getPrefDays(){
        int[] arPrefDays = new int[7];
        for (int i = 0; i<7; i++){
            if (((CheckBox)llPrefDays.getChildAt(i)).isChecked()){
                arPrefDays[i] = 1;
            }
        }
        return arPrefDays;
    }
}