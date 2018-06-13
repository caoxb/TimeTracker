package com.example.fragment;


import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.R;
import com.example.TimeTrackerActivity;
import com.example.adapter.TimeListAdapter;
import com.example.provider.TaskProvider;

public class TaskListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    TimeListAdapter mAdapter;
    TaskListener mListener;

    public static interface TaskListener {
        public void onTaskSelected(long id, String name, String desc, long date, long time);
    }
    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // indicate this fragment adds a menu option
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = (TaskListener) getActivity();
        mAdapter = new TimeListAdapter(getActivity(), null, 0);
        TimeTrackerActivity activity = (TimeTrackerActivity) getActivity();

        Button button = (Button) activity.findViewById(R.id.new_task);
        button.setOnClickListener(activity);

        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        String name = cursor.getString(cursor.getColumnIndexOrThrow(TaskProvider.Task.NAME));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow(TaskProvider.Task.DESCRIPTION));
        long date = cursor.getLong(cursor.getColumnIndexOrThrow(TaskProvider.Task.DATE));
        int time = cursor.getInt(cursor.getColumnIndexOrThrow(TaskProvider.Task.TIME));
        mListener.onTaskSelected(id, name, desc, date, time);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.clear_all:
                //Testing
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm.findFragmentByTag("dialog") == null) {
                    ConfirmClearDialogFragment frag = ConfirmClearDialogFragment.newInstance();
                    frag.show(fm, "dialog");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri uri = TaskProvider.getContentUri();
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
