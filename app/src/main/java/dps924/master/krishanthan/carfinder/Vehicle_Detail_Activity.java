package dps924.master.krishanthan.carfinder;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import dps924.master.krishanthan.carfinder.tabs.SlidingTabLayout;


public class Vehicle_Detail_Activity extends ActionBarActivity {

    static NavigationDrawerFragment drawerFragment;
    public String test;
    public HashMap<String, ArrayList<HashMap<Long, String[][]>>> yearData = new HashMap<>();
    ArrayList<String> tabsData;
    String curYear;
    ProgressBar prgrsBar;
    PagerAdapter PA;
    boolean isFavorites = false;
    private Toolbar toolbar;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private FragmentManager fm;
    private ArrayList<String> mMake = new ArrayList<>();
    private ArrayList<String> mModel = new ArrayList<>();
    private ArrayList<String> mYears = new ArrayList<>();

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_details);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Bundle extras = getIntent().getExtras();

        tabsData = new ArrayList<>();
        if (extras != null) {
            mMake = extras.getStringArrayList("makes");
            mModel = extras.getStringArrayList("models");
            mYears = extras.getStringArrayList("years");
            isFavorites = extras.getBoolean("favorites");
        }

        if (!isFavorites)
            tabsData = extras.getStringArrayList("years");
        else
            for (int i = 0; i < mYears.size(); i++)
                tabsData.add(mYears.get(i) + " " + mMake.get(i) + " " + mModel.get(i));

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
        prgrsBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        resume();
        curYear = tabsData.get(0);

        createViewPager();
    }

    public void pause() {
        prgrsBar.setVisibility(View.VISIBLE);
        LinearLayout layoutWaiting = (LinearLayout) findViewById(R.id.waiting_dialog);
        layoutWaiting.setVisibility(View.VISIBLE);
    }

    public void resume() {
        prgrsBar.setVisibility(View.GONE);
        LinearLayout layoutWaiting = (LinearLayout) findViewById(R.id.waiting_dialog);
        layoutWaiting.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }


    public void createViewPager() {

        fm = getSupportFragmentManager();
        mPager = (ViewPager) findViewById(R.id.pager);
        PA = new MyPagerAdapter(fm);
        mPager.setAdapter(PA);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setHorizontalScrollBarEnabled(true);
        mTabs.setSelectedIndicatorColors(R.color.white);
        mTabs.setDistributeEvenly(true);
        mTabs.setViewPager(mPager);
    }


    /**
     * ********* DetailFragment *******
     */

    public static class DetailFragment extends Fragment implements AdapterView.OnItemSelectedListener {

        ArrayList<HashMap<Long, String[][]>> curYearData = new ArrayList<>();
        String[] curStyles;
        Long[] curKeys;
        Spinner spinner;
        double conversionRate = 1.23;
        View layout;
        DBAdapter dbAdapter;
        boolean isFav = false;
        String CurYear, CurMake, CurModel;
        private TextView txtPrice, txtVehicleType, txtMPGCity, txtMPGHwy, txtDriveType, txtDoors, CurrentTitle, CurrentStyle;
        private LinearLayout layoutColorLinear, tableRowEngineDetail, tableRowTransmissionDetail;
        private ImageView btnFav;
        private HashMap<Long, String[][]> dEngineDetails;
        private HashMap<Long, String[][]> dTransmissionDetails;
        private HashMap<Long, String[][]> dColorDetails;
        private HashMap<Long, String[][]> dOtherVehicleDetails;
        private HashMap<Long, String[][]> dCatergoryDetails;

        public static DetailFragment getInstance(String CurMake, String CurModel, String CurYear, ArrayList<HashMap<Long, String[][]>> data) {

            DetailFragment detailFragment = new DetailFragment();

            Bundle args = new Bundle();
            args.putSerializable("vehicleData", data);
            args.putString("curmake", CurMake);
            args.putString("curmodel", CurModel);
            args.putString("curyear", CurYear);

            detailFragment.setArguments(args);
            return detailFragment;
        }

        static String splitCamelCase(String s) {
            String a = s.replaceAll(
                    String.format("%s|%s|%s",
                            "(?<=[A-Z])(?=[A-Z][a-z])",
                            "(?<=[^A-Z])(?=[A-Z])",
                            "(?<=[A-Za-z])(?=[^A-Za-z])"
                    ),
                    " "
            );
            return a.substring(0, 1).toUpperCase() + a.substring(1);
        }

        public void favoriteClick(String make, String Model, String Year) {

            if (!isFav) {
                dbAdapter.addToFavorites(make, Model, Year);
                btnFav.setImageResource(R.mipmap.like);
                isFav = true;
                Toast.makeText(getActivity(), "" + make + " " + Model + " " + Year + " Has been added to your favorites", Toast.LENGTH_LONG).show();
            } else {
                dbAdapter.removeFavorites(make, Model, Year);
                btnFav.setImageResource(R.mipmap.likeoutline);
                isFav = false;
                Toast.makeText(getActivity(), "" + make + " " + Model + " " + Year + " Has been removed from your favorites", Toast.LENGTH_LONG).show();

            }
            drawerFragment.menuUpdated();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            dbAdapter = new DBAdapter(this.getActivity());
            layout = inflater.inflate(R.layout.detail_fragment, container, false);

            final Bundle bundle = getArguments();
            CurMake = bundle.getString("curmake");
            CurModel = bundle.getString("curmodel");
            CurYear = bundle.getString("curyear");

            btnFav = (ImageView) layout.findViewById(R.id.favBtn);
            if (dbAdapter.isFavorites(CurMake, CurModel, CurYear)) {
                btnFav.setImageResource(R.mipmap.like);
                isFav = true;
            } else {
                btnFav.setImageResource(R.mipmap.likeoutline);
                isFav = false;
            }
            btnFav.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    favoriteClick(CurMake, CurModel, CurYear);
                }
            });

            txtPrice = (TextView) layout.findViewById(R.id.vehiclePrice);
            txtDriveType = (TextView) layout.findViewById(R.id.driveType);
            txtMPGHwy = (TextView) layout.findViewById(R.id.mpgHWY);
            txtMPGCity = (TextView) layout.findViewById(R.id.mpgCity);
            txtDoors = (TextView) layout.findViewById(R.id.numDoors);
            txtVehicleType = (TextView) layout.findViewById(R.id.vehicleType);
            layoutColorLinear = (LinearLayout) layout.findViewById(R.id.ColorDetailRow);
            CurrentTitle = (TextView) layout.findViewById(R.id.CurrentTitle);
            CurrentStyle = (TextView) layout.findViewById(R.id.CurrentStyle);

            tableRowEngineDetail = (LinearLayout) layout.findViewById(R.id.engingDetailRow);
            tableRowTransmissionDetail = (LinearLayout) layout.findViewById(R.id.transmissionDetailRow);


            if (bundle != null) {
                if (curYearData != null) {
                    curYearData =
                            (ArrayList<HashMap<Long, String[][]>>) bundle.getSerializable("vehicleData");

                    dEngineDetails = curYearData.get(0);
                    dTransmissionDetails = curYearData.get(1);
                    dColorDetails = curYearData.get(2);
                    dOtherVehicleDetails = curYearData.get(3);
                    dCatergoryDetails = curYearData.get(4);

                    curStyles = new String[dOtherVehicleDetails.size()];
                    curKeys = new Long[dOtherVehicleDetails.size()];
                    int ind = 0;

                    for (Long key : dOtherVehicleDetails.keySet()) {
                        curKeys[ind] = key;
                        curStyles[ind] = String.valueOf(dOtherVehicleDetails.get(key)[0][1]);
                        ind++;
                    }
                }

                spinner = (Spinner) layout.findViewById(R.id.SubModelList);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, curStyles);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                spinner.setOnItemSelectedListener(this);


            }
            return layout;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            spinner.setSelection(position);
            setValuesToView(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

        public void setValuesToView(int position) {
            Long SelectedStyle = curKeys[position];

            String[][] temp;
            double tempDouble;
            String tempString;
            DecimalFormat df = new DecimalFormat("#,###.##");

            //Check Vehicle Doors
            if (dOtherVehicleDetails.get(SelectedStyle)[1][1] == "" || dOtherVehicleDetails.get(SelectedStyle)[1][1] == null) {
                txtDoors.setText("N/A");
            } else {
                txtDoors.setText(dOtherVehicleDetails.get(SelectedStyle)[1][1]);
            }
            layout.findViewById(R.id.vehicleDoorImage).setBackgroundResource(R.drawable.doors);

            //Check Vehicle Price
            if (dOtherVehicleDetails.get(SelectedStyle)[3][1] == "" || dOtherVehicleDetails.get(SelectedStyle)[3][1] == null) {
                txtPrice.setText("MSRP Unavailable, Sorry!");
            } else {
                tempDouble = Long.valueOf(dOtherVehicleDetails.get(SelectedStyle)[3][1]);
                txtPrice.setText("MSRP: $" + df.format(tempDouble * conversionRate) + " CAD");
            }

            //Check Vehicle Gas
            if (dOtherVehicleDetails.get(SelectedStyle)[5][1] == "" || dOtherVehicleDetails.get(SelectedStyle)[5][1] == null) {
                txtMPGCity.setText("N/A");
            } else {
                tempDouble = Long.valueOf(dOtherVehicleDetails.get(SelectedStyle)[5][1]);
                txtMPGCity.setText(df.format(282 / tempDouble) + " L");
            }
            if (dOtherVehicleDetails.get(SelectedStyle)[4][1] == "" || dOtherVehicleDetails.get(SelectedStyle)[4][1] == null) {
                txtMPGHwy.setText("N/A");
            } else {
                tempDouble = Long.valueOf(dOtherVehicleDetails.get(SelectedStyle)[4][1]);
                txtMPGHwy.setText(df.format(282 / tempDouble) + " L");
            }
            layout.findViewById(R.id.vehicleMPGCityImage).setBackgroundResource(R.drawable.gas);
            layout.findViewById(R.id.vehicleMPGHwyImage).setBackgroundResource(R.drawable.gas);
            //Check Vehicle Drive Train
            if (dCatergoryDetails.get(SelectedStyle)[2][1] != null) {
                tempString = dOtherVehicleDetails.get(SelectedStyle)[2][1];
                if (tempString.toLowerCase().contains("front"))
                    txtDriveType.setText("FWD");
                else if (tempString.toLowerCase().contains("rear"))
                    txtDriveType.setText("RWD");
                else if (tempString.toLowerCase().contains("all"))
                    txtDriveType.setText("AWD");
                else
                    txtDriveType.setText("N/A");
                layout.findViewById(R.id.vehicleDriveImage).setBackgroundResource(R.drawable.drive);
            }

            //Check Vehicle Type
            if (dCatergoryDetails.get(SelectedStyle)[0][1] != null) {
                tempString = dCatergoryDetails.get(SelectedStyle)[0][1];
                if (tempString.toLowerCase().contains("hatchback")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.hatchback);
                    txtVehicleType.setText("Hatchback");
                } else if (tempString.toLowerCase().contains("sedan")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.sedan);
                    txtVehicleType.setText("Sedan");
                } else if (tempString.toLowerCase().contains("coupe")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.coupe);
                    txtVehicleType.setText("Coupe");
                } else if (tempString.toLowerCase().contains("suv")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.suv);
                    txtVehicleType.setText("SUV");
                } else if (tempString.toLowerCase().contains("minivan")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.van);
                    txtVehicleType.setText("Minivan");
                } else if (tempString.toLowerCase().contains("truck")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.truck);
                    txtVehicleType.setText("Truck");
                } else if (tempString.toLowerCase().contains("wagon")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.wagon);
                    txtVehicleType.setText("Wagon");
                } else if (tempString.toLowerCase().contains("convertible")) {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.convertible);
                    txtVehicleType.setText("Convertible");
                } else {
                    layout.findViewById(R.id.vehicleTypeImage).setBackgroundResource(R.drawable.sedan);
                    txtVehicleType.setText("N/A");
                }
            }

            CurrentTitle.setText(CurYear + " " + CurMake + " " + CurModel);
            CurrentStyle.setText(curStyles[position]);

            //================== Display Color Information =====================

            float colorBoxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            float colorBoxMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

            if (dColorDetails.get(SelectedStyle).length == 0) {
                TableRow ColorTileRow = (TableRow) layout.findViewById(R.id.ColorTitle);
                ColorTileRow.removeAllViews();
            } else {
                LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textParam.setMargins((int) colorBoxMargin, (int) colorBoxMargin, (int) colorBoxMargin, (int) colorBoxMargin);
                textParam.height = (int) colorBoxWidth;
                textParam.width = (int) colorBoxWidth;

                layoutColorLinear.removeAllViews();
                HorizontalScrollView hsv = new HorizontalScrollView(this.getActivity());
                LinearLayout innerColorLayout = new LinearLayout(this.getActivity());
                innerColorLayout.setOrientation(LinearLayout.HORIZONTAL);

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < dColorDetails.get(SelectedStyle).length; i++) {

                    if (dColorDetails.get(SelectedStyle)[i][0] != null || dColorDetails.get(SelectedStyle)[i][1] != null) {

                        if (dColorDetails.get(SelectedStyle)[i][0] == "category") {
                            if (i != 0) {
                                hsv.addView(innerColorLayout);
                                layoutColorLinear.addView(hsv);
                            }

                            TextView txtColorTitle = new TextView(this.getActivity());
                            txtColorTitle.setText(dColorDetails.get(SelectedStyle)[i][1] + " Colors:");
                            layoutColorLinear.addView(txtColorTitle);

                            if (i != 0) {
                                hsv = new HorizontalScrollView(this.getActivity());
                                innerColorLayout = new LinearLayout(this.getActivity());
                                innerColorLayout.setOrientation(LinearLayout.HORIZONTAL);


                            }
                        }


                        if (dColorDetails.get(SelectedStyle)[i][0] == "name") {
                            if (dColorDetails.get(SelectedStyle)[i + 1][0] != "hex") {
                                sb.append("- " + dColorDetails.get(SelectedStyle)[i][1] + "\n");
                            }
                        }


                        if (dColorDetails.get(SelectedStyle)[i][0] == "hex") {
                            if (!dColorDetails.get(SelectedStyle)[i][1].equals("") || dColorDetails.get(SelectedStyle)[i][1] != null) {
                                TextView txtColorBox = new TextView(this.getActivity());
                                txtColorBox.setLayoutParams(textParam);
                                txtColorBox.setText("");
                                txtColorBox.setBackgroundColor(Color.parseColor("#" + dColorDetails.get(SelectedStyle)[i][1]));
                                innerColorLayout.addView(txtColorBox);
                            }
                        }
                    }
                }
                TextView txtName = new TextView(this.getActivity());
                txtName.setText(sb);
                layoutColorLinear.addView(txtName);

                hsv.addView(innerColorLayout);
                layoutColorLinear.addView(hsv);
            }// Display Color Information


            //================== Engine Information =====================

            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            float screenWidth = size.x;
            float screenHeight = size.y;

            tableRowEngineDetail.removeAllViews();

            colorBoxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            colorBoxMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

            for (int i = 0; i < dEngineDetails.get(SelectedStyle).length; i++) {
                if (!((dEngineDetails.get(SelectedStyle)[i][0].equals("id")) || (dEngineDetails.get(SelectedStyle)[i][0].equals("_ID")))) {

                    RelativeLayout relLayoutRow = new RelativeLayout(this.getActivity());
                    relLayoutRow.setPadding((int) colorBoxWidth, (int) colorBoxMargin, (int) colorBoxWidth, (int) colorBoxMargin);

                    if (i % 2 == 0)
                        relLayoutRow.setBackgroundColor(getResources().getColor(R.color.listRowAlternative));
                    else
                        relLayoutRow.setBackgroundColor(getResources().getColor(R.color.listRow));

                    RelativeLayout.LayoutParams rightAlignParam = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rightAlignParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                    TextView txtInfo = new TextView(this.getActivity());
                    if (dEngineDetails.get(SelectedStyle)[i][1].equals(""))
                        txtInfo.setText("N/A");
                    else
                        txtInfo.setText(dEngineDetails.get(SelectedStyle)[i][1]);

                    TextView txtTitle = new TextView(this.getActivity());
                    txtTitle.setText(splitCamelCase(dEngineDetails.get(SelectedStyle)[i][0]) + ":");
                    txtTitle.setTypeface(null, Typeface.BOLD);
                    txtTitle.setId(100 + i);

                    txtInfo.measure(0, 0);
                    txtTitle.measure(0, 0);
                    float infoWidth = txtInfo.getMeasuredWidth();
                    float titleWidth = txtTitle.getMeasuredWidth();

                    relLayoutRow.addView(txtTitle);

                    if (screenWidth - infoWidth < titleWidth)
                        rightAlignParam.addRule(RelativeLayout.BELOW, txtTitle.getId());

                    txtInfo.setLayoutParams(rightAlignParam);
                    relLayoutRow.addView(txtInfo);
                    tableRowEngineDetail.addView(relLayoutRow);
                }
            }// Display Engine Information


            // ================ Transmission Information =====================

            tableRowTransmissionDetail.removeAllViews();

            for (int i = 0; i < dTransmissionDetails.get(SelectedStyle).length; i++) {
                if (!((dTransmissionDetails.get(SelectedStyle)[i][0].equals("id")) || (dTransmissionDetails.get(SelectedStyle)[i][0].equals("_ID")))) {
                    RelativeLayout relLayoutRow = new RelativeLayout(this.getActivity());
                    relLayoutRow.setPadding((int) colorBoxWidth, (int) colorBoxMargin, (int) colorBoxWidth, (int) colorBoxMargin);

                    if (i % 2 == 0)
                        relLayoutRow.setBackgroundColor(getResources().getColor(R.color.listRowAlternative));
                    else
                        relLayoutRow.setBackgroundColor(getResources().getColor(R.color.listRow));


                    RelativeLayout.LayoutParams rightAlignParam = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rightAlignParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                    TextView txtInfo = new TextView(this.getActivity());

                    if (dTransmissionDetails.get(SelectedStyle)[i][1].equals(""))
                        txtInfo.setText("N/A");
                    else
                        txtInfo.setText(dTransmissionDetails.get(SelectedStyle)[i][1]);

                    TextView txtTitle = new TextView(this.getActivity());
                    txtTitle.setText(splitCamelCase(dTransmissionDetails.get(SelectedStyle)[i][0]) + ":");
                    txtTitle.setTypeface(null, Typeface.BOLD);
                    txtTitle.setId(100 + i);

                    txtInfo.measure(0, 0);
                    txtTitle.measure(0, 0);
                    float infoWidth = txtInfo.getMeasuredWidth();
                    float titleWidth = txtTitle.getMeasuredWidth();
                    relLayoutRow.addView(txtTitle);

                    if (screenWidth - infoWidth < titleWidth)
                        rightAlignParam.addRule(RelativeLayout.BELOW, txtTitle.getId());

                    txtInfo.setLayoutParams(rightAlignParam);
                    relLayoutRow.addView(txtInfo);
                    tableRowTransmissionDetail.addView(relLayoutRow);
                }
            }// Display Transmission Information
        }
    }//Details Fragment Class


    /**
     * ********* PAGER ADAPTER *******
     */
    public class MyPagerAdapter extends FragmentStatePagerAdapter implements DataReturnDetail {

        String curYear;
        String curMake;
        String curModel;
        boolean isDone = false;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            curYear = mYears.get(position);
            curMake = mMake.get(position);
            curModel = mModel.get(position);
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            toolbar.setTitle(mMake.get(mPager.getCurrentItem()) + " - " + mModel.get(mPager.getCurrentItem()));
            ArrayList<HashMap<Long, String[][]>> value = yearData.get(curMake + "_" + curModel + "_" + curYear);
            if (value == null) {
                FetchDataTask fdt = new FetchDataTask(curYear, curMake, curModel, getBaseContext());
                fdt.DRD = this;
                fdt.execute(curYear);

                while (!isDone) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isDone = false;
            }

            DetailFragment detailFragment = DetailFragment.getInstance(mMake.get(position), mModel.get(position), mYears.get(position), yearData.get(curMake + "_" + curModel + "_" + curYear));
            return detailFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabsData.get(position);
        }

        @Override
        public int getCount() {
            return tabsData.size();
        }

        @Override
        public void handleReturnData(ArrayList<HashMap<Long, String[][]>> list) {
            yearData.put(curMake + "_" + curModel + "_" + curYear, list);
            isDone = true;
        }
    }//Pager Adapter
}//Vehicle Detail Activity
