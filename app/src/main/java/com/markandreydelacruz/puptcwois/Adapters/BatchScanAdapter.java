package com.markandreydelacruz.puptcwois.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.markandreydelacruz.puptcwois.Models.BatchScanModel;
import com.markandreydelacruz.puptcwois.R;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by mark on 7/23/2017.
 */

public class BatchScanAdapter extends ArrayAdapter {
    private List<BatchScanModel> batchScanModelList;
    private int resource;
    private LayoutInflater inflater;
    public BatchScanAdapter(Context context, int resource, List<BatchScanModel> objects) {
        super(context, resource, objects);
        batchScanModelList = objects;
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(resource, null);
            holder.textViewCount = (TextView)convertView.findViewById(R.id.textViewCount);
            holder.textViewSerialNumber = (TextView)convertView.findViewById(R.id.textViewSerialNumber);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textViewCount.setText(batchScanModelList.get(position).getCount());
        holder.textViewSerialNumber.setText(batchScanModelList.get(position).getSerialNumber());

        return convertView;
    }


    class ViewHolder{
        private TextView textViewCount;
        private TextView textViewSerialNumber;
    }
}
