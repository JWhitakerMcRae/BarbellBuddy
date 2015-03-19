package com.mcraesolutions.barbellbuddy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;

import com.mcraesolutions.watchfacelibrary.WatchfaceLayout;

public class SetTimerWearActivity extends Activity {

    private static final String TAG = "BarbellBuddyWear";

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

    // watchface widget
    private WatchfaceLayout mWatchface; // watchface object inflated from XML, reference set in onCreate

    // broadcast receiver to listen for settings updates (sent from service)
    BroadcastReceiver mUpdateSettingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_UPDATE_SETTINGS_VALUES)) {
                updateSettingsValues(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onCreate");
        //}
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_timer_wear);

        // keep the watch screen from timing out
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // create on layout inflated listener
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // find watchface object
                mWatchface = (WatchfaceLayout) stub.findViewById(R.id.watchfaceLayout_watchface);

                // auto-start watchface chronometer
                mWatchface.startChronometer();

                // read settings values
                readSettingsValues();
            }
        });
    }

    @Override
    protected void onStart() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onStart");
        //}
        super.onStart();

        // initialize settings values from resource file
        initSettingsValues();

        // start listener service
        startService(new Intent(this, SettingsListenerService.class));
    }

    @Override
    protected void onResume() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onResume");
        //}
        super.onResume();

        // read settings values
        readSettingsValues();

        try {
            // register update settings receiver
            IntentFilter intentFilter = new IntentFilter(BROADCAST_UPDATE_SETTINGS_VALUES);
            registerReceiver(mUpdateSettingsReceiver, intentFilter);
        }
        catch (NullPointerException e) {
            e.printStackTrace(); // most likely initSettingsValues didn't complete successfully
        }
    }

    @Override
    protected void onPause() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onPause");
        //}
        super.onPause();

        // deregister update settings receiver
        if (mUpdateSettingsReceiver != null) {
            unregisterReceiver(mUpdateSettingsReceiver);
        }
    }

    // onStop

    // onDestroy

    // ****************************************************************************************** //

    private void initSettingsValues() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "initSettingsValues");
        //}

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
        }
        catch (NullPointerException e) {
            e.printStackTrace(); // most likely getting called too soon, before Resources object is created
        }
    }

    private void readSettingsValues() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "readSettingsValues");
        //}
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // make sure watchface has been inflated
        if (mWatchface == null) {
            Log.w(TAG, "Unable to read settings values, watchface object is still null!");
            return;
        }

        try {
            // set phase lengths (ms) -- set from settings (phone)
            if (sharedPref.contains(EXTRA_PREPARE_PHASE_LENGTH_MS)) {
                mWatchface.setPreparePhaseLength_ms(sharedPref.getInt(EXTRA_PREPARE_PHASE_LENGTH_MS, mWatchface.getPreparePhaseLength_ms()));
            }
            if (sharedPref.contains(EXTRA_LIFT_PHASE_LENGTH_MS)) {
                mWatchface.setLiftPhaseLength_ms(sharedPref.getInt(EXTRA_LIFT_PHASE_LENGTH_MS, mWatchface.getLiftPhaseLength_ms()));
            }
            if (sharedPref.contains(EXTRA_WAIT_PHASE_LENGTH_MS)) {
                mWatchface.setWaitPhaseLength_ms(sharedPref.getInt(EXTRA_WAIT_PHASE_LENGTH_MS, mWatchface.getWaitPhaseLength_ms()));
            }

            // set phase background colors -- set from settings (phone)
            if (sharedPref.contains(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR)) {
                mWatchface.setPreparePhaseBackgroundColor(sharedPref.getInt(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, mWatchface.getPreparePhaseBackgroundColor()));
            }
            if (sharedPref.contains(EXTRA_LIFT_PHASE_BACKGROUND_COLOR)) {
                mWatchface.setLiftPhaseBackgroundColor(sharedPref.getInt(EXTRA_LIFT_PHASE_BACKGROUND_COLOR, mWatchface.getLiftPhaseBackgroundColor()));
            }
            if (sharedPref.contains(EXTRA_WAIT_PHASE_BACKGROUND_COLOR)) {
                mWatchface.setWaitPhaseBackgroundColor(sharedPref.getInt(EXTRA_WAIT_PHASE_BACKGROUND_COLOR, mWatchface.getWaitPhaseBackgroundColor()));
            }

            // set phase alert on/off status
            if (sharedPref.contains(EXTRA_PREPARE_PHASE_START_ALERT_ON)) {
                mWatchface.setPreparePhaseStartAlertOn(sharedPref.getBoolean(EXTRA_PREPARE_PHASE_START_ALERT_ON, mWatchface.isPreparePhaseStartAlertOn()));
            }
            if (sharedPref.contains(EXTRA_LIFT_PHASE_START_ALERT_ON)) {
                mWatchface.setLiftPhaseStartAlertOn(sharedPref.getBoolean(EXTRA_LIFT_PHASE_START_ALERT_ON, mWatchface.isLiftPhaseStartAlertOn()));
            }
            if (sharedPref.contains(EXTRA_WAIT_PHASE_START_ALERT_ON)) {
                mWatchface.setWaitPhaseStartAlertOn(sharedPref.getBoolean(EXTRA_WAIT_PHASE_START_ALERT_ON, mWatchface.isWaitPhaseStartAlertOn()));
            }
        }
        catch (NullPointerException e) { // most likely initSettingsValues didn't complete successfully, possibly bad shared preferences or watchface object
            e.printStackTrace();
        }
    }

    private void updateSettingsValues(Intent intent) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "updateSettingsValues");
        //}

        try {
            // set phase lengths (ms) -- set from settings (phone)
            if (intent.getExtras().keySet().contains(EXTRA_PREPARE_PHASE_LENGTH_MS)) {
                mWatchface.setPreparePhaseLength_ms(intent.getIntExtra(EXTRA_PREPARE_PHASE_LENGTH_MS, mWatchface.getPreparePhaseLength_ms()));
            }
            if (intent.getExtras().keySet().contains(EXTRA_LIFT_PHASE_LENGTH_MS)) {
                mWatchface.setLiftPhaseLength_ms(intent.getIntExtra(EXTRA_LIFT_PHASE_LENGTH_MS, mWatchface.getLiftPhaseLength_ms()));
            }
            if (intent.getExtras().keySet().contains(EXTRA_WAIT_PHASE_LENGTH_MS)) {
                mWatchface.setWaitPhaseLength_ms(intent.getIntExtra(EXTRA_WAIT_PHASE_LENGTH_MS, mWatchface.getWaitPhaseLength_ms()));
            }

            // set phase background colors -- set from settings (phone)
            if (intent.getExtras().keySet().contains(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR)) {
                mWatchface.setPreparePhaseBackgroundColor(intent.getIntExtra(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, mWatchface.getPreparePhaseBackgroundColor()));
            }
            if (intent.getExtras().keySet().contains(EXTRA_LIFT_PHASE_BACKGROUND_COLOR)) {
                mWatchface.setLiftPhaseBackgroundColor(intent.getIntExtra(EXTRA_LIFT_PHASE_BACKGROUND_COLOR, mWatchface.getLiftPhaseBackgroundColor()));
            }
            if (intent.getExtras().keySet().contains(EXTRA_WAIT_PHASE_BACKGROUND_COLOR)) {
                mWatchface.setWaitPhaseBackgroundColor(intent.getIntExtra(EXTRA_WAIT_PHASE_BACKGROUND_COLOR, mWatchface.getWaitPhaseBackgroundColor()));
            }

            // set phase alert on/off status
            if (intent.getExtras().keySet().contains(EXTRA_PREPARE_PHASE_START_ALERT_ON)) {
                mWatchface.setPreparePhaseStartAlertOn(intent.getBooleanExtra(EXTRA_PREPARE_PHASE_START_ALERT_ON, mWatchface.isPreparePhaseStartAlertOn()));
            }
            if (intent.getExtras().keySet().contains(EXTRA_LIFT_PHASE_START_ALERT_ON)) {
                mWatchface.setLiftPhaseStartAlertOn(intent.getBooleanExtra(EXTRA_LIFT_PHASE_START_ALERT_ON, mWatchface.isLiftPhaseStartAlertOn()));
            }
            if (intent.getExtras().keySet().contains(EXTRA_WAIT_PHASE_START_ALERT_ON)) {
                mWatchface.setWaitPhaseStartAlertOn(intent.getBooleanExtra(EXTRA_WAIT_PHASE_START_ALERT_ON, mWatchface.isWaitPhaseStartAlertOn()));
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace(); // most likely initSettingsValues didn't complete successfully, possibly bad watchface object
        }
    }
}
