package com.app.attendance.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.app.attendance.Model.StoreListResponse;
import com.app.attendance.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by User on 2/28/2018.
 */

public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.ViewHolder> {
    Context context;
    List<StoreListResponse.Data> storeList;
    JobClicked jobClicked;
    //categoryProductClick categoryProductClick;
    private final String TAG = "berich_"+this.getClass().getSimpleName();
    SimpleDateFormat dateFormat;
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, id;
        LinearLayout fullView;
        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.tvName);
            address = (TextView) view.findViewById(R.id.tvAddress);
            id = (TextView) view.findViewById(R.id.tvId);
            fullView = (LinearLayout) view.findViewById(R.id.FullView);
        }
    }
    public StoreListAdapter(Context context, List<StoreListResponse.Data> colors, JobClicked jobClicked ) {  //categoryProductClick categoryProductClick
        storeList = colors;
        this.context = context;
        this.jobClicked = jobClicked;
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    @Override
    public StoreListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_list, parent, false);
        StoreListAdapter.ViewHolder viewHolder = new StoreListAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StoreListAdapter.ViewHolder holder, final int position) {
            holder.name.setText("Store name: "+storeList.get(position).getName());
            holder.address.setText("Store address: "+storeList.get(position).getAddress());
            holder.id.setText("Id: "+storeList.get(position).getId());
            holder.fullView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jobClicked.jobClicked(position);
                }
            });
    }
}
