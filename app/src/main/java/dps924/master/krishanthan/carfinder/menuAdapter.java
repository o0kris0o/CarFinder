package dps924.master.krishanthan.carfinder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Krishanthan on 3/30/2015.
 */

//MENU ADAPTER. Used for the favorites list (Recycler view)
public class menuAdapter extends RecyclerView.Adapter<menuAdapter.MyViewHolder> {

    static List<menuItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;
    private ClickListener clicklistener;

    public menuAdapter(Context context, List<menuItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.menu_row, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        menuItem current = data.get(i);
        viewHolder.title.setText(current.make + " " + current.model + " " + current.year);
        viewHolder.icon.setImageResource(current.iconId);
    }

    public void setData(List<menuItem> data) {
        this.data = data;
    }

    public void setClicklistener(ClickListener clicklistener) {
        this.clicklistener = clicklistener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //Interface for Click Listener
    public interface ClickListener {
        public void itemClicked(View view, int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView icon;

        public MyViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.menuText);
            icon = (ImageView) itemView.findViewById(R.id.menuIcon);
        }

        @Override
        public void onClick(View v) {
            if (clicklistener != null) {
                clicklistener.itemClicked(v, getPosition());
            }
        }
    }
}
