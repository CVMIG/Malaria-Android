package com.cajama.malaria.newreport;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cajama.malaria.R;

/**
 * Created by Jasper on 12/2/13.
 */
public class CustomAdapter extends ArrayAdapter<String> {
    Context c;
    String[] s;

    public CustomAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
        c = context;
        s = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row=inflater.inflate(R.layout.row, parent, false);
        TextView label=(TextView)row.findViewById(R.id.weekofday);
        label.setText(s[position]);

        return row;
    }
}
