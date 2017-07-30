package com.markandreydelacruz.puptcwois.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.markandreydelacruz.puptcwois.Models.LocationReportsListModel;
import com.markandreydelacruz.puptcwois.R;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by mark on 7/24/2017.
 */

public class LocationReportsListAdapter extends ArrayAdapter {
    private List<LocationReportsListModel> reportsListModelList;
    private int resource;
    private LayoutInflater inflater;
    public LocationReportsListAdapter(Context context, int resource, List<LocationReportsListModel> objects) {
        super(context, resource, objects);
        reportsListModelList = objects;
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LocationReportsListAdapter.ViewHolder holder = null;

        if(convertView == null){
            holder = new LocationReportsListAdapter.ViewHolder();
            convertView = inflater.inflate(resource, null);
            holder.textViewID = (TextView)convertView.findViewById(R.id.textViewID);
            holder.textViewDate = (TextView)convertView.findViewById(R.id.textViewDate);
            holder.textViewTime = (TextView)convertView.findViewById(R.id.textViewTime);
            convertView.setTag(holder);
        } else {
            holder = (LocationReportsListAdapter.ViewHolder) convertView.getTag();
        }
        holder.textViewID.setText(reportsListModelList.get(position).getReport_id());
        holder.textViewDate.setText(reportsListModelList.get(position).getDate());
        holder.textViewTime.setText(reportsListModelList.get(position).getTime());

        return convertView;
    }


    class ViewHolder{
        private TextView textViewID;
        private TextView textViewDate;
        private TextView textViewTime;
    }
}
