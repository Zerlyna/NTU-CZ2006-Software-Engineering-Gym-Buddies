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
//                    .createFromAsset(FILE_DB_NAME)
//                    .addCallback( roomCallback )
                    .build();
        }
        return instance;
    }


//    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
//
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//            new PopulateDbAsyncTask(instance).execute();
//        }
//    };
//
//    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void>{
//        private CarParkDao carParkDao;
//
//        public PopulateDbAsyncTask(GBDatabase db){
//            carParkDao = db.carParkDao();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            carParkDao.insert(new CarPark("Y1", "Test addr 1", "A"));
//            carParkDao.insert(new CarPark("Y2", "Test addr 2", "A"));
//            carParkDao.insert(new CarPark("Y3", "Test addr 3", "B"));
//            return null;
//        }
//    }
}
