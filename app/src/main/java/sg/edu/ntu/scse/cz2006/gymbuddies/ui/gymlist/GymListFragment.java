package sg.edu.ntu.scse.cz2006.gymbuddies.ui.gymlist;

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

public class GymListFragment extends Fragment {

    private GymListViewModel gymListViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        gymListViewModel =
                ViewModelProviders.of(this).get(GymListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gym_list, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        gymListViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}