package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdsearch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BuddySearchViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BuddySearchViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is BD search fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}