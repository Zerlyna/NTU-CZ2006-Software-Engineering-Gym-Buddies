package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface CarParkDao {

    @Query("SELECT * FROM hdbcarparks")
    LiveData<List<CarPark>> getAllCarParks();


    @Insert
    void insert(CarPark cp);
}
