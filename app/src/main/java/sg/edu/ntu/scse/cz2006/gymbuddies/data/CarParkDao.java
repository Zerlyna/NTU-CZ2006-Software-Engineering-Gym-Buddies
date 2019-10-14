package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;


/**
 * Data Accessing Object for entity 'hdbcarparks' in the Room database.
 *
 * @author Chia Yu
 * @since 2019-09-14
 */
@Dao
public interface CarParkDao {

    /**
     * getting list of all car parks in room database
     */
    @Query("SELECT * FROM hdbcarparks")
    LiveData<List<CarPark>> getAllCarParks();


}
