package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdsearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;

public class BuddySearchFragment extends Fragment {

    private BuddySearchViewModel buddySearchViewModel;

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
        return root;
    }
}