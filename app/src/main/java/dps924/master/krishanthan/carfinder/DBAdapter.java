package dps924.master.krishanthan.carfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_CATEGORY;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_CHIP_HEX;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_DOORS;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_DRIVENWHEEL;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_ID;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MPGCITY;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_MPGHWY;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_NAME;
import static dps924.master.krishanthan.carfinder.Keys.EndpointResult.KEY_PRICE;

/**
 * Created by Krishanthan on 4/7/2015.
 */

//DATABASE ADAPTER.
public class DBAdapter {

    DBHelper helper;


    public DBAdapter(Context context) {
        helper = new DBHelper(context);
    }

    //Creates a LinkedHashMap of the Vehicle Makes, Models to use for VehicleListFragment
    //Returns Collection of Makes and Models.
    public LinkedHashMap<String, ArrayList<String>> getMakes() {
        LinkedHashMap<String, ArrayList<String>> makeData = new LinkedHashMap<>();

        ArrayList<String> makes = new ArrayList<>();
        ArrayList<Long> makeIDs = new ArrayList<>();
        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {DBHelper.ID, DBHelper.NAME, DBHelper.NICENAME, DBHelper.TIMESTAMP};

        Cursor cursor = db.query(DBHelper.TABLEMAKES, columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            makes.add(cursor.getString(cursor.getColumnIndex(DBHelper.NAME)));
            makeIDs.add(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
        }


        for (int i = 0; i < makeIDs.size(); i++) {
            makeData.put(makes.get(i), getModels(makeIDs.get(i)));
        }
        db.close();
        return makeData;
    }

    //Creates an ArrayList of Models based on specific Make ID.
    //Returns Arraylist Models.
    public ArrayList<String> getModels(Long makeID) {

        ArrayList<String> models = new ArrayList<>();

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.MODELID, DBHelper.NAME, DBHelper.NICENAME, DBHelper.MAKEID};
        Cursor cursor = db.query(DBHelper.TABLEMODELS, columns, DBHelper.MAKEID + " = '" + makeID + "'", null, null, null, null);

        while (cursor.moveToNext()) {
            models.add(cursor.getString(cursor.getColumnIndex(DBHelper.NAME)));
        }
        return models;
    }

    //Creates and Arraylist of All Vehicle Data based on a specific unique YearID
    //Returns a Arraylist of Hashmap that contains vehicle information on a vehicle
    public ArrayList<HashMap<Long, String[][]>> getDBVehicleData(long yearID) {

        ArrayList<HashMap<Long, String[][]>> curYearData = new ArrayList<>();

        String[][] otherVehicleData;
        String[][] categoryData;
        HashMap<Long, String[][]> mEngineDetails = new HashMap<>();
        HashMap<Long, String[][]> mTransmissionDetails = new HashMap<>();
        HashMap<Long, String[][]> mOtherVehicleDetails = new HashMap<>();
        HashMap<Long, String[][]> mCatergoryDetails = new HashMap<>();
        HashMap<Long, String[][]> mColorDetails = new HashMap<>();


        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {DBHelper.ID, DBHelper.NAME, DBHelper.DOOR, DBHelper.DRIVEWHEEL, DBHelper.PRICE, DBHelper.MPGHWY, DBHelper.MPGCITY,
                DBHelper.MARKET, DBHelper.EPA, DBHelper.VEHICLESIZE, DBHelper.PRIMARYBODYTYPE, DBHelper.VEHICLESTYLE, DBHelper.VEHICLETYPE, DBHelper.ENGINEID,
                DBHelper.TRANMISSIONID, DBHelper.YEARID
        };
        Cursor cursor = db.query(DBHelper.TABLESTYLES, columns, DBHelper.YEARID + " = '" + yearID + "'", null, null, null, null);


        long curStyle;
        long engineID;
        long tranID;
        while (cursor.moveToNext()) {
            curStyle = cursor.getLong(cursor.getColumnIndex(DBHelper.ID));

            otherVehicleData = new String[7][2];
            otherVehicleData[0][0] = KEY_NAME;
            otherVehicleData[0][1] = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            otherVehicleData[1][0] = KEY_DOORS;
            otherVehicleData[1][1] = cursor.getString(cursor.getColumnIndex(DBHelper.DOOR));
            otherVehicleData[2][0] = KEY_DRIVENWHEEL;
            otherVehicleData[2][1] = cursor.getString(cursor.getColumnIndex(DBHelper.DRIVEWHEEL));
            otherVehicleData[3][0] = KEY_PRICE;
            otherVehicleData[3][1] = String.valueOf(cursor.getLong(cursor.getColumnIndex(DBHelper.PRICE)));
            otherVehicleData[4][0] = KEY_MPGHWY;
            otherVehicleData[4][1] = cursor.getString(cursor.getColumnIndex(DBHelper.MPGHWY));
            otherVehicleData[5][0] = KEY_MPGCITY;
            otherVehicleData[5][1] = cursor.getString(cursor.getColumnIndex(DBHelper.MPGCITY));
            otherVehicleData[6][0] = KEY_ID;
            otherVehicleData[6][1] = String.valueOf(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
            mOtherVehicleDetails.put(curStyle, otherVehicleData);

            categoryData = new String[6][2];

            categoryData[0][0] = DBHelper.MARKET;
            categoryData[0][1] = cursor.getString(cursor.getColumnIndex(DBHelper.MARKET));
            categoryData[1][0] = DBHelper.EPA;
            categoryData[1][1] = cursor.getString(cursor.getColumnIndex(DBHelper.EPA));
            categoryData[2][0] = DBHelper.VEHICLESIZE;
            categoryData[2][1] = cursor.getString(cursor.getColumnIndex(DBHelper.VEHICLESIZE));
            categoryData[3][0] = DBHelper.PRIMARYBODYTYPE;
            categoryData[3][1] = cursor.getString(cursor.getColumnIndex(DBHelper.PRIMARYBODYTYPE));
            categoryData[4][0] = DBHelper.VEHICLESTYLE;
            categoryData[4][1] = cursor.getString(cursor.getColumnIndex(DBHelper.VEHICLESTYLE));
            categoryData[5][0] = DBHelper.VEHICLETYPE;
            categoryData[5][1] = cursor.getString(cursor.getColumnIndex(DBHelper.VEHICLETYPE));

            mCatergoryDetails.put(curStyle, categoryData);

            engineID = cursor.getLong((cursor.getColumnIndex(DBHelper.ENGINEID)));
            mEngineDetails.put(curStyle, getEngineDetails(engineID));

            tranID = cursor.getLong((cursor.getColumnIndex(DBHelper.TRANMISSIONID)));
            mTransmissionDetails.put(curStyle, getTransmissionDetails(tranID));
            mColorDetails.put(curStyle, getColorDetails(curStyle));

        }
        curYearData.add(mEngineDetails);
        curYearData.add(mTransmissionDetails);
        curYearData.add(mColorDetails);
        curYearData.add(mOtherVehicleDetails);
        curYearData.add(mCatergoryDetails);

        db.close();
        return curYearData;
    }

    //Get Collection of Color Details for specific Style
    //Returns 2D Array of colors. Contains Hex value and name if it has.
    public String[][] getColorDetails(long styleID) {
        String[][] data;

        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor mCursor = db.rawQuery("SELECT name, type, hex, styleID FROM CarColors INNER JOIN CarStyleColors " +
                "ON CarStyleColors.colorID = CarColors._ID AND CarStyleColors.styleID = " + styleID + " " +
                "GROUP BY type " +
                "ORDER BY type DESC "
                , null);

        Cursor cursor = db.rawQuery("SELECT name, type, hex, styleID FROM CarColors INNER JOIN CarStyleColors " +
                "ON CarStyleColors.colorID = CarColors._ID AND CarStyleColors.styleID = " + styleID + " " +
                "ORDER BY type DESC "
                , null);

        int rows = (cursor.getCount() * 2) + mCursor.getCount();


        data = new String[rows][2];
        String curCat = "";
        String category;
        String name;
        String hex;

        int i = 0;
        while (cursor.moveToNext()) {

            category = cursor.getString(cursor.getColumnIndex(DBHelper.TYPE));
            name = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            hex = cursor.getString(cursor.getColumnIndex(DBHelper.HEX));


            if (curCat == "") {
                curCat = category;
                data[i][0] = KEY_CATEGORY;
                data[i][1] = category;
                i++;
            }

            if (curCat.equals(category)) {
                data[i][0] = KEY_NAME;
                data[i][1] = name;
                i++;
                data[i][0] = KEY_CHIP_HEX;
                data[i][1] = hex;
                i++;
            } else {
                data[i][0] = KEY_CATEGORY;
                data[i][1] = category;
                i++;
                data[i][0] = KEY_NAME;
                data[i][1] = name;
                i++;
                data[i][0] = KEY_CHIP_HEX;
                data[i][1] = hex;
                i++;
                curCat = category;
            }

        }

        return data;
    }

    //Get Specific Transmission Details
    //Return 2D Array of Tranmission Details. [name][value]
    public String[][] getTransmissionDetails(long tranID) {
        String[][] data = new String[1][2];

        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {DBHelper.ID, DBHelper.NAME, DBHelper.AUTOMATICTYPE, DBHelper.TRANSMISSIONTYPE, DBHelper.NUMOFSPEEDS, DBHelper.AVAILABILITY, DBHelper.EQUIPMENTTYPE
        };
        Cursor cursor = db.query(DBHelper.TABLETRANMISSION, columns, DBHelper.ID + " = '" + tranID + "'", null, null, null, null);

        while (cursor.moveToNext()) {
            data = new String[7][2];

            data[0][0] = DBHelper.ID;
            data[0][1] = String.valueOf(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
            data[1][0] = DBHelper.NAME;
            data[1][1] = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            data[2][0] = DBHelper.AUTOMATICTYPE;
            data[2][1] = cursor.getString(cursor.getColumnIndex(DBHelper.AUTOMATICTYPE));
            data[3][0] = DBHelper.TRANSMISSIONTYPE;
            data[3][1] = cursor.getString(cursor.getColumnIndex(DBHelper.TRANSMISSIONTYPE));
            data[4][0] = DBHelper.NUMOFSPEEDS;
            data[4][1] = cursor.getString(cursor.getColumnIndex(DBHelper.NUMOFSPEEDS));
            data[5][0] = DBHelper.AVAILABILITY;
            data[5][1] = cursor.getString(cursor.getColumnIndex(DBHelper.AVAILABILITY));
            data[6][0] = DBHelper.EQUIPMENTTYPE;
            data[6][1] = cursor.getString(cursor.getColumnIndex(DBHelper.EQUIPMENTTYPE));

        }

        return data;
    }

    //Get Specific Engine Details
    //Return 2D Array of Engine Details. [name][value]
    public String[][] getEngineDetails(long engineID) {
        String[][] data = new String[1][2];

        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {DBHelper.ID, DBHelper.NAME, DBHelper.COMPRESSIONRATIO, DBHelper.CYLINDERS, DBHelper.DISPLACEMENT, DBHelper.CONFIGURATION, DBHelper.FUELTYPE,
                DBHelper.TORQUE, DBHelper.TOTALVALVES, DBHelper.EQUIPMENTTYPE, DBHelper.TYPE, DBHelper.CODE, DBHelper.COMPRESSORTYPE, DBHelper.SIZE, DBHelper.HORSEPOWER
        };
        Cursor cursor = db.query(DBHelper.TABLEENGINE, columns, DBHelper.ID + " = '" + engineID + "'", null, null, null, null);


        while (cursor.moveToNext()) {
            data = new String[15][2];

            data[0][0] = DBHelper.ID;
            data[0][1] = String.valueOf(cursor.getLong(cursor.getColumnIndex(DBHelper.ID)));
            data[1][0] = DBHelper.NAME;
            data[1][1] = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            data[2][0] = DBHelper.COMPRESSIONRATIO;
            data[2][1] = cursor.getString(cursor.getColumnIndex(DBHelper.COMPRESSIONRATIO));
            data[3][0] = DBHelper.CYLINDERS;
            data[3][1] = cursor.getString(cursor.getColumnIndex(DBHelper.CYLINDERS));
            data[4][0] = DBHelper.DISPLACEMENT;
            data[4][1] = cursor.getString(cursor.getColumnIndex(DBHelper.DISPLACEMENT));
            data[5][0] = DBHelper.CONFIGURATION;
            data[5][1] = cursor.getString(cursor.getColumnIndex(DBHelper.CONFIGURATION));
            data[6][0] = DBHelper.FUELTYPE;
            data[6][1] = cursor.getString(cursor.getColumnIndex(DBHelper.FUELTYPE));
            data[7][0] = DBHelper.TORQUE;
            data[7][1] = cursor.getString(cursor.getColumnIndex(DBHelper.TORQUE));
            data[8][0] = DBHelper.TOTALVALVES;
            data[8][1] = cursor.getString(cursor.getColumnIndex(DBHelper.TOTALVALVES));
            data[9][0] = DBHelper.EQUIPMENTTYPE;
            data[9][1] = cursor.getString(cursor.getColumnIndex(DBHelper.EQUIPMENTTYPE));
            data[10][0] = DBHelper.TYPE;
            data[10][1] = cursor.getString(cursor.getColumnIndex(DBHelper.TYPE));
            data[11][0] = DBHelper.CODE;
            data[11][1] = cursor.getString(cursor.getColumnIndex(DBHelper.CODE));
            data[12][0] = DBHelper.COMPRESSORTYPE;
            data[12][1] = cursor.getString(cursor.getColumnIndex(DBHelper.COMPRESSORTYPE));
            data[13][0] = DBHelper.SIZE;
            data[13][1] = cursor.getString(cursor.getColumnIndex(DBHelper.SIZE));
            data[14][0] = DBHelper.HORSEPOWER;
            data[14][1] = String.valueOf(cursor.getLong(cursor.getColumnIndex(DBHelper.HORSEPOWER)));
        }

        return data;
    }

    //Add Make Data to DB. Bulk Inserts data at once. Faster that using content values
    public void addMakeData(ArrayList<Long> id, ArrayList<String> name, ArrayList<String> nicename) {

        SQLiteDatabase db = helper.getWritableDatabase();

        String sql = "INSERT INTO " + DBHelper.TABLEMAKES + " VALUES (?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date today = Calendar.getInstance().getTime();

        for (int i = 0; i < id.size(); i++) {
            statement.clearBindings();
            statement.bindLong(1, id.get(i));
            statement.bindString(2, name.get(i));
            statement.bindString(3, nicename.get(i));
            statement.bindString(4, format.format(today));
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    //Add Model Data to DB. Bulk Inserts data at once. Faster that using content values
    public void addModelData(ArrayList<Integer> id, ArrayList<String> modelID, ArrayList<String> name, ArrayList<String> nicename, ArrayList<Long> makeID) {

        SQLiteDatabase db = helper.getWritableDatabase();

        String sql = "INSERT INTO " + DBHelper.TABLEMODELS + " VALUES (?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();

        for (int i = 0; i < id.size(); i++) {
            statement.clearBindings();
            statement.bindLong(1, id.get(i));
            statement.bindString(2, modelID.get(i));
            statement.bindString(3, name.get(i));
            statement.bindString(4, nicename.get(i));
            statement.bindLong(5, makeID.get(i));
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    //Add Year Data to DB. Bulk Inserts data at once. Faster that using content values
    public void addYearData(ArrayList<Integer> id, ArrayList<Long> yearId, ArrayList<Integer> name, ArrayList<String> modelNames, ArrayList<String> makeName) {

        SQLiteDatabase db = helper.getWritableDatabase();

        String sql = "INSERT INTO " + DBHelper.TABLEYEARS + " VALUES (?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();

        for (int i = 0; i < id.size(); i++) {
            statement.clearBindings();
            statement.bindLong(1, id.get(i));
            statement.bindLong(2, yearId.get(i));
            statement.bindLong(3, name.get(i));
            statement.bindString(4, modelNames.get(i));
            statement.bindString(5, makeName.get(i));
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    //Adds Vehicle Data for specific Unique Year ID.
    public void addVehicleData(String curMake, String curModel, String curYear, String[][] mEngineDetails,
                               String[][] mTransmissionDetails, String[][] mColorDetails,
                               String[][] mOtherVehicleDetails, String[][] mCatergoryDetails) {

        long yearID = getYearID(curMake, curModel, curYear);
        long StyleID;
        long EngineID;
        long TransmissionID;


        if (yearID == -1) {
            Log.v("DBAdapter", "addVehicleData yearID: " + yearID);
            return;
        } else {
            EngineID = addEngineData(mEngineDetails);
            TransmissionID = addTransmissionData(mTransmissionDetails);
            StyleID = addStylesData(mOtherVehicleDetails, yearID, EngineID, TransmissionID, mCatergoryDetails);
            addColorData(mColorDetails, StyleID);
        }
    }

    //Adds color data for specific vehicle styles
    public void addColorData(String[][] mColorDetails, long styleID) {

        String cCategory = "";
        String cHex;
        String cName;
        Long cColorID;

        for (int j = 0; j < mColorDetails.length; j++) {
            if (mColorDetails[j][0] != null || mColorDetails[j][1] != null) {
                if (mColorDetails[j][0].equals(KEY_CATEGORY)) {
                    cCategory = mColorDetails[j][1];
                } else if (mColorDetails[j][0].equals(KEY_NAME)) {
                    cName = mColorDetails[j][1];

                    if (mColorDetails[j + 1][0].equals(KEY_CHIP_HEX)) {
                        cHex = mColorDetails[j + 1][1];
                        j = j + 1;
                    } else {
                        cHex = "";
                    }
                    cColorID = addColor(cCategory, cName, cHex);
                    addColorStylesData(cColorID, styleID);
                }
            }
        }
    }

    //Adds the Many to Many relationship to DB, for Color and Style
    public void addColorStylesData(Long colorID, Long styleID) {

        if (!colorLinkExists(colorID, styleID)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.COLORID, colorID);
            contentValues.put(DBHelper.STYLEID, styleID);
            db.insert(DBHelper.TABLESTYLECOLORS, null, contentValues);
            db.close();
        }
    }

    //Check if data in DB exists. (ColorStyle Table)
    //Returns false if data not in DB, else true;
    private boolean colorLinkExists(Long colorID, Long styleID) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.COLORID, DBHelper.STYLEID};
        Cursor cursor = db.query(DBHelper.TABLESTYLECOLORS, columns, DBHelper.COLORID + "=? and " + DBHelper.STYLEID + "=? ", new String[]{String.valueOf(colorID), String.valueOf(styleID)}, null, null, null);

        if (cursor.getCount() > 0)
            return true;
        else
            return false;

    }

    //Add Vehicle to Favorites
    public void addToFavorites(String Make, String Model, String Year) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.MAKENAME, Make);
        contentValues.put(DBHelper.MODELNAME, Model);
        contentValues.put(DBHelper.YEARID, Year);
        db.insert(DBHelper.TABLEFAVORITES, null, contentValues);
        db.close();
    }

    //Check if vehicle is a favorite
    //return true if data is favorites
    public boolean isFavorites(String Make, String Model, String Year) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.MAKENAME, DBHelper.MODELNAME, DBHelper.YEARID};
        Cursor cursor = db.query(DBHelper.TABLEFAVORITES, columns, DBHelper.MAKENAME + "=? and " + DBHelper.MODELNAME + "=? and " + DBHelper.YEARID + "=? ", new String[]{Make, Model, Year}, null, null, null);

        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    //Remove vehicle from favorites
    public long removeFavorites(String Make, String Model, String Year) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.MAKENAME, DBHelper.MODELNAME, DBHelper.YEARID};
        int cursor = db.delete(DBHelper.TABLEFAVORITES, DBHelper.MAKENAME + "=? and " + DBHelper.MODELNAME + "=? and " + DBHelper.YEARID + "=? ", new String[]{Make, Model, Year});
        db.close();
        return cursor;
    }


    //Get list of vehicles for favorites. (used in recyler view)
    //Returns List of Favorites
    public List<menuItem> getFavorites(Context applicationContext) {

        List<menuItem> data = new ArrayList<>();

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.MAKENAME, DBHelper.MODELNAME, DBHelper.YEARID};
        Cursor cursor = db.query(DBHelper.TABLEFAVORITES, columns, null, null, null, null, null);

        if (cursor.getCount() <= 0) {
            menuItem current = new menuItem();
            current.iconId = R.mipmap.sad;
            current.make = "You Have No Favorites";
            current.model = "";
            current.year = "";
            data.add(current);
        }

        while (cursor.moveToNext()) {

            int j = applicationContext.getResources().getIdentifier(parseNameToNiceName(cursor.getString(cursor.getColumnIndex(DBHelper.MAKENAME))),
                    "drawable", applicationContext.getPackageName());
            menuItem current = new menuItem();
            current.iconId = j;
            current.make = cursor.getString(cursor.getColumnIndex(DBHelper.MAKENAME));
            current.model = cursor.getString(cursor.getColumnIndex(DBHelper.MODELNAME));
            current.year = cursor.getString(cursor.getColumnIndex(DBHelper.YEARID));
            data.add(current);
        }
        return data;
    }

    //Parses name. Used for drawable images, which can have only lowercase and underscores.
    //Retuns parsed name.
    public String parseNameToNiceName(String name) {

        if (name.length() == 0) return "";
        name = name.replaceAll("-", "_").toLowerCase();
        name = name.replaceAll(" ", "_").toLowerCase();
        return name;
    }

    //Add a color to db.
    //returns newly added color ID
    public long addColor(String Category, String Name, String Hex) {
        long result;

        result = colorExist(Name, Category, Hex);
        if (result == -2) {
            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.NAME, Name);
            contentValues.put(DBHelper.TYPE, Category);
            contentValues.put(DBHelper.HEX, Hex);

            result = db.insert(DBHelper.TABLECOLORS, null, contentValues);
            db.close();
        }
        return result;
    }

    //Check if color exists
    //returns -2 if not, else returns color ID
    private long colorExist(String name, String category, String hex) {

        long id = -2;
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.NAME, DBHelper.TYPE, DBHelper.HEX};
        Cursor cursor = db.query(DBHelper.TABLECOLORS, columns, DBHelper.NAME + "=? and " + DBHelper.TYPE + "=? and " + DBHelper.HEX + "=? ", new String[]{name, category, hex}, null, null, null);

        while (cursor.moveToNext()) {
            id = (int) cursor.getLong(cursor.getColumnIndex(DBHelper.ID));
        }

        return id;
    }

    //Add data to Styles DB
    public long addStylesData(String[][] mOtherVehicleDetails, long yearID, long engineID, long transmissionID, String[][] mCatergoryDetails) {

        long sID = -1;
        String sDoor = "";
        String sName = "";
        String sDriveWheel = "";
        String sPrice = "";
        String sMPGHwy = "";
        String sMPGCity = "";
        String sMarket = "";
        String sEPA = "";
        String sVehicleSize = "";
        String sPriamryBodyType = "";
        String sVehicleStyle = "";
        String sVehicleType = "";


        for (int j = 0; j < mOtherVehicleDetails.length; j++) {

            if (mOtherVehicleDetails[j][0].equals(KEY_ID))
                sID = Long.parseLong(mOtherVehicleDetails[j][1]);
            else if (mOtherVehicleDetails[j][0].equals(KEY_NAME))
                sName = mOtherVehicleDetails[j][1];
            else if (mOtherVehicleDetails[j][0].equals(KEY_DOORS))
                sDoor = mOtherVehicleDetails[j][1];
            else if (mOtherVehicleDetails[j][0].equals(KEY_DRIVENWHEEL))
                sDriveWheel = mOtherVehicleDetails[j][1];
            else if (mOtherVehicleDetails[j][0].equals(KEY_PRICE))
                sPrice = mOtherVehicleDetails[j][1];
            else if (mOtherVehicleDetails[j][0].equals(KEY_MPGCITY))
                sMPGHwy = mOtherVehicleDetails[j][1];
            else if (mOtherVehicleDetails[j][0].equals(KEY_MPGHWY))
                sMPGCity = mOtherVehicleDetails[j][1];
            else {
                Log.v("DBAdapter", "DATA MISSING Styles mOtherVehicleDetails id: " + sID + " key: " + mOtherVehicleDetails[j][0] + " value:" + mOtherVehicleDetails[j][1]);
            }
        }

        for (int j = 0; j < mCatergoryDetails.length; j++) {

            if (mCatergoryDetails[j][0].equals("market"))
                sMarket = mCatergoryDetails[j][1];
            else if (mCatergoryDetails[j][0].equals("EPAClass"))
                sEPA = mCatergoryDetails[j][1];
            else if (mCatergoryDetails[j][0].equals("vehicleSize"))
                sVehicleSize = mCatergoryDetails[j][1];
            else if (mCatergoryDetails[j][0].equals("primaryBodyType"))
                sPriamryBodyType = mCatergoryDetails[j][1];
            else if (mCatergoryDetails[j][0].equals("vehicleStyle"))
                sVehicleStyle = mCatergoryDetails[j][1];
            else if (mCatergoryDetails[j][0].equals("vehicleType"))
                sVehicleType = mCatergoryDetails[j][1];

            else {
                Log.v("DBAdapter", "DATA MISSING Styles mCatergoryDetails  id: " + sID + " key: " + mOtherVehicleDetails[j][0] + " value:" + mOtherVehicleDetails[j][1]);
            }
        }

        if (!StyleExists(sID)) {
            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.ID, sID);
            contentValues.put(DBHelper.NAME, sName);
            contentValues.put(DBHelper.DOOR, sDoor);
            contentValues.put(DBHelper.DRIVEWHEEL, sDriveWheel);
            contentValues.put(DBHelper.PRICE, sPrice);
            contentValues.put(DBHelper.MPGHWY, sMPGHwy);
            contentValues.put(DBHelper.MPGCITY, sMPGCity);
            contentValues.put(DBHelper.MARKET, sMarket);
            contentValues.put(DBHelper.EPA, sEPA);
            contentValues.put(DBHelper.VEHICLESIZE, sVehicleSize);
            contentValues.put(DBHelper.PRIMARYBODYTYPE, sPriamryBodyType);
            contentValues.put(DBHelper.VEHICLESTYLE, sVehicleStyle);
            contentValues.put(DBHelper.VEHICLETYPE, sVehicleType);
            contentValues.put(DBHelper.ENGINEID, engineID);
            contentValues.put(DBHelper.TRANMISSIONID, transmissionID);
            contentValues.put(DBHelper.YEARID, yearID);

            Long result = db.insert(DBHelper.TABLESTYLES, null, contentValues);
            db.close();
        } else {
            Log.v("DBAdapter", "Style exisits  id: " + sID);
        }

        return sID;
    }

    //Add data to Transmission DB
    public long addTransmissionData(String[][] mTransmissionDetails) {

        long tID = -1;
        String tName = "";
        String tAutoType = "";
        String tTranType = "";
        String tNumOfSpeeds = "";
        String tAvailability = "";
        String tEquipmentType = "";


        for (int j = 0; j < mTransmissionDetails.length; j++) {

            if (mTransmissionDetails[j][0].equals(KEY_ID))
                tID = Long.parseLong(mTransmissionDetails[j][1]);
            else if (mTransmissionDetails[j][0].equals(KEY_NAME))
                tName = mTransmissionDetails[j][1];
            else if (mTransmissionDetails[j][0].equals("equipmentType"))
                tEquipmentType = mTransmissionDetails[j][1];
            else if (mTransmissionDetails[j][0].equals("automaticType"))
                tAutoType = mTransmissionDetails[j][1];
            else if (mTransmissionDetails[j][0].equals("transmissionType"))
                tTranType = mTransmissionDetails[j][1];
            else if (mTransmissionDetails[j][0].equals("numberOfSpeeds"))
                tNumOfSpeeds = mTransmissionDetails[j][1];
            else if (mTransmissionDetails[j][0].equals("availablility"))
                tAvailability = mTransmissionDetails[j][1];
            else {
                Log.v("DBAdapter", "DATA MISSING Transmission  id: " + tID + " key: " + mTransmissionDetails[j][0] + " value:" + mTransmissionDetails[j][1]);
            }
        }


        if (!transmissionExists(tID)) {
            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.ID, tID);
            contentValues.put(DBHelper.NAME, tName);
            contentValues.put(DBHelper.AUTOMATICTYPE, tAutoType);
            contentValues.put(DBHelper.TRANSMISSIONTYPE, tTranType);
            contentValues.put(DBHelper.NUMOFSPEEDS, tNumOfSpeeds);
            contentValues.put(DBHelper.AVAILABILITY, tAvailability);
            contentValues.put(DBHelper.EQUIPMENTTYPE, tEquipmentType);

            Long result = db.insert(DBHelper.TABLETRANMISSION, null, contentValues);
            db.close();
        } else {
            Log.v("DBAdapter", "Transmission exisits  id: " + tID);

        }
        return tID;

    }

    //Check if Style Exists already by StyleID
    //Returns true if it does
    public boolean StyleExists(Long id) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID};
        Cursor cursor = db.query(DBHelper.TABLESTYLES, columns, DBHelper.ID + " = '" + id + "'", null, null, null, null);

        if (cursor.getCount() > 0)
            return true;
        else
            return false;

    }

    //Check if Style Exists already by YearID
    //Returns true if it does
    public boolean StyleExistsByYearID(Long id) {

        Log.v("DBAdapter", "Style exisits  id: " + id);

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.YEARID};
        Cursor cursor = db.query(DBHelper.TABLESTYLES, columns, DBHelper.YEARID + " = " + id + "", null, null, null, null);

        while (cursor.moveToNext()) {
            Log.v("DBAdapter", "cursor  : " + cursor.getString(cursor.getColumnIndex(DBHelper.YEARID)));
        }

        if (cursor.getCount() > 0) {
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    //Check if Style Transmission already by ID
    //Returns true if it does
    public boolean transmissionExists(Long id) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.NAME};
        Cursor cursor = db.query(DBHelper.TABLETRANMISSION, columns, DBHelper.ID + " = '" + id + "'", null, null, null, null);

        if (cursor.getCount() > 0)
            return true;
        else
            return false;

    }

    //Check if Style Engine already by ID
    //Returns true if it does
    public boolean engineExists(Long id) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.NAME};
        Cursor cursor = db.query(DBHelper.TABLEENGINE, columns, DBHelper.ID + " = '" + id + "'", null, null, null, null);

        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    //Add Engine Data to DB
    public long addEngineData(String[][] mEngineDetails) {
        long eID = -1;
        String eName = "";
        String eCompressionRatio = "";
        String eCylinders = "";
        String eDisplacement = "";
        String eConfiguration = "";
        String eFuelType = "";
        String eTorque = "";
        String eTotalValve = "";
        String eType = "";
        String eCode = "";
        String eCompressorType = "";
        String eSize = "";
        String eHorsePower = "";
        String eEquipmentType = "";


        for (int j = 0; j < mEngineDetails.length; j++) {

            if (mEngineDetails[j][0].equals(KEY_ID))
                eID = Long.parseLong(mEngineDetails[j][1]);
            else if (mEngineDetails[j][0].equals(KEY_NAME))
                eName = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("compressionRatio"))
                eCompressionRatio = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("cylinder"))
                eCylinders = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("size"))
                eSize = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("displacement"))
                eDisplacement = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("configuration"))
                eConfiguration = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("fuelType"))
                eFuelType = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("horsepower"))
                eHorsePower = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("torque"))
                eTorque = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("totalValves"))
                eTotalValve = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("type"))
                eType = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("code"))
                eCode = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("compressorType"))
                eCompressorType = mEngineDetails[j][1];
            else if (mEngineDetails[j][0].equals("equipmentType"))
                eEquipmentType = mEngineDetails[j][1];

            else {
                Log.v("DBAdapter", "DATA MISSING Engine  id: " + eID + " key: " + mEngineDetails[j][0] + " value:" + mEngineDetails[j][1]);
            }
        }


        if (!engineExists(eID)) {
            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.ID, eID);
            contentValues.put(DBHelper.NAME, eName);
            contentValues.put(DBHelper.COMPRESSIONRATIO, eCompressionRatio);
            contentValues.put(DBHelper.CYLINDERS, eCylinders);
            contentValues.put(DBHelper.DISPLACEMENT, eDisplacement);
            contentValues.put(DBHelper.CONFIGURATION, eConfiguration);
            contentValues.put(DBHelper.FUELTYPE, eFuelType);
            contentValues.put(DBHelper.TORQUE, eTorque);
            contentValues.put(DBHelper.TOTALVALVES, eTotalValve);
            contentValues.put(DBHelper.EQUIPMENTTYPE, eEquipmentType);
            contentValues.put(DBHelper.TYPE, eType);
            contentValues.put(DBHelper.CODE, eCode);
            contentValues.put(DBHelper.COMPRESSORTYPE, eCompressorType);
            contentValues.put(DBHelper.SIZE, eSize);
            contentValues.put(DBHelper.HORSEPOWER, eHorsePower);

            Long result = db.insert(DBHelper.TABLEENGINE, null, contentValues);
            db.close();
        } else {
            Log.v("DBAdapter", "Engine exisits  id: " + eID);
        }
        return eID;
    }


    //Gets Year ID from make,model,and year.
    //Returns yearID
    public long getYearID(String curMake, String curModel, String curYear) {
        long id = -1;

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.YEARID, DBHelper.NAME, DBHelper.MODELNAME, DBHelper.MAKENAME};
        Cursor cursor = db.query(DBHelper.TABLEYEARS, columns, DBHelper.MAKENAME + "=? and " + DBHelper.MODELNAME + "=? and " + DBHelper.NAME + "=? ", new String[]{curMake, curModel, curYear}, null, null, null);

        while (cursor.moveToNext()) {
            id = (int) cursor.getLong(cursor.getColumnIndex(DBHelper.ID));
        }

        return id;
    }

    //Gets all years for specific model
    //Returns String array of all the years
    public String[] getYears(String modelName) {
        String[] years;

        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {DBHelper.ID, DBHelper.YEARID, DBHelper.NAME, DBHelper.MODELNAME, DBHelper.MAKENAME};
        Cursor cursor = db.query(DBHelper.TABLEYEARS, columns, DBHelper.MODELNAME + " = '" + modelName + "'", null, null, null, null);
        years = new String[cursor.getCount()];

        int i = 0;
        while (cursor.moveToNext()) {
            years[i] = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            i++;

        }

        return years;
    }

    //Check if DB is up to date.
    //Returns true if it does
    public Boolean isDBUpdated() {

        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {helper.ID, helper.NAME, helper.NICENAME, helper.TIMESTAMP};

        String QueryDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date today = new Date();
        Date d2 = null;
        long diffDays = 5;

        Cursor cursor = db.query(helper.TABLEMAKES, columns, null, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.moveToFirst()) {
            QueryDate = cursor.getString(3);

            try {
                d2 = format.parse(QueryDate);
                long diff = d2.getTime() - today.getTime();
                diffDays = diff / (24 * 60 * 60 * 1000);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (diffDays < -7 || diffDays > 0) {
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }

    }

    //Initiates drop tables. Used for when db not up to date
    public void DropTables() {
        Log.v("DBAdapter", "DropTables Initiated");
        SQLiteDatabase db = helper.getWritableDatabase();

        helper.onUpgrade(db, db.getVersion(), db.getVersion() + 1);

    }


    //Database Helper Class
    static class DBHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        //DATABASE CONSTANTS

        //TABLE NAMES
        private static final String DATABASE_NAME = "CarFinderDB";
        private static final String TABLEMAKES = "CarMakes";
        private static final String TABLEMODELS = "CarModels";
        private static final String TABLEYEARS = "CarYears";
        private static final String TABLESTYLES = "CarStyles";
        private static final String TABLECOLORS = "CarColors";
        private static final String TABLESTYLECOLORS = "CarStyleColors";
        private static final String TABLEENGINE = "CarEngine";
        private static final String TABLETRANMISSION = "CarTransmission";
        private static final String TABLEFAVORITES = "Favorites";

        //DROP COMMANDS
        private static final String DROP_MAKE_TABLE = "DROP TABLE IF EXISTS " + TABLEMAKES + ";";
        private static final String DROP_MODEL_TABLE = "DROP TABLE IF EXISTS " + TABLEMODELS + ";";
        private static final String DROP_YEARS_TABLE = "DROP TABLE IF EXISTS " + TABLEYEARS + ";";
        private static final String DROP_STYLES_TABLE = "DROP TABLE IF EXISTS " + TABLESTYLES + ";";
        private static final String DROP_COLORS_TABLE = "DROP TABLE IF EXISTS " + TABLECOLORS + ";";
        private static final String DROP_STYLECOLORS_TABLE = "DROP TABLE IF EXISTS " + TABLESTYLECOLORS + ";";
        private static final String DROP_ENGINE_TABLE = "DROP TABLE IF EXISTS " + TABLEENGINE + ";";
        private static final String DROP_TRANMISSION_TABLE = "DROP TABLE IF EXISTS " + TABLETRANMISSION + ";";
        private static final String DROP_FAVORITES_TABLE = "DROP TABLE IF EXISTS " + TABLEFAVORITES + ";";

        //TABLE COLUMN NAMES
        private static final String ID = "_ID";
        private static final String NAME = "name";
        private static final String NICENAME = "niceName";
        private static final String TIMESTAMP = "timestamp";
        private static final String MAKEID = "makeID";
        private static final String YEARID = "yearID";
        private static final String MODELID = "modelID";
        private static final String MODELNAME = "modelName";
        private static final String MAKENAME = "makeName";
        private static final String COLORID = "colorID";
        private static final String MANCOLORNAME = "manufactureOptionName";
        private static final String HEX = "hex";
        private static final String TYPE = "type";
        private static final String STYLEID = "styleID";
        private static final String DOOR = "doors";
        private static final String DRIVEWHEEL = "driveWheel";
        private static final String PRICE = "price";
        private static final String MPGHWY = "hwyMPG";
        private static final String MPGCITY = "cityMPG";
        private static final String ENGINEID = "engineID";
        private static final String TRANMISSIONID = "transmissionID";
        private static final String MARKET = "market";
        private static final String EPA = "EPAClass";
        private static final String VEHICLESIZE = "vehicleSize";
        private static final String PRIMARYBODYTYPE = "primaryBodyType";
        private static final String VEHICLESTYLE = "vehicleStyle";
        private static final String VEHICLETYPE = "vehicleType";
        private static final String AUTOMATICTYPE = "automaticType";
        private static final String TRANSMISSIONTYPE = "transmissionType";
        private static final String NUMOFSPEEDS = "numOfSpeed";
        private static final String AVAILABILITY = "availability";
        private static final String EQUIPMENTTYPE = "equipmentType";
        private static final String COMPRESSIONRATIO = "compressionRatio";
        private static final String CYLINDERS = "cylinders";
        private static final String DISPLACEMENT = "displacement";
        private static final String CONFIGURATION = "configuration";
        private static final String FUELTYPE = "fuelType";
        private static final String TORQUE = "torque";
        private static final String TOTALVALVES = "totalValves";
        private static final String CODE = "Code";
        private static final String COMPRESSORTYPE = "compressorType";
        private static final String SIZE = "size";
        private static final String HORSEPOWER = "horsePower";


        //CREATE TABLES
        private static final String CREATE_MAKE_TABLE = "CREATE TABLE " + TABLEMAKES
                + " (" + ID + " LONG PRIMARY KEY , "
                + NAME + " VARCHAR(50),"
                + NICENAME + " VARCHAR(50), "
                + TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL"
                + ");";

        private static final String CREATE_MODEL_TABLE = "CREATE TABLE " + TABLEMODELS
                + " (" + ID + " Int PRIMARY KEY , "
                + MODELID + " VARCHAR(50)," + NAME + " VARCHAR(50),"
                + NICENAME + " VARCHAR(50), " + MAKEID + " LONG"
                + ");";

        private static final String CREATE_YEAR_TABLE = "CREATE TABLE " + TABLEYEARS
                + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + YEARID + " LONG,"
                + NAME + " int,"
                + MODELNAME + " VARCHAR(50),"
                + MAKENAME + " VARCHAR(50)"
                + ");";

        private static final String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLEFAVORITES
                + " (" + ID + " LONG PRIMARY KEY , "
                + MAKENAME + " VARCHAR(50),"
                + MODELNAME + " VARCHAR(50),"
                + YEARID + " VARCHAR(50)"
                + ");";

        private static final String CREATE_COLOR_TABLE = "CREATE TABLE " + TABLECOLORS
                + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME + " VARCHAR(100),"
                + TYPE + " VARCHAR(50),"
                + HEX + " VARCHAR(10)"
                + ");";

        private static final String CREATE_COLOR_STYLE_TABLE = "CREATE TABLE " + TABLESTYLECOLORS
                + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT  , "
                + COLORID + " LONG,"
                + STYLEID + " LONG"
                + ");";

        private static final String CREATE_STYLE_TABLE = "CREATE TABLE " + TABLESTYLES
                + " (" + ID + " LONG PRIMARY KEY , "
                + NAME + " VARCHAR(50),"
                + DOOR + " VARCHAR(5),"
                + DRIVEWHEEL + " VARCHAR(50),"
                + PRICE + " LONG,"
                + MPGHWY + " VARCHAR(5),"
                + MPGCITY + " VARCHAR(5),"
                + MARKET + " VARCHAR(50),"
                + EPA + " VARCHAR(50),"
                + VEHICLESIZE + " VARCHAR(50),"
                + PRIMARYBODYTYPE + " VARCHAR(50),"
                + VEHICLESTYLE + " VARCHAR(50),"
                + VEHICLETYPE + " VARCHAR(50),"
                + ENGINEID + " LONG,"
                + TRANMISSIONID + " LONG,"
                + YEARID + " Int"
                + ");";

        private static final String CREATE_TRANSMISSION_TABLE = "CREATE TABLE " + TABLETRANMISSION
                + " (" + ID + " LONG PRIMARY KEY , "
                + NAME + " VARCHAR(50),"
                + AUTOMATICTYPE + " VARCHAR(50),"
                + TRANSMISSIONTYPE + " VARCHAR(50),"
                + NUMOFSPEEDS + " VARCHAR(50),"
                + AVAILABILITY + " VARCHAR(50),"
                + EQUIPMENTTYPE + " VARCHAR(50)"
                + ");";

        private static final String CREATE_ENGINE_TABLE = "CREATE TABLE " + TABLEENGINE
                + " (" + ID + " LONG PRIMARY KEY , "
                + NAME + " VARCHAR(50),"
                + COMPRESSIONRATIO + " VARCHAR(50),"
                + CYLINDERS + " VARCHAR(50),"
                + DISPLACEMENT + " VARCHAR(50),"
                + CONFIGURATION + " VARCHAR(50),"
                + FUELTYPE + " VARCHAR(50),"
                + TORQUE + " VARCHAR(50),"
                + TOTALVALVES + " VARCHAR(50),"
                + EQUIPMENTTYPE + " VARCHAR(50),"
                + TYPE + " VARCHAR(50),"
                + CODE + " VARCHAR(50),"
                + COMPRESSORTYPE + " VARCHAR(50),"
                + SIZE + " VARCHAR(50),"
                + HORSEPOWER + " LONG"
                + ");";


        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        //ONCREATE, Create all tables
        @Override
        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_MAKE_TABLE);
                db.execSQL(CREATE_MODEL_TABLE);
                db.execSQL(CREATE_YEAR_TABLE);
                db.execSQL(CREATE_COLOR_STYLE_TABLE);
                db.execSQL(CREATE_COLOR_TABLE);
                db.execSQL(CREATE_ENGINE_TABLE);
                db.execSQL(CREATE_STYLE_TABLE);
                db.execSQL(CREATE_TRANSMISSION_TABLE);
                db.execSQL(CREATE_FAVORITES_TABLE);
            } catch (SQLException e) {
                Log.v("DBAdapter", "onCreate ERROR: " + e);
            }
        }

        //ONUPGRADE, DROP ALL TABLES
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v("DBAdapter", "onUpgrade initiated");
            try {
                db.execSQL(DROP_MAKE_TABLE);
                db.execSQL(DROP_MODEL_TABLE);
                db.execSQL(DROP_COLORS_TABLE);
                db.execSQL(DROP_ENGINE_TABLE);
                db.execSQL(DROP_STYLECOLORS_TABLE);
                db.execSQL(DROP_STYLES_TABLE);
                db.execSQL(DROP_TRANMISSION_TABLE);
                db.execSQL(DROP_YEARS_TABLE);
                db.execSQL(DROP_FAVORITES_TABLE);

                onCreate(db);
            } catch (SQLException e) {
                Log.v("DBAdapter", "onUpgrade ERROR: " + e);

            }
        }
    }
}

