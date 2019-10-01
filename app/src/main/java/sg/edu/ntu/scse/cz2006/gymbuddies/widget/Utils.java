package sg.edu.ntu.scse.cz2006.gymbuddies.widget;

/**
 * Created by Kenneth on 1/10/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.widget in Gym Buddies!
 */
public class Utils {
    public static double mapValueFromRangeToRange(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
        return toLow + ((value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow));
    }

    public static double clamp(double value, double low, double high) {
        return Math.min(Math.max(value, low), high);
    }
}
