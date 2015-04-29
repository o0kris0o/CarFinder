package dps924.master.krishanthan.carfinder;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Navigational Drawer Fragment
 */
public class NavigationDrawerFragment extends Fragment implements dps924.master.krishanthan.carfinder.menuAdapter.ClickListener {

    public static final String PREF_FILE_NAME = "testpref";
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";
    public static DBAdapter dbAdapter;
    TextView btnFav;
    List<menuItem> data = new ArrayList<>();
    Context ctx;
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View containerView;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private menuAdapter menuAdapter;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    public static void saveToPreferences(Context ctx, String preferenceName, String preferanceValue) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferanceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context ctx, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    @Override
    public void onResume() {
        super.onResume();
        menuUpdated();
    }

    public List<menuItem> getData() {
        data = dbAdapter.getFavorites(this.getActivity().getApplicationContext());
        return data;
    }

    public void menuUpdated() {
        menuAdapter.setData(getData());
        menuAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = true;
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ctx = this.getActivity();

        dbAdapter = new DBAdapter(this.getActivity());
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        menuAdapter = new menuAdapter(getActivity(), getData());
        menuAdapter.setClicklistener(this);

        recyclerView.setAdapter(menuAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        btnFav = (TextView) layout.findViewById(R.id.viewAllFav);
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkAvailable()) {
                    if (data.get(0).model.equals("") && data.size() == 1) {
                        Toast.makeText(getActivity(), "Sorry, You have no favorites to view", Toast.LENGTH_SHORT).show();
                    } else {

                        ArrayList<String> years = new ArrayList<>();
                        ArrayList<String> models = new ArrayList<>();
                        ArrayList<String> makes = new ArrayList<>();
                        for (int i = 0; i < data.size(); i++) {

                            if (!(data.get(i).model.equals(""))) {
                                makes.add(data.get(i).make);
                                models.add(data.get(i).model);
                                years.add(data.get(i).year);
                            }
                        }

                        Intent intent = new Intent(ctx, Vehicle_Detail_Activity.class);

                        intent.putStringArrayListExtra("makes", makes);
                        intent.putStringArrayListExtra("models", models);
                        intent.putStringArrayListExtra("years", years);
                        intent.putExtra("favorites", true);

                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(ctx, " Sorry, You Need Internet Connection to View Details", Toast.LENGTH_LONG).show();
                }
            }
        });
        return layout;
    }

    public void setUp(int fragmentid, DrawerLayout drawerLayout, Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentid);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (!mUserLearnedDrawer) {
                    super.onDrawerOpened(drawerView);
                    mUserLearnedDrawer = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearnedDrawer + "");
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(containerView);
        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void itemClicked(View view, int position) {

        if (isNetworkAvailable()) {
            ArrayList<String> years = new ArrayList<>();
            ArrayList<String> models = new ArrayList<>();
            ArrayList<String> makes = new ArrayList<>();

            makes.add(data.get(position).make);
            models.add(data.get(position).model);
            years.add(data.get(position).year);

            Intent intent = new Intent(this.getActivity(), Vehicle_Detail_Activity.class);

            intent.putStringArrayListExtra("makes", makes);
            intent.putStringArrayListExtra("models", models);
            intent.putStringArrayListExtra("years", years);
            intent.putExtra("favorites", true);

            startActivity(intent);

        } else {
            Toast.makeText(ctx, " Sorry, You Need Internet Connection to View Details", Toast.LENGTH_LONG).show();

        }
    }


}
