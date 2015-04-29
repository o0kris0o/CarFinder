package dps924.master.krishanthan.carfinder;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import dps924.master.krishanthan.carfinder.network.VolleySingleton;

import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_ID;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MAKES;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MODELS;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_NAME;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_NICENAME;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_YEAR;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_YEARS;

/**
 * Created by Krishanthan on 3/15/2015.
 */
public class VehicleListFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String URL_EDMUNDS_VEHICLE_LIST = "https://api.edmunds.com/api/vehicle/v2/makes?view=full&fmt=json&api_key=" + MyApplication.API_KEY_EDMUNDS;
    Context ctx;
    ExpandableListView expList;
    VehicleAdapter adapter;
    DBAdapter dbAdapter;
    private SearchView mSearchView;
    private RequestQueue requestQueue;
    private LinkedHashMap<String, ArrayList<String>> makeData = new LinkedHashMap<>();
    private VolleySingleton volleySingleton;

    public VehicleListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.vehicle_list_fragment,
                container, false);
        ctx = rootview.getContext();
        expList = (ExpandableListView) rootview.findViewById(R.id.explist);


        dbAdapter = new DBAdapter(getActivity());

        volleySingleton = VolleySingleton.getsInstance();
        requestQueue = volleySingleton.getmRequestQueue();

        startDataPull();


        adapter = new VehicleAdapter(ctx, makeData);
        expList.setAdapter(adapter);


        // LISTENERS FOR EXPANDABLE LIST VIEW
        expList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
//                Toast.makeText(ctx, makeList.get(groupPosition).getName() + "is Expanded", Toast.LENGTH_SHORT).show();
            }
        });

        // LISTENERS FOR EXPANDABLE LIST VIEW
        expList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
//                Toast.makeText(ctx, makeList.get(groupPosition).getName() + "is Collapsed", Toast.LENGTH_SHORT).show();
            }
        });
        // LISTENERS FOR EXPANDABLE LIST VIEW
        expList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                makeData = adapter.getMakeList();

                String make = (String) adapter.getGroup(groupPosition);
                String model = (String) adapter.getChild(groupPosition, childPosition);
                ArrayList<String> years = new ArrayList<>(Arrays.asList(dbAdapter.getYears(model)));
                ArrayList<String> models = new ArrayList<>();
                ArrayList<String> makes = new ArrayList<>();

                Collections.reverse(years);
                for (int i = 0; i < years.size(); i++) {
                    models.add(model);
                    makes.add(make);
                }

                adapter.setSelected(groupPosition, childPosition);
                expList.setItemChecked(childPosition, true);

                ((MainActivity) getActivity()).callDetails(makes, models, years);

                return false;
            }
        });

        return rootview;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint("Search Vehicle...");
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setIconifiedByDefault(true);

        mSearchView.setOnQueryTextListener(this);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void expandAll() {
        int count = adapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expList.expandGroup(i);
        }
    }

    private void collapseAll() {
        int count = adapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            expList.collapseGroup(i);
        }
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filterData(s);
        if (s == null || s.isEmpty()) {
            collapseAll();
        } else
            expandAll();
        return false;
    }


    public void getDBData() {
        makeData = dbAdapter.getMakes();
        ((MainActivity) getActivity()).resume();
    }

    public void parseJSON(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.length() == 0) {
            return;
        }

        try {

            Long tempMakeID;

            //MAKE DB
            ArrayList<Long> dbMakeID = new ArrayList<>();
            ArrayList<String> dbMakeName = new ArrayList<>();
            ArrayList<String> dbMakeNiceName = new ArrayList<>();

            //MODELDB
            ArrayList<Integer> dbModelID = new ArrayList<>();
            ArrayList<String> dbModelIDJson = new ArrayList<>();
            ArrayList<String> dbModelName = new ArrayList<>();
            ArrayList<String> dbModelNiceName = new ArrayList<>();
            ArrayList<Long> dbModelsMakeID = new ArrayList<>();


            //YEARS
            ArrayList<Integer> dbYearID = new ArrayList<>();
            ArrayList<Long> dbYearIDJson = new ArrayList<>();
            ArrayList<Integer> dbYearName = new ArrayList<>();
            ArrayList<String> dbYearModelName = new ArrayList<>();
            ArrayList<String> dbYearMakeName = new ArrayList<>();


            ArrayList<String> modelsList;
            String[] yearsList;


            JSONArray arrayMakes = jsonObject.getJSONArray(KEY_MAKES);

            int uniqueModelID = 1;
            int uniqueYearID = 1;
            for (int i = 0; i < arrayMakes.length(); i++) {
                JSONObject curMake = arrayMakes.getJSONObject(i);

                tempMakeID = curMake.getLong(KEY_ID);
                dbMakeID.add(tempMakeID);
                dbMakeName.add(curMake.getString(KEY_NAME));
                dbMakeNiceName.add(curMake.getString(KEY_NICENAME));


                JSONArray arrayModels = curMake.getJSONArray(KEY_MODELS);
                modelsList = new ArrayList<>();
                for (int x = 0; x < arrayModels.length(); x++) {
                    JSONObject curModelObject = arrayModels.getJSONObject(x);

                    dbModelID.add(uniqueModelID);

                    dbModelIDJson.add(curModelObject.getString(KEY_ID));
                    dbModelNiceName.add(curModelObject.getString(KEY_NICENAME));
                    dbModelsMakeID.add(tempMakeID);
                    dbModelName.add(curModelObject.getString(KEY_NAME));


                    JSONArray arrayYears = curModelObject.getJSONArray(KEY_YEARS);
                    yearsList = new String[arrayYears.length()];
                    for (int y = 0; y < arrayYears.length(); y++) {
                        JSONObject curYearObject = arrayYears.getJSONObject(y);

                        dbYearID.add(uniqueYearID);
                        dbYearIDJson.add(curYearObject.getLong(KEY_ID));
                        dbYearName.add((int) curYearObject.getLong(KEY_YEAR));
                        dbYearModelName.add(curModelObject.getString(KEY_NAME));
                        dbYearMakeName.add(curMake.getString(KEY_NAME));
                        uniqueYearID++;
                        yearsList[y] = String.valueOf(curYearObject.getLong(KEY_YEAR));
                    }
                    uniqueModelID++;

                    modelsList.add(curModelObject.getString(KEY_NAME));
                }

                makeData.put(curMake.getString(KEY_NAME), modelsList);
            }

            dbAdapter.addMakeData(dbMakeID, dbMakeName, dbMakeNiceName);
            dbAdapter.addModelData(dbModelID, dbModelIDJson, dbModelName, dbModelNiceName, dbModelsMakeID);
            dbAdapter.addYearData(dbYearID, dbYearIDJson, dbYearName, dbYearModelName, dbYearMakeName);

            adapter.setOriginalList(makeData);
            adapter.filterData("");

        } catch (Exception e) {
            e.printStackTrace();
            Log.v("VehicleList", "Error parsing data [" + e.getMessage() + " " + e.getLocalizedMessage() + "] ");
        }
        ((MainActivity) getActivity()).resume();
    }

    private void startDataPull() {

        if (dbAdapter.isDBUpdated()) {
            getDBData();
        } else {
            dbAdapter.DropTables();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_EDMUNDS_VEHICLE_LIST, (String) null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            parseJSON(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ctx, "Sorry, There was an issue trying to get the required information. Please try again Later", Toast.LENGTH_LONG).show();
                        }
                    });

            requestQueue.add(request);
        }
    }
}



