package com.example.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.R;
import com.example.adapter.TimeListAdapter;
import com.example.provider.TaskProvider;

public class ConfirmClearDialogFragment extends DialogFragment {

    //private TimeListAdapter mAdapter;

/*    public static ConfirmClearDialogFragment newInstance(TimeListAdapter adapter) {
        ConfirmClearDialogFragment frag = new ConfirmClearDialogFragment();
        frag.mAdapter = adapter;
        return frag;
    }*/
    public static ConfirmClearDialogFragment newInstance() {
        ConfirmClearDialogFragment frag = new ConfirmClearDialogFragment();
        return frag;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_clear_all_title)
                .setMessage(R.string.confirm_clear_all_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //mAdapter.clear();

                        // Delete all existint tasks from the DB
                        AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {};
                        handler.startDelete(0, null, TaskProvider.getContentUri(), null, null);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
