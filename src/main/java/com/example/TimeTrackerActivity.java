package com.example;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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
import android.widget.TabHost;
import android.widget.TextView;

import com.example.adapter.PagerAdapter;
import com.example.adapter.TimeListAdapter;
import com.example.data.ABTabListener;
import com.example.data.DummyTabFactory;
import com.example.fragment.ConfirmClearDialogFragment;
import com.example.fragment.TaskListFragment;
import com.example.fragment.TimerFragment;
import com.example.service.TimerService;
import com.example.utils.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TimeTrackerActivity extends AppCompatActivity implements View.OnClickListener,ServiceConnection,ViewPager.OnPageChangeListener, TaskListFragment.TaskListener {
    public static final String ACTION_TIME_UPDATE = "com.example.ActionTimeUpdate";
    public static final String ACTION_TIMER_FINISHED = "com.example.ActionTimerFinished";
    private static final String TAG = "TimeTrackerActivity";
    public static int TIMER_NOTIFICATION = 0;

    public static int DATE_FLAGS = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE;

    private TimeListAdapter mTimeListAdapter = null;
    //private long mStart = 0;
    //private long mTime = 0;
    //private TimeHandler mHandler;

    private TimerService mTimerService = null;
    private TabHost mTabHost;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private long mCurrentTask = -1;
    private long mCurrentTime = 0;

    //private long mDateTime = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        //init(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TIME_UPDATE);
        registerReceiver(mTimeReceiver, filter);

        if (savedInstanceState != null) {
            long id = savedInstanceState.getLong("id");
            if (id > 0)
                mCurrentTask = id;
            long time = savedInstanceState.getLong("time");
            if (time > 0)
                mCurrentTime = time;
        }

        FragmentManager fm = getSupportFragmentManager();
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(fm);
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(this);

        // add tabs. Use ActionBar for 3.0 and above, otherwise use TabWidget
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            final ActionBar bar = getSupportActionBar();
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            bar.addTab(bar.newTab()
                    .setText(R.string.timer)
                    .setTabListener(new ABTabListener(mPager)));
            bar.addTab(bar.newTab()
                    .setText(R.string.tasks)
                    .setTabListener(new ABTabListener(mPager)));
        } else {
            // Use TabWidget instead
            mTabHost = (TabHost) findViewById(android.R.id.tabhost);
            mTabHost.setup();
            mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    if ("timer".equals(tabId)) {
                        mPager.setCurrentItem(0);
                    } else if ("tasks".equals(tabId)) {
                        mPager.setCurrentItem(1);
                    }
                }
            });

            String timer = getResources().getString(R.string.timer);
            mTabHost.addTab(mTabHost.newTabSpec("timer").setIndicator(timer).setContent(new DummyTabFactory(this)));
            String tasks = getResources().getString(R.string.tasks);
            mTabHost.addTab(mTabHost.newTabSpec("tasks").setIndicator(tasks).setContent(new DummyTabFactory(this)));
        }
    }

    private void init(Bundle savedInstanceState) {
        // Create a handler to run the timer
        //mHandler = new TimeHandler(this);

        // Initialize the fields
        //mDateTime = System.currentTimeMillis();
        // Initialize the Timer
        TextView counter = (TextView) findViewById(R.id.counter);
        counter.setText(DateUtils.formatElapsedTime(0));

        Button startButton = (Button) findViewById(R.id.start_stop);
        startButton.setOnClickListener(this);

/*        Button stopButton = (Button) findViewById(R.id.reset);
        stopButton.setOnClickListener(this);*/

        Button editButton = (Button) findViewById(R.id.edit);
        editButton.setOnClickListener(this);

        TextView date = (TextView) findViewById(R.id.task_date);
        //date.setText(DateUtils.formatDateTime(this, mDateTime, DATE_FLAGS));

        TextView description = (TextView) findViewById(R.id.task_desc);
        description.setText(getResources().getString(R.string.description));

        if (savedInstanceState != null) {
            CharSequence seq = savedInstanceState.getCharSequence("currentTime");
            if (seq != null)
                counter.setText(seq);

            //mDateTime = savedInstanceState.getLong("dateTime", System.currentTimeMillis());
        }

        // Register the TimeReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TIME_UPDATE);
        registerReceiver(mTimeReceiver, filter);

/*        List<Long> values = new ArrayList<Long>();
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
*/
/*        if (Util.useStrictMode(this)) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
        }*/
    }

/*    @Override
    protected void onSaveInstanceState(Bundle outState) {
*//*        if (mTimeListAdapter != null) {
            int count = mTimeListAdapter.getCount();
            long[] arr = new long[count];
            for (int i = 0; i<count; i++) {
                arr[i] = mTimeListAdapter.getItem(i);
            }
            outState.putLongArray("times", arr);
        }*//*

        TextView counter = (TextView) findViewById(R.id.counter);
        if (counter != null){
            outState.putCharSequence("currentTime", counter.getText());
        }
        TextView date = (TextView) findViewById(R.id.task_date);
        if (date!=null){
            outState.putCharSequence("dateTime", date.getText());
        }
        super.onSaveInstanceState(outState);
    }*/

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        // Bind to the TimerService
        bindTimerService();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("id", mCurrentTask);
        outState.putLong("time", mCurrentTime);
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onDestroy() {
        //mHandler.removeMessages(0);
        if (mTimeReceiver != null)
            unregisterReceiver(mTimeReceiver);

        if (mTimerService != null) {
            mCurrentTask = mTimerService.getTaskId();
            mCurrentTime = mTimerService.getTime();
            unbindService(this);
            mTimerService = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            long taskId = data.getLongExtra(EditTaskActivity.TASK_ID, 0);
            long time = data.getLongExtra(EditTaskActivity.TASK_TIME, 0);
            String name = data.getStringExtra(EditTaskActivity.TASK_NAME);
            long date = data.getLongExtra(EditTaskActivity.TASK_DATE, 0);
            String desc = data.getStringExtra(EditTaskActivity.TASK_DESCRIPTION);

            if (taskId > -1) {
                onTaskSelected(taskId, name, desc, date, time);
            }
        }
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

/*        if (v.getId() == R.id.start_stop) {
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
        }*/

        if (v.getId() == R.id.start_stop) {
            if (mTimerService == null) {
                ssButton.setText(R.string.stop);
                startService(new Intent(this, TimerService.class));
            } else if (!mTimerService.isTimerRunning()) {
                ssButton.setText(R.string.stop);
                mTimerService.setTask(mCurrentTask, mCurrentTime);
                mTimerService.startService(new Intent(this, TimerService.class));
            } else {
                mCurrentTask = mTimerService.getTaskId();
                mCurrentTime = mTimerService.getTime();
                ssButton.setText(R.string.start);
                mTimerService.stopTimer();
            }
        } else if (v.getId() == R.id.edit) {
            mCurrentTask = mTimerService.getTaskId();
            mCurrentTime = mTimerService.getTime();
            ssButton.setText(R.string.start);
            mTimerService.stopTimer();
            // Finish the time input activity
            Intent intent = new Intent(TimeTrackerActivity.this, EditTaskActivity.class);
            intent.putExtra(EditTaskActivity.TASK_ID, mTimerService.getTaskId());
            intent.putExtra(EditTaskActivity.TASK_TIME, mTimerService.getTime());
            startActivityForResult(intent, 0);
        }else if (v.getId() == R.id.new_task) {
            startNewTimerTask();
            ssButton.setText(R.string.start);
        }
    }
    private void startNewTimerTask() {
        mPager.setCurrentItem(0);
        mTimerService.resetTimer();

        Resources res = getResources();
        onTaskSelected(
                -1,
                res.getString(R.string.new_task),
                res.getString(R.string.description),
                System.currentTimeMillis(),
                0);
    }
/*    @Override
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
    }*/

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
                if (counter != null){
                    counter.setText(DateUtils.formatElapsedTime(time/1000));
                }
            } /*else if (ACTION_TIMER_FINISHED.equals(action)) {
                if (mTimeListAdapter != null && time > 0)
                    mTimeListAdapter.add(time/1000);
            }*/
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final ActionBar bar = getSupportActionBar();
            bar.setSelectedNavigationItem(position);
        } else {
            mTabHost.setCurrentTab(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTaskSelected(long id, String name, String desc, long date, long time) {
        mPager.setCurrentItem(0);
        // ViewPager keeps fragments by tag: "android:switcher:<pager_id>:<item_pos>"
        TimerFragment frag = (TimerFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0");
        frag.setName(name);
        frag.setDescription(desc);
        frag.setDate(DateUtils.formatDateTime(this, date, DATE_FLAGS));
        frag.setCounter(DateUtils.formatElapsedTime(time/1000));
        mCurrentTask = id;
        mCurrentTime = time;
        if (mTimerService != null){
            mTimerService.setTask(id, time);
        }
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
