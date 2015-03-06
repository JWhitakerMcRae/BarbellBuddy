package com.mcraesolutions.barbellbuddy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jwhitakermcrae on 2/9/15.
 */
public class SettingsListenerService extends WearableListenerService {

    private static final String TAG = "SettingsListenerService";

    /*
     * Broadcast Actions
     */

    public String BROADCAST_LAUNCH_WEAR_APP; // initialize by initSettingsValues()
    public String BROADCAST_UPDATE_SETTINGS_VALUES; // initialize by initSettingsValues()

    /*
     * Intent Extra Keys
     */

    // settings value - set phase length (value should be phase length as int, should be zero or positive and in milliseconds)
    public String EXTRA_PREPARE_PHASE_LENGTH_MS; // initialize by initSettingsValues()
    public String EXTRA_LIFT_PHASE_LENGTH_MS; // initialize by initSettingsValues()
    public String EXTRA_WAIT_PHASE_LENGTH_MS; // initialize by initSettingsValues()

    // settings value - set phase background color (value should be color value as int, usually displayed in hex)
    public String EXTRA_PREPARE_PHASE_BACKGROUND_COLOR; // initialize by initSettingsValues()
    public String EXTRA_LIFT_PHASE_BACKGROUND_COLOR; // initialize by initSettingsValues()
    public String EXTRA_WAIT_PHASE_BACKGROUND_COLOR; // initialize by initSettingsValues()

    // settings value - set phase alert on/off status (value should be boolean, true = on, false = off)
    public String EXTRA_PREPARE_PHASE_START_ALERT_ON; // initialize by initSettingsValues()
    public String EXTRA_LIFT_PHASE_START_ALERT_ON; // initialize by initSettingsValues()
    public String EXTRA_WAIT_PHASE_START_ALERT_ON; // initialize by initSettingsValues()

    /*
     * PutDataMapRequest Paths (used to read DataEvent objects from phone)
     */

    // start BarbellBuddy Wear app
    public String PATH_START_WEAR_ACTIVITY; // initialize by initSettingsValues()

    // settings data path - root level
    public String PATH_PREPARE_ROOT; // initialize by initSettingsValues()
    public String PATH_LIFT_ROOT; // initialize by initSettingsValues()
    public String PATH_WAIT_ROOT; // initialize by initSettingsValues()

    // settings data path - set phase length (ms)
    public String PATH_PREPARE_PHASE_LENGTH_MS; // initialize by initSettingsValues()
    public String PATH_LIFT_PHASE_LENGTH_MS; // initialize by initSettingsValues()
    public String PATH_WAIT_PHASE_LENGTH_MS; // initialize by initSettingsValues()

    // settings data path - set phase background color
    public String PATH_PREPARE_PHASE_BACKGROUND_COLOR; // initialize by initSettingsValues()
    public String PATH_LIFT_PHASE_BACKGROUND_COLOR; // initialize by initSettingsValues()
    public String PATH_WAIT_PHASE_BACKGROUND_COLOR; // initialize by initSettingsValues()

    // settings data path - set phase alert on/off status
    public String PATH_PREPARE_PHASE_START_ALERT_ON; // initialize by initSettingsValues()
    public String PATH_LIFT_PHASE_START_ALERT_ON; // initialize by initSettingsValues()
    public String PATH_WAIT_PHASE_START_ALERT_ON; // initialize by initSettingsValues()

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize settings values from resource file
        initSettingsValues();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents); // TODO: why wasn't this in Google tutorial?

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }
        final List<DataEvent> events = FreezableUtils
                .freezeIterable(dataEvents);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        // loop through the events and attempt to process each
        for (DataEvent event : events) {
            boolean processed = processDataEvent(event);
            if (!processed) {
                if (Log.isLoggable(TAG, Log.INFO)) {
                    Log.i(TAG, "Ignoring unprocessed data event...");
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        boolean processed = processMessage(messageEvent);
        if (!processed) {
            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, "Ignoring unprocessed message...");
            }
            super.onMessageReceived(messageEvent);
        }
    }

    private void initSettingsValues() {

        try {
            /*
             * Broadcast Actions
             */

            BROADCAST_LAUNCH_WEAR_APP = getResources().getString(R.string.broadcast_launch_wear_app);
            BROADCAST_UPDATE_SETTINGS_VALUES = getResources().getString(R.string.broadcast_update_settings_values);

            /*
             * Intent Extra Keys
             */

            // settings value - set phase length (value should be phase length as int, should be zero or positive and in milliseconds)
            EXTRA_PREPARE_PHASE_LENGTH_MS = getResources().getString(R.string.extra_prepare_phase_length_ms);
            EXTRA_LIFT_PHASE_LENGTH_MS = getResources().getString(R.string.extra_lift_phase_length_ms);
            EXTRA_WAIT_PHASE_LENGTH_MS = getResources().getString(R.string.extra_wait_phase_length_ms);

            // settings value - set phase background color (value should be color value as int, usually displayed in hex)
            EXTRA_PREPARE_PHASE_BACKGROUND_COLOR = getResources().getString(R.string.extra_prepare_phase_background_color);
            EXTRA_LIFT_PHASE_BACKGROUND_COLOR = getResources().getString(R.string.extra_lift_phase_background_color);
            EXTRA_WAIT_PHASE_BACKGROUND_COLOR = getResources().getString(R.string.extra_wait_phase_background_color);

            // settings value - set phase alert on/off status (value should be boolean, true = on, false = off)
            EXTRA_PREPARE_PHASE_START_ALERT_ON = getResources().getString(R.string.extra_prepare_phase_start_alert_on);
            EXTRA_LIFT_PHASE_START_ALERT_ON = getResources().getString(R.string.extra_lift_phase_start_alert_on);
            EXTRA_WAIT_PHASE_START_ALERT_ON = getResources().getString(R.string.extra_wait_phase_start_alert_on);

            /*
             * PutDataMapRequest Paths (used to read DataEvent objects from phone)
             */

            // start BarbellBuddy Wear app
            PATH_START_WEAR_ACTIVITY = getResources().getString(R.string.path_start_wear_activity);

            // settings data path - root level
            PATH_PREPARE_ROOT = getResources().getString(R.string.path_prepare_root);
            PATH_LIFT_ROOT = getResources().getString(R.string.path_lift_root);
            PATH_WAIT_ROOT = getResources().getString(R.string.path_wait_root);

            // settings data path - set phase length (ms)
            PATH_PREPARE_PHASE_LENGTH_MS = getResources().getString(R.string.path_prepare_phase_length_ms);
            PATH_LIFT_PHASE_LENGTH_MS = getResources().getString(R.string.path_lift_phase_length_ms);
            PATH_WAIT_PHASE_LENGTH_MS = getResources().getString(R.string.path_wait_phase_length_ms);

            // settings data path - set phase background color
            PATH_PREPARE_PHASE_BACKGROUND_COLOR = getResources().getString(R.string.path_prepare_phase_background_color);
            PATH_LIFT_PHASE_BACKGROUND_COLOR = getResources().getString(R.string.path_lift_phase_background_color);
            PATH_WAIT_PHASE_BACKGROUND_COLOR = getResources().getString(R.string.path_wait_phase_background_color);
        }
        catch (NullPointerException e) {
            e.printStackTrace(); // most likely getting called too soon, before Resources object is created
        }
    }

    /**
     * Processes input DataEvent objects.
     * @param event to be processed
     * @return true if processed successfully
     */
    private boolean processDataEvent(DataEvent event) {

        Uri uri = event.getDataItem().getUri();
        final String path = uri != null ? uri.getPath() : null;
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "DataEvent path: " + path);
        }

        // pass data event and path to root level parsers, return true if any are successful
        if (processStartWearActivity(event, path)) {
            return true;
        }
        else if (processSetPreparePhaseData(event, path)) {
            return true;
        }
        else if (processSetLiftPhaseData(event, path)) {
            return true;
        }
        if (processSetWaitPhaseData(event, path)) {
            return true;
        }
        return false;
    }

    private boolean processMessage(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(PATH_START_WEAR_ACTIVITY)) {
            startWearActivity();
            return true;
        }
        return false;
    }

    /*
     * DataEvent root level path parsers
     */

    private boolean processStartWearActivity(DataEvent event, String path) {
        if (path.equals(PATH_START_WEAR_ACTIVITY)) {
            return startWearActivity();
        }
        return false;
    }

    private boolean processSetPreparePhaseData(DataEvent event, String path) {

        if (path.startsWith(PATH_PREPARE_ROOT)) {

            if (path.equals(PATH_PREPARE_PHASE_LENGTH_MS)) {
                return setPreparePhaseLength_ms(event);
            }
            else if (path.equals(PATH_PREPARE_PHASE_BACKGROUND_COLOR)) {
                return setPreparePhaseBackgroundColor(event);
            }
            else if (path.equals(PATH_PREPARE_PHASE_START_ALERT_ON)) {
                return setPreparePhaseStartAlertOn(event);
            }
        }
        return false;
    }

    private boolean processSetLiftPhaseData(DataEvent event, String path) {

        if (path.startsWith(PATH_LIFT_ROOT)) {

            if (path.equals(PATH_LIFT_PHASE_LENGTH_MS)) {
                return setLiftPhaseLength_ms(event);
            }
            else if (path.equals(PATH_LIFT_PHASE_BACKGROUND_COLOR)) {
                return setLiftPhaseBackgroundColor(event);
            }
            else if (path.equals(PATH_LIFT_PHASE_START_ALERT_ON)) {
                return setLiftPhaseStartAlertOn(event);
            }
        }
        return false;
    }

    private boolean processSetWaitPhaseData(DataEvent event, String path) {

        if (path.startsWith(PATH_WAIT_ROOT)) {

            if (path.equals(PATH_WAIT_PHASE_LENGTH_MS)) {
                return setWaitPhaseLength_ms(event);
            }
            else if (path.equals(PATH_WAIT_PHASE_BACKGROUND_COLOR)) {
                return setWaitPhaseBackgroundColor(event);
            }
            else if (path.equals(PATH_WAIT_PHASE_START_ALERT_ON)) {
                return setWaitPhaseStartAlertOn(event);
            }
        }
        return false;
    }

    /*
     * DataEvent secondary path parsers
     */

    private boolean startWearActivity() {

        try {
            Intent wearActivityIntent = new Intent(this, SetTimerWearActivity.class);
            wearActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // launching activity
            startActivity(wearActivityIntent);
        }
        catch (ActivityNotFoundException e) {
            e.printStackTrace(); // most likely something failed in app install
            return false;
        }
        return true;
    }

    private boolean setPreparePhaseLength_ms(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_PREPARE_PHASE_LENGTH_MS)) {
            int value = map.getInt(EXTRA_PREPARE_PHASE_LENGTH_MS, -1); // default to invalid value
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new prepare phase length: " + value + " ms");
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(EXTRA_PREPARE_PHASE_LENGTH_MS, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_PREPARE_PHASE_LENGTH_MS, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set prepare phase length with invalid DataEvent.");
        }
        return false;
    }

    private boolean setPreparePhaseBackgroundColor(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR)) {
            int value = map.getInt(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new prepare phase background color: " + Integer.toHexString(value));
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set prepare phase background color with invalid DataEvent.");
        }
        return false;
    }

    private boolean setPreparePhaseStartAlertOn(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_LIFT_PHASE_START_ALERT_ON)) {
            boolean value = map.getBoolean(EXTRA_PREPARE_PHASE_START_ALERT_ON);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new prepare phase start alert on: " + value);
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(EXTRA_PREPARE_PHASE_START_ALERT_ON, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_PREPARE_PHASE_START_ALERT_ON, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set prepare phase start alert on/off with invalid DataEvent.");
        }
        return false;
    }

    private boolean setLiftPhaseLength_ms(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_LIFT_PHASE_LENGTH_MS)) {
            int value = map.getInt(EXTRA_LIFT_PHASE_LENGTH_MS);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new lift phase length: " + value + " ms");
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(EXTRA_LIFT_PHASE_LENGTH_MS, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_LIFT_PHASE_LENGTH_MS, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set lift phase length with invalid DataEvent.");
        }
        return false;
    }

    private boolean setLiftPhaseBackgroundColor(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_LIFT_PHASE_BACKGROUND_COLOR)) {
            int value = map.getInt(EXTRA_LIFT_PHASE_BACKGROUND_COLOR);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new lift phase background color: " + Integer.toHexString(value));
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(EXTRA_LIFT_PHASE_BACKGROUND_COLOR, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_LIFT_PHASE_BACKGROUND_COLOR, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set lift phase background color with invalid DataEvent.");
        }
        return false;
    }

    private boolean setLiftPhaseStartAlertOn(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_LIFT_PHASE_START_ALERT_ON)) {
            boolean value = map.getBoolean(EXTRA_LIFT_PHASE_START_ALERT_ON);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new lift phase start alert on: " + value);
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(EXTRA_LIFT_PHASE_START_ALERT_ON, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_LIFT_PHASE_START_ALERT_ON, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set lift phase start alert on/off with invalid DataEvent.");
        }
        return false;
    }

    private boolean setWaitPhaseLength_ms(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_WAIT_PHASE_LENGTH_MS)) {
            int value = map.getInt(EXTRA_WAIT_PHASE_LENGTH_MS);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new wait phase length: " + value + " ms");
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(EXTRA_WAIT_PHASE_LENGTH_MS, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_WAIT_PHASE_LENGTH_MS, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set wait phase length with invalid DataEvent.");
        }
        return false;
    }

    private boolean setWaitPhaseBackgroundColor(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_WAIT_PHASE_BACKGROUND_COLOR)) {
            int value = map.getInt(EXTRA_WAIT_PHASE_BACKGROUND_COLOR);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new wait phase background color: " + Integer.toHexString(value));
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(EXTRA_WAIT_PHASE_BACKGROUND_COLOR, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_WAIT_PHASE_BACKGROUND_COLOR, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set wait phase background color with invalid DataEvent.");
        }
        return false;
    }

    private boolean setWaitPhaseStartAlertOn(DataEvent event) {

        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        if (map.keySet().contains(EXTRA_WAIT_PHASE_BACKGROUND_COLOR)) {
            boolean value = map.getBoolean(EXTRA_WAIT_PHASE_START_ALERT_ON);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received new wait phase start alert on/off: " + value);
            }

            // save shared preference (read by activity on start)
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(EXTRA_WAIT_PHASE_START_ALERT_ON, value);
            editor.commit();

            // broadcast shared preference (in case activity running)
            Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
            intent.putExtra(EXTRA_WAIT_PHASE_START_ALERT_ON, value);
            sendBroadcast(intent);
            return true;
        }
        else {
            Log.w(TAG, "Attempting to set wait phase start alert on/off with invalid DataEvent.");
        }
        return false;
    }
}