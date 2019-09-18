package sg.edu.ntu.scse.cz2006.gymbuddies.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarPark;
import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarParkDao;
import sg.edu.ntu.scse.cz2006.gymbuddies.data.GBDatabase;

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private LiveData<List<CarPark>> carParks;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        Log.d("Cy.GymBuddies.HomeVM", "Init start");
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");



        // TODO: move to gym details view model
        try{
            GBDatabase db = GBDatabase.getInstance(application);
            CarParkDao dao = db.carParkDao();
            carParks = dao.getAllCarParks();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<CarPark>> getCarParks(){return carParks; }
}