package sg.edu.ntu.scse.cz2006.gymbuddies.ui.forum;

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

import sg.edu.ntu.scse.cz2006.gymbuddies.MainActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DialogHelper;

public class ForumFragment extends Fragment {

    private ForumViewModel forumViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        forumViewModel = ViewModelProviders.of(this).get(ForumViewModel.class);
        View root = inflater.inflate(R.layout.fragment_forum, container, false);
        final TextView textView = root.findViewById(R.id.text_tools);
        forumViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        if (getActivity() != null) {
            MainActivity activity = (MainActivity) getActivity();
            //activity.fab.hide();
            activity.fab.setOnClickListener(view ->  DialogHelper.displayBuildInfo(getContext()) );
        }
        return root;
    }
}