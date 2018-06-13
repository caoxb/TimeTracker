package com.example.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.R;
import com.example.provider.TaskProvider;

import java.util.List;

public class TimeListAdapter extends CursorAdapter {

    public TimeListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*    public TimeListAdapter(Context context, int textViewResourceId, List<Long> list) {
            super(context, textViewResourceId, list);
        }

        public TimeListAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.time_row, null);
            }

            long time = getItem(position);

            TextView name = (TextView) view.findViewById(R.id.lap_name);
            String taskString = getContext().getResources().getString(R.string.task_name);
            name.setText(String.format(taskString, position+1));

            TextView lapTime = (TextView) view.findViewById(R.id.lap_time);
            lapTime.setText(DateUtils.formatElapsedTime(time));

            return view;
        }*/

    private static class ViewHolder {
        int nameIndex;
        int timeIndex;
        TextView name;
        TextView time;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.time_row, null);
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) view.findViewById(R.id.task_name);
        holder.time = (TextView) view.findViewById(R.id.task_time);
        holder.nameIndex = cursor.getColumnIndexOrThrow(TaskProvider.Task.NAME);
        holder.timeIndex = cursor.getColumnIndexOrThrow(TaskProvider.Task.TIME);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(cursor.getString(holder.nameIndex));
        long time = cursor.getLong(holder.timeIndex);
        holder.time.setText(DateUtils.formatElapsedTime(time/1000));
    }
}
