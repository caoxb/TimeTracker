package com.example.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import com.example.R;
import com.example.TimeTrackerActivity;
import com.example.service.TimerService;

/**
 * Implementation of App Widget functionality.
 */
public class TimerWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TimeTrackerActivity.ACTION_TIME_UPDATE.equals(action)) {
            Bundle extras = intent.getExtras();
            long time = extras.getLong("time");

            updateWidgetTime(context, time, true);
            return;
        } else if (TimeTrackerActivity.ACTION_TIMER_FINISHED.equals(action) || TimeTrackerActivity.ACTION_TIMER_STOPPED.equals(action)) {
            Bundle extras = intent.getExtras();
            long time = extras.getLong("time");

            updateWidgetTime(context, time, false);
            return;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, TimerService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timer_widget_provider);
        views.setOnClickPendingIntent(R.id.start_stop, pi);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private void updateWidgetTime(Context context, long time, boolean isRunning) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TimerWidgetProvider.class));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timer_widget_provider);
        views.setTextViewText(R.id.counter, DateUtils.formatElapsedTime(time/1000));
        views.setImageViewResource(R.id.start_stop, isRunning ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);

        manager.updateAppWidget(ids, views);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

