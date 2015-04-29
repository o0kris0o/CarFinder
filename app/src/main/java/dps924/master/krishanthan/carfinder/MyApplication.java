package dps924.master.krishanthan.carfinder;

import android.app.Application;
import android.content.Context;

/**
 * Created by Krishanthan on 3/31/2015.
 */
//USED TO get application context
public class MyApplication extends Application {

    public static final String API_KEY_EDMUNDS = "r9np3quhapgetwpz3rjqm8dy";
    private static MyApplication sInstance;

    public static MyApplication getsInstance() {
        return sInstance;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

}
