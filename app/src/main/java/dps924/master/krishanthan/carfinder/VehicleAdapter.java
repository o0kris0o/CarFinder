package dps924.master.krishanthan.carfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Krishanthan on 3/31/2015.
 */

public class VehicleAdapter extends BaseExpandableListAdapter {
    ArrayList<String> Makes;
    int parentSelected = -1;
    int childSelected = -1;
    private Context ctx;
    private LinkedHashMap<String, ArrayList<String>> makeData;
    private LinkedHashMap<String, ArrayList<String>> originalMakeData;

    public VehicleAdapter(Context ctx, LinkedHashMap<String, ArrayList<String>> makeList) {
        this.ctx = ctx;
        makeData = new LinkedHashMap<>(makeList);
        originalMakeData = new LinkedHashMap<>(makeList);
        Makes = new ArrayList<>(makeData.keySet());
    }

    public LinkedHashMap<String, ArrayList<String>> getMakeList() {
        return makeData;
    }

    public void setOriginalList(LinkedHashMap<String, ArrayList<String>> originalList) {
        this.originalMakeData = originalList;
    }

    public void setSelected(int parent, int child) {
        this.parentSelected = parent;
        this.childSelected = child;
    }

    @Override
    public Object getChild(int parent, int child) {
        return makeData.get(Makes.get(parent)).get(child);
    }

    @Override
    public long getChildId(int parent, int child) {
        return child;
    }

    @Override
    public View getChildView(int parent, int child, boolean lastChild, View convertview,
                             ViewGroup parentview) {


        String child_data = (String) getChild(parent, child);

        if (convertview == null) {
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertview = layoutInflater.inflate(R.layout.child_layout, null);
        }

        TextView name = (TextView) convertview.findViewById(R.id.child_txt);
        name.setText(child_data);

        if (childSelected == child && parentSelected == parent) {
            name.setBackgroundColor(ctx.getResources().getColor(R.color.item_pressed));
            name.setTextColor(ctx.getResources().getColor(R.color.white));
        } else {
            name.setBackgroundColor(ctx.getResources().getColor(R.color.white));
            name.setTextColor(ctx.getResources().getColor(R.color.item_netural));
        }

        return convertview;
    }

    @Override
    public int getChildrenCount(int arg0) {
        return makeData.get(Makes.get(arg0)).size();
    }

    @Override
    public Object getGroup(int arg0) {
        return Makes.get(arg0);
    }

    @Override
    public int getGroupCount() {
        return Makes.size();
    }

    @Override
    public long getGroupId(int arg0) {
        return arg0;
    }

    @Override
    public View getGroupView(int parent, boolean isExpanded, View convertview, ViewGroup parentview) {
        String make_title = (String) getGroup(parent);

        if (convertview == null) {
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertview = layoutInflater.inflate(R.layout.parent_layout, null);
        }

        ImageView imgListChild = (ImageView) convertview.findViewById(R.id.groupImage);
        int j = ctx.getResources().getIdentifier(parseNameToNiceName(make_title), "drawable", ctx.getPackageName());

        imgListChild.setImageResource(j);
        TextView heading = (TextView) convertview.findViewById(R.id.parent_txt);
        heading.setText(make_title);


        return convertview;
    }

    public String parseNameToNiceName(String name) {

        if (name.length() == 0) return "";

        name = name.replaceAll("-", "_").toLowerCase();
        name = name.replaceAll(" ", "_").toLowerCase();
        return name;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }


    public void filterData(String query) {
        query = query.toLowerCase();
        parentSelected = -1;
        childSelected = -1;

        makeData.clear();


        if (query.isEmpty() || query == "") {
            makeData.putAll(originalMakeData);
        } else {
            for (Map.Entry<String, ArrayList<String>> entry : originalMakeData.entrySet()) {
                String makeName = entry.getKey();
                ArrayList<String> makeList = entry.getValue();

                if (makeName.toLowerCase().contains(query)) {
                    makeData.put(makeName, makeList);
                } else {
                    ArrayList<String> newMakeList = new ArrayList<>();
                    boolean one = false;
                    for (int i = 0; i < makeList.size(); i++) {
                        if (makeList.get(i).toLowerCase().contains(query)) {
                            newMakeList.add(makeList.get(i));
                            one = true;
                        }
                    }
                    if (one)
                        makeData.put(makeName, newMakeList);
                }
            }

        }
        Makes = new ArrayList<>(makeData.keySet());
        notifyDataSetChanged();
    }
}
