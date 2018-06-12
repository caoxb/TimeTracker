package com.example;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.adapter.TimeListAdapter;
import com.example.fragment.ConfirmClearDialogFragment;
import com.example.service.TimerService;
import com.example.utils.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TimeTrackerActivity extends AppCompatActivity implements View.OnClickListener,ServiceConnection {
    public static final String ACTION_TIME_UPDATE = "com.example.ActionTimeUpdate";
    public static final String ACTION_TIMER_FINISHED = "com.example.ActionTimerFinished";
    private static final String TAG = "TimeTrackerActivity";
    public static int TIMER_NOTIFICATION = 0;

    private TimeListAdapter mTimeListAdapter = null;
    //private long mStart = 0;
    //private long mTime = 0;
    //private TimeHandler mHandler;

    private TimerService mTimerService = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_tracker);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        // Create a handler to run the timer
        //mHandler = new TimeHandler(this);

        // Initialize the Timer
        TextView counter = (TextView) findViewById(R.id.counter);
        counter.setText(DateUtils.formatElapsedTime(0));

        Button startButton = (Button) findViewById(R.id.start_stop);
        startButton.setOnClickListener(this);

        Button stopButton = (Button) findViewById(R.id.reset);
        stopButton.setOnClickListener(this);

        List<Long> values = new ArrayList<Long>();
        if (savedInstanceState != null) {
            long[] arr = savedInstanceState.getLongArray("times");
            for (long l : arr) {
                values.add(l);
            }

            CharSequence seq = savedInstanceState.getCharSequence("currentTime");
            if (seq != null)
                counter.setText(seq);
        }

        if (mTimeListAdapter == null){
            mTimeListAdapter = new TimeListAdapter(this,0, values);
        }else {
            mTimeListAdapter.addAll(values);
        }

        ListView list = (ListView) findViewById(R.id.time_list);
        list.setAdapter(mTimeListAdapter);

        // Register the TimeReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TIME_UPDATE);
        filter.addAction(ACTION_TIMER_FINISHED);
        registerReceiver(mTimeReceiver, filter);

        if (Util.useStrictMode(this)) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mTimeListAdapter != null) {
            int count = mTimeListAdapter.getCount();
            long[] arr = new long[count];
            for (int i = 0; i<count; i++) {
                arr[i] = mTimeListAdapter.getItem(i);
            }
            outState.putLongArray("times", arr);
        }

        TextView counter = (TextView) findViewById(R.id.counter);
        if (counter != null)
            outState.putCharSequence("currentTime", counter.getText());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        // Bind to the TimerService
        bindTimerService();
    }

    @Override
    protected void onDestroy() {
        //mHandler.removeMessages(0);
        if (mTimeReceiver != null)
            unregisterReceiver(mTimeReceiver);

        if (mTimerService != null) {
            unbindService(this);
            mTimerService = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        TextView ssButton = (TextView) findViewById(R.id.start_stop);

/*        if (v.getId() == R.id.start_stop) {
            if (!isTimerRunning()) {
                startTimer();
                ssButton.setText(R.string.stop);
            } else {
                stopTimer();
                ssButton.setText(R.string.start);
            }
        } else if (v.getId() == R.id.reset) {
            resetTimer();
            TextView counter = (TextView) findViewById(R.id.counter);
            counter.setText(DateUtils.formatElapsedTime(0));
            ssButton.setText(R.string.start);
        }*/

        if (v.getId() == R.id.start_stop) {
            if (mTimerService == null) {
                ssButton.setText(R.string.stop);
                startService(new Intent(this, TimerService.class));
            } else if (!mTimerService.isTimerRunning()) {
                ssButton.setText(R.string.stop);
                mTimerService.startService(new Intent(this, TimerService.class));
            } else {
                ssButton.setText(R.string.start);
                mTimerService.stopTimer();
            }
        } else if (v.getId() == R.id.reset) {
            if (mTimerService != null) {
                mTimerService.resetTimer();
            }
            TextView counter = (TextView) findViewById(R.id.counter);
            counter.setText(DateUtils.formatElapsedTime(0));
            ssButton.setText(R.string.start);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.clear_all:
                //Testing
                FragmentManager fm = getSupportFragmentManager();
                if (fm.findFragmentByTag("dialog") == null) {
                    ConfirmClearDialogFragment frag = ConfirmClearDialogFragment.newInstance(mTimeListAdapter);
                    frag.show(fm, "dialog");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

  /*  private void startTimer() {
        mStart = System.currentTimeMillis();
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessage(0);
    }

    private void stopTimer() {
        mHandler.removeMessages(0);
    }

    private boolean isTimerRunning() {
        return mHandler.hasMessages(0);
    }

    private void resetTimer() {
        stopTimer();
        if (mTimeListAdapter != null){
            mTimeListAdapter.add(mTime/1000);
        }
        mTime = 0;
    }*/

    private void bindTimerService() {
        bindService(new Intent(this, TimerService.class), this, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            long time = intent.getLongExtra("time", 0);

            if (ACTION_TIME_UPDATE.equals(action)) {
                TextView counter = (TextView) TimeTrackerActivity.this.findViewById(R.id.counter);
                counter.setText(DateUtils.formatElapsedTime(time/1000));
            } else if (ACTION_TIMER_FINISHED.equals(action)) {
                if (mTimeListAdapter != null && time > 0)
                    mTimeListAdapter.add(time/1000);
            }
        }
    };

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.i(TAG, "onServiceConnected");
        mTimerService = ((TimerService.LocalBinder)iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.i(TAG, "onServiceDisconnected");
        mTimerService = null;
    }

/*    private static class TimeHandler extends Handler {
        WeakReference<TimeTrackerActivity> mActivityRef;

        public TimeHandler(TimeTrackerActivity activity) {
            mActivityRef = new WeakReference<TimeTrackerActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TimeTrackerActivity activity = mActivityRef.get();
            if (activity != null) {
                long current = System.currentTimeMillis();
                activity.mTime += current - activity.mStart;
                activity.mStart = current;

                TextView counter = (TextView) activity.findViewById(R.id.counter);
                counter.setText(DateUtils.formatElapsedTime(activity.mTime/1000));

                sendEmptyMessageDelayed(0, 250);
            }
        }
    }*/
}
