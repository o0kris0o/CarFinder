package dps924.master.krishanthan.carfinder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_CATEGORIES;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_CATEGORY;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_CHIP;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_CHIP_HEX;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_CHIP_PRIMARY;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_COLORS;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_DOORS;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_DRIVENWHEEL;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_ENGINE;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_ID;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MPG;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MPGCITY;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MPGHWY;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MSRP;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_NAME;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_OPTIONS;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_PRICE;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_STYLES;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_TRANSMISSION;

/**
 * Created by Krishanthan on 4/14/2015.
 */
//Async Task to load data for viewPager
public class FetchDataTask extends AsyncTask<String, Void, JSONObject> {

    //API URL.
    private static final String URL_EDMUNDS_URL = "https://api.edmunds.com/api/vehicle/v2/";
    private static final String URL_EDMUNDS_URL_END = "/styles?view=full&fmt=json&api_key=" + MyApplication.API_KEY_EDMUNDS;

    public DataReturnDetail DRD = null;
    HashMap<Long, String[][]> mEngineDetails;
    HashMap<Long, String[][]> mTransmissionDetails;
    HashMap<Long, String[][]> mOtherVehicleDetails;
    HashMap<Long, String[][]> mCatergoryDetails;
    HashMap<Long, String[][]> mColorDetails;
    String curYear;
    String curMake;
    String curModel;
    DBAdapter dbAdapter;
    Context ctx;
    ArrayList<HashMap<Long, String[][]>> curYearData = new ArrayList<>();


    FetchDataTask(String year, String make, String model, Context baseContext) {
        super();
        dbAdapter = new DBAdapter(baseContext);
        ctx = baseContext;
        curYear = year;
        curMake = make;
        curModel = model;
    }

    //Parse Space for JSON Call. Space needs to be replaced with %20.
    public static String parseSpace(String name) {
        if (name.length() == 0) return "";
        return name.replaceAll(" ", "%20");
    }

    @Override
    protected void onPostExecute(JSONObject s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        //Gets the unique Year ID from make,model,year.
        long dbYearID = dbAdapter.getYearID(curMake, curModel, curYear);
        //Check if DB contains styles for the year.
        if (dbAdapter.StyleExistsByYearID(dbYearID)) {
            //DB Contains data. Pase the information.
            curYearData = parseDB(dbYearID);
        } else {
            //GetJson Data, and parse.
            Log.v("FetchDataTask", URL_EDMUNDS_URL + parseSpace(curMake) + "/" + parseSpace(curModel) + "/" + curYear + URL_EDMUNDS_URL_END);
            String readJSON = getJSON((URL_EDMUNDS_URL + parseSpace(curMake) + "/" + parseSpace(curModel) + "/" + curYear + URL_EDMUNDS_URL_END));
            curYearData = parseJSON(readJSON);
        }
        //Update Data in Activity
        DRD.handleReturnData(curYearData);

        return null;
    }

    //Parse DB, call dbAdapter and get the information
    public ArrayList<HashMap<Long, String[][]>> parseDB(long yearID) {
        return dbAdapter.getDBVehicleData(yearID);
    }

    //GetJson. Uses HTTPGet to grab the data from Json Feed.
    public String getJSON(String address) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failed to JSON object");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }


    //Parse JSON request.
    public ArrayList<HashMap<Long, String[][]>> parseJSON(String jsonRead) {

        try {
            Long StyleID;
            String[][] otherVehicleData;
            String[][] engineData;
            String[][] transmissionData;
            String[][] categoryData;
            String[][] colorOptionsData;


            mOtherVehicleDetails = new HashMap<>();
            mTransmissionDetails = new HashMap<>();
            mEngineDetails = new HashMap<>();
            mCatergoryDetails = new HashMap<>();
            mColorDetails = new HashMap<>();
            JSONObject jsonObject = new JSONObject(jsonRead);

            JSONArray arrayStyles = jsonObject.getJSONArray(KEY_STYLES);

            //RETRIEVES ALL DATA FROM JSON FEED.
            for (int i = 0; i < arrayStyles.length(); i++) {
                JSONObject curStyle = arrayStyles.getJSONObject(i);
                StyleID = curStyle.getLong(KEY_ID);

                //PROCESS OTHER INFORMATION FROM JSON
                otherVehicleData = new String[7][2];

                otherVehicleData[0][0] = KEY_NAME;
                otherVehicleData[0][1] = curStyle.getString(KEY_NAME);

                otherVehicleData[1][0] = KEY_DOORS;
                otherVehicleData[1][1] = curStyle.getString(KEY_DOORS);

                otherVehicleData[2][0] = KEY_DRIVENWHEEL;
                otherVehicleData[2][1] = curStyle.getString(KEY_DRIVENWHEEL);

                otherVehicleData[3][0] = KEY_PRICE;
                otherVehicleData[3][1] = "";
                if (curStyle.has(KEY_PRICE)) {
                    JSONObject curPrice = curStyle.getJSONObject(KEY_PRICE);

                    if (curPrice.has(KEY_MSRP)) {
                        otherVehicleData[3][1] = String.valueOf(curPrice.getInt(KEY_MSRP));
                    }
                }
                otherVehicleData[4][0] = KEY_MPGHWY;
                otherVehicleData[5][0] = KEY_MPGCITY;

                if (curStyle.has(KEY_MPG)) {
                    JSONObject curMPG = curStyle.getJSONObject(KEY_MPG);

                    otherVehicleData[4][1] = String.valueOf(curMPG.getString(KEY_MPGHWY));
                    otherVehicleData[5][1] = String.valueOf(curMPG.getString(KEY_MPGCITY));
                }

                otherVehicleData[6][0] = KEY_ID;
                otherVehicleData[6][1] = String.valueOf(StyleID);

                mOtherVehicleDetails.put(StyleID, otherVehicleData);

                //PROCESS Engine INFORMATION FROM JSON
                JSONObject curEngine = curStyle.getJSONObject(KEY_ENGINE);


                Iterator iter = curEngine.keys();
                engineData = new String[curEngine.length()][2];
                int iterIndex = 0;
                while (iter.hasNext()) {
                    engineData[iterIndex][0] = (String) iter.next();
                    engineData[iterIndex][1] = curEngine.getString(engineData[iterIndex][0]);
                    iterIndex++;
                }

                mEngineDetails.put(StyleID, engineData);

                //PROCESS Transmission INFORMATION FROM JSON
                JSONObject curTransmission = curStyle.getJSONObject(KEY_TRANSMISSION);

                iter = curTransmission.keys();
                transmissionData = new String[curTransmission.length()][2];
                iterIndex = 0;
                while (iter.hasNext()) {
                    transmissionData[iterIndex][0] = (String) iter.next();
                    transmissionData[iterIndex][1] = curTransmission.getString(transmissionData[iterIndex][0]);
                    iterIndex++;
                }

                mTransmissionDetails.put(StyleID, transmissionData);

                //PROCESS Category INFORMATION FROM JSON
                JSONObject curCategories = curStyle.getJSONObject(KEY_CATEGORIES);

                iter = curCategories.keys();
                categoryData = new String[curCategories.length()][2];
                iterIndex = 0;
                while (iter.hasNext()) {
                    categoryData[iterIndex][0] = (String) iter.next();
                    categoryData[iterIndex][1] = curCategories.getString(categoryData[iterIndex][0]);
                    iterIndex++;
                }

                mCatergoryDetails.put(StyleID, categoryData);

                //PROCESS Color INFORMATION FROM JSON
                JSONArray curColorArray = curStyle.getJSONArray(KEY_COLORS);
                int curInd = 0;
                int totalColors = 0;
                int vvv = curColorArray.length();

                for (int t = 0; t < vvv; t++) {
                    JSONObject curColorsObject = curColorArray.getJSONObject(t);
                    JSONArray curColorOptions = curColorsObject.getJSONArray(KEY_OPTIONS);
                    totalColors += curColorOptions.length();
                }

                totalColors = (totalColors * 2) + vvv;
                colorOptionsData = new String[totalColors][2];


                for (int x = 0; x < curColorArray.length(); x++) {

                    JSONObject curColorsObject = curColorArray.getJSONObject(x);
                    JSONArray curColorOptions = curColorsObject.getJSONArray(KEY_OPTIONS);

                    colorOptionsData[curInd][0] = KEY_CATEGORY;
                    colorOptionsData[curInd][1] = curColorsObject.getString(KEY_CATEGORY);
                    curInd++;

                    for (int z = 0; z < curColorOptions.length(); z++) {
                        JSONObject curColorOptionsObject = curColorOptions.getJSONObject(z);
                        String optionName = curColorOptionsObject.getString(KEY_NAME);

                        String hex;
                        if (curColorOptionsObject.has(KEY_CHIP)) {
                            JSONObject curColorChips = curColorOptionsObject.getJSONObject(KEY_CHIP);
                            if (curColorChips.has(KEY_CHIP_PRIMARY)) {
                                JSONObject curColorChipsPrimary = curColorChips.getJSONObject(KEY_CHIP_PRIMARY);
                                hex = curColorChipsPrimary.getString(KEY_CHIP_HEX);

                                colorOptionsData[curInd][0] = KEY_NAME;
                                colorOptionsData[curInd][1] = optionName;
                                curInd++;
                                colorOptionsData[curInd][0] = KEY_CHIP_HEX;
                                colorOptionsData[curInd][1] = hex;
                                curInd++;
                            }
                        }
                    }
                }

                mColorDetails.put(StyleID, colorOptionsData);
                //Add All data to DB.
                dbAdapter.addVehicleData(curMake, curModel, curYear, engineData, transmissionData, colorOptionsData, otherVehicleData, categoryData);
            }


            curYearData = new ArrayList<>();

            curYearData.add(mEngineDetails);
            curYearData.add(mTransmissionDetails);
            curYearData.add(mColorDetails);
            curYearData.add(mOtherVehicleDetails);
            curYearData.add(mCatergoryDetails);

        } catch (Exception e) {
            e.printStackTrace();
            Log.v("FetchDataTask", "Error parsing data [" + e.getMessage() + " " + e.getLocalizedMessage() + "] ");
        }
        return curYearData;
    }
}
