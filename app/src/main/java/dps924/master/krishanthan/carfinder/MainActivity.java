package dps924.master.krishanthan.carfinder;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {


    ProgressBar prgrsBar;
    boolean isFirst = true;
    private Toolbar toolbar;
    private FragmentManager fm;
    private VehicleListFragment vehcileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Force Portrait only.
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //TOOLBAR AND NAVIGATION DRAWER
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        //Start ProgressBar
        prgrsBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        prgrsBar.setVisibility(View.VISIBLE);
        isFirst = true;

        //Create VehicleList Fragment
        vehcileList = new VehicleListFragment();
        fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.container, vehcileList);
        fragmentTransaction.commit();


    }

    @Override
    protected void onResume() {
        if (!isFirst)
            resume();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //On item click, call detail activity.
    public void callDetails(ArrayList<String> makes, ArrayList<String> models, ArrayList<String> years) {

        if (isNetworkAvailable()) {
            //Create new intent
            Intent intent = new Intent(MainActivity.this, Vehicle_Detail_Activity.class);
            //Add values to intent
            intent.putStringArrayListExtra("makes", makes);
            intent.putStringArrayListExtra("models", models);
            intent.putStringArrayListExtra("years", years);
            intent.putExtra("favorites", false);
            isFirst = false;
            pause();
            //startActivity
            startActivity(intent);
        } else {
            Toast.makeText(this, " Sorry, You Need Internet Connection to View Details", Toast.LENGTH_LONG).show();
        }
    }

    //Check if network is available.
    //Returns true if it is available.
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Starts progressbar.
    public void pause() {
        prgrsBar.setVisibility(View.VISIBLE);
        LinearLayout layoutWaiting = (LinearLayout) findViewById(R.id.waiting_dialog);
        layoutWaiting.setVisibility(View.VISIBLE);
    }

    //Ends Progressbar
    public void resume() {
        prgrsBar.setVisibility(View.GONE);
        LinearLayout layoutWaiting = (LinearLayout) findViewById(R.id.waiting_dialog);
        layoutWaiting.setVisibility(View.GONE);
    }
}
