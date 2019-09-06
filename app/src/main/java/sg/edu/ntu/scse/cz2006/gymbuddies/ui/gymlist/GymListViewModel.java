package sg.edu.ntu.scse.cz2006.gymbuddies.ui.gymlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GymListViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GymListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is GList fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}