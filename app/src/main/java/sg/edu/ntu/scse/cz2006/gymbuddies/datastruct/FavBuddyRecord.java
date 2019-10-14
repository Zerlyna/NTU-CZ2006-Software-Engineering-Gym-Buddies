package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct;


import java.util.ArrayList;
import java.util.List;


/**
 * FavBuddyRecord is data structure to hold data of user's fav buddy list
 *
 * @author Chia Yu
 * @since 2019-10-05
 */
public class FavBuddyRecord {
    private List<String> buddiesId = new ArrayList<String>();

    public FavBuddyRecord() {
    }

    public FavBuddyRecord(List<String> buddyList) {
        this.buddiesId = buddyList;
    }


    public List getBuddiesId() {
        return buddiesId;
    }

    public void setBuddiesId(List<String> buddyList) {
        this.buddiesId = buddyList;
    }
}


