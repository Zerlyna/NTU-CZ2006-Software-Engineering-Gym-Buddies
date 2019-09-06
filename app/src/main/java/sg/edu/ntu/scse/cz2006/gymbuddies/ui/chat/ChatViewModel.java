package sg.edu.ntu.scse.cz2006.gymbuddies.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChatViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ChatViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Chat fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}