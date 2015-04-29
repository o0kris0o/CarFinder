package dps924.master.krishanthan.carfinder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Krishanthan on 4/14/2015.
 */
//Interface Used to update view once data in ViewPager is loaded.
public interface DataReturnDetail {
    void handleReturnData(ArrayList<HashMap<Long, String[][]>> list);
}
