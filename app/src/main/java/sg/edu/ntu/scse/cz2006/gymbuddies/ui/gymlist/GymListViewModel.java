package sg.edu.ntu.scse.cz2006.gymbuddies.ui.gymlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;

import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList;

public class GymListViewModel extends ViewModel {

    private MutableLiveData<GymList.GymShell> selectedGym = new MutableLiveData<>(null);
    private MutableLiveData<Integer> favCount = new MutableLiveData<>(0);
    private MutableLiveData<HashMap<String, Integer>> currentUserFavList = new MutableLiveData<>(new HashMap<>());

    public LiveData<GymList.GymShell> getSelectedGym() { return selectedGym; }

    public LiveData<Integer> getFavCount() { return favCount; }

    public void setSelectedGym(@Nullable FavGymObject gym) {
        if (gym == null) {
            this.selectedGym.setValue(null);
            this.favCount.setValue(0);
        }
        else {
            this.selectedGym.setValue(gym.getGym());
            this.favCount.setValue(gym.getFavCount());
        }
    }

    public void updateFavCount(int count) {
        this.favCount.setValue(count);
    }

    public LiveData<HashMap<String, Integer>> getCurrentUserFavourites() {
        return currentUserFavList;
    }

    public void updateCurrentUserFavourites(@NonNull HashMap<String, Integer> currentFav) {
        currentUserFavList.setValue(currentFav);
    }
}