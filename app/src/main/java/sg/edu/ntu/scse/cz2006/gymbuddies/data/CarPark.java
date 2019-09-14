package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hdbcarparks")
public class CarPark {
    public CarPark(String cpid, String address, String type){
        this.cpid = cpid;
        this.address = address;
        this.type = type;
    }

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "car_park_no")
    public String cpid;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "car_park_type")
    public String type;




}
