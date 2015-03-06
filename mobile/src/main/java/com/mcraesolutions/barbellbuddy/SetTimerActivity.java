package com.mcraesolutions.barbellbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mcraesolutions.watchfacelibrary.WatchfaceLayout;


public class SetTimerActivity extends ActionBarActivity {

    private static final String TAG = "BarbellBuddy";

    // Google API client
    GoogleApiClient mGoogleApiClient;
    boolean mGoogleApiClientConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onCreate");
        //}
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_timer);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SetTimerFragment())
                    .commit();
        } // TODO: why use SupportFragmentManager instead of FragmentManager ???
    }

    @Override
    protected void onStart() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onStart");
        //}
        super.onStart();

        // Initialize Google API Client
        initGoogleApi();
    }

    @Override
    protected void onStop() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onStop");
        //}
        super.onStop();

        // disconnect Google API Client
        disconnectGoogleApi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onCreateOptionsMenu");
        //}

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onOptionsItemSelected");
        //}

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start_wear_app) {
            return startWearApp();
        }
        else if (id == R.id.action_settings) {
            return openSettings();
        }
        else if (id == R.id.action_help) {
            return openHelp();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initGoogleApi() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "initGoogleApi");
        //}

        // Create instance
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                            mGoogleApiClientConnected = true;
                        }

                        @Override
                        public void onConnectionSuspended(int cause) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                            mGoogleApiClientConnected = false;
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                            mGoogleApiClientConnected = false;
                        }
                    })
                    // Request access only to the Wearable API
                    .addApi(Wearable.API)
                    .build();
        }

        // Start connection
        mGoogleApiClient.connect(); // ready to use once onConnected() callback is called
    }

    private void disconnectGoogleApi() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "disconnectGoogleApi");
        //}

        // Stop connection
        mGoogleApiClient.disconnect();
    }

    private boolean startWearApp() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "startWearApp");
        //}

        // sync value to wearable
        if (mGoogleApiClientConnected && mGoogleApiClient != null) {
            //PutDataMapRequest dataMap = PutDataMapRequest.create(getResources().getString(R.string.path_start_wear_activity)); // TODO: move constants from fragment to activity??
            //PutDataRequest request = dataMap.asPutDataRequest();
            //PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
            //        .putDataItem(mGoogleApiClient, request);
            if(mGoogleApiClient.isConnected()) { // TODO: replace mGoogleApiClientConnected attribute with this call?
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                        for (Node node : nodes.getNodes()) {
                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), getResources().getString(R.string.path_start_wear_activity), null).await();
                            if (result.getStatus().isSuccess()) {
                                Log.i("test", "success!! sent to: " + node.getDisplayName());
                            }
                            else {
                                Log.e("test", "error");
                            }
                        }
                    }
                }).start();
                return true;

            } else {
                Log.w(TAG, "Unable to sync data to the wearable due to Google API issue (not connected)");
            }
        }
        else { // not possible to sync
            Log.w(TAG, "Unable to sync data to the wearable due to Google API issue.");
        }
        return false;
    }

    private boolean openSettings() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "openSettings");
        //}

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
        return true;
    }

    private boolean openHelp() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "openHelp");
        //}

        getFragmentManager().beginTransaction()
                .add(R.id.container, new HelpFragment())
                .commit();
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SetTimerFragment extends Fragment {

        private static final String TAG = "BarbellBuddy";

        /*
         * Broadcast Actions
         */

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

        // set timer start/stop button, string values
        private Button mStartStopButton;
        private String BUTTON_STRING_START;
        private String BUTTON_STRING_STOP;

        // broadcast receiver to listen for settings updates (sent from service)
        BroadcastReceiver mUpdateSettingsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_UPDATE_SETTINGS_VALUES)) {
                    updateSettingsValues(intent);
                }
            }
        };

        // reset on click listener
        View.OnClickListener mOnStartStopListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "mOnStartStopListener.onClick");
                //}

                // toggle chronometer
                if (mWatchface.isChronometerRunning()) {
                    mWatchface.stopChronometer();

                    // attempt to update button string
                    try {
                        Button button = (Button) v;
                        button.setText(BUTTON_STRING_START); // just stopped, next press starts
                    }
                    catch (ClassCastException e) {
                        e.printStackTrace(); // most likely on click listener not set to a button
                    }
                }
                else { // chrono stopped
                    mWatchface.startChronometer();

                    // attempt to update button string
                    try {
                        Button button = (Button) v;
                        button.setText(BUTTON_STRING_STOP); // just started, next press stops
                    }
                    catch (ClassCastException e) {
                        e.printStackTrace(); // most likely on click listener not set to a button
                    }
                }
            }
        };

        public SetTimerFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "onCreateView");
            //}

            View rootView = inflater.inflate(R.layout.fragment_set_timer, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            //if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "onActivityCreated");
            //}
            super.onActivityCreated(savedInstanceState);

            // NOTE: although the wear app keeps screen on here the mobile app does not

            // initialize settings values from resource file
            initSettingsValues();

            // read/default settings values
            readSettingsValues();

            // find watchface object
            mWatchface = (WatchfaceLayout) getActivity().findViewById(R.id.watchfaceLayout_watchface);

            // find set timer start/stop button, set up on click listener
            mStartStopButton = (Button) getActivity().findViewById(R.id.button_setTimerStartStop);
            mStartStopButton.setOnClickListener(mOnStartStopListener);

            // NOTE: although the wear app auto starts set timer here the mobile app does not
        }

        private void initSettingsValues() {

            try {
                /*
                 * Broadcast Actions
                 */

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

                // get set timer start/stop button string values
                BUTTON_STRING_START = getResources().getString(R.string.button_set_timer_start);
                BUTTON_STRING_STOP = getResources().getString(R.string.button_set_timer_stop);
            }
            catch (NullPointerException e) {
                e.printStackTrace(); // most likely getting called too soon, before Resources object is created
            }
        }

        private void readSettingsValues() {
            //if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "readSettingsValues");
            //}
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
}
