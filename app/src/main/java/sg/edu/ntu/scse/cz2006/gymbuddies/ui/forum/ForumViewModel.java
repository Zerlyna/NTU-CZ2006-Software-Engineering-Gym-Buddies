package sg.edu.ntu.scse.cz2006.gymbuddies.ui.forum;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ForumViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ForumViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Forum fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}