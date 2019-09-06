package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BuddyListViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BuddyListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is BD List");
    }

    public LiveData<String> getText() {
        return mText;
    }
}