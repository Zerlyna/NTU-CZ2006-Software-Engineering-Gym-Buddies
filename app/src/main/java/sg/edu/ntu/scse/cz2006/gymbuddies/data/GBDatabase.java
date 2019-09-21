package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CarPark.class}, version=1)
public abstract class GBDatabase extends RoomDatabase {
    private static GBDatabase instance;
    public abstract CarParkDao carParkDao();


    static String FILE_DB_NAME = "database/hdb-carpark-information.db";

    public static synchronized GBDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    GBDatabase.class,
                    "db_gym_buddies")
                    .fallbackToDestructiveMigration()
                    .createFromAsset(FILE_DB_NAME)
                    .build();
        }
        return instance;
    }
}
