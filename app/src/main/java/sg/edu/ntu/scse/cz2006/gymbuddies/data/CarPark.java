package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * Data Model for entity 'hdbcarparks' in the Room database.
 * For sg.edu.ntu.scse.cz2006.gymbuddies.data in Gym Buddies!
 *
 * @author Chia Yu
 * @since 2019-09-14
 */
@Entity(tableName = "hdbcarparks")
public class CarPark {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "car_park_no")
    public String id;

    @NonNull
    @ColumnInfo(name = "address")
    public String address;

    @NonNull
    @ColumnInfo(name = "x_coord")
    public double x;

    @NonNull
    @ColumnInfo(name = "y_coord")
    public double y;

    @NonNull
    @ColumnInfo(name = "car_park_type")
    public String carParkType;

    @NonNull
    @ColumnInfo(name = "type_of_parking_system")
    public String systemType;

    @NonNull
    @ColumnInfo(name = "short_term_parking")
    public String shortTermParking;

    @NonNull
    @ColumnInfo(name = "free_parking")
    public String freeParking;

    @NonNull
    @ColumnInfo(name = "night_parking")
    public String nightParking;

    @NonNull
    @ColumnInfo(name = "car_park_decks")
    public int decks;

    @NonNull
    @ColumnInfo(name = "gantry_height")
    public double gantryHeight;

    @NonNull
    @ColumnInfo(name = "car_park_basement")
    public String basement;

    @Override
    public String toString() {
        return "CarPark{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", carParkType='" + carParkType + '\'' +
                ", systemType='" + systemType + '\'' +
                ", shortTermParking='" + shortTermParking + '\'' +
                ", freeParking='" + freeParking + '\'' +
                ", nightParking='" + nightParking + '\'' +
                ", decks=" + decks +
                ", gantryHeight=" + gantryHeight +
                ", basement='" + basement + '\'' +
                '}';
    }
}
