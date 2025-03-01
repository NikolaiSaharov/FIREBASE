package com.example.pupukaka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class LogAdapter extends ArrayAdapter<Log> {
    private Context context;
    private List<Log> logs;

    public LogAdapter(Context context, List<Log> logs) {
        super(context, R.layout.log_item, logs);
        this.context = context;
        this.logs = logs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.log_item, parent, false);
        }

        Log log = logs.get(position);
        TextView logAction = convertView.findViewById(R.id.logAction);
        TextView logTimestamp = convertView.findViewById(R.id.logTimestamp);

        logAction.setText(log.getAction());
        logTimestamp.setText(log.getTimestamp());

        return convertView;
    }
}
