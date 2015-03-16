package com.mcraesolutions.barbellbuddy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mcraesolutions.watchfacelibrary.WatchfaceLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetTimerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetTimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetTimerFragment extends Fragment {

    private static final String TAG = "BarbellBuddy";

    private OnFragmentInteractionListener mListener;

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

    // ****************************************************************************************** //

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetTimerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetTimerFragment newInstance() {
        SetTimerFragment fragment = new SetTimerFragment();
        return fragment;
    }

    public SetTimerFragment() {
        // Required empty public constructor
    }

    // ****************************************************************************************** //

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_timer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onActivityCreated");
        //}
        super.onActivityCreated(savedInstanceState);

        // NOTE: although the wear app keeps screen on here the mobile app does not

        // NOTE: although the wear app initializes settings values here the mobile app does this in onResume

        // find watchface object
        mWatchface = (WatchfaceLayout) getActivity().findViewById(R.id.watchfaceLayout_watchface);

        // find set timer start/stop button, set up on click listener
        mStartStopButton = (Button) getActivity().findViewById(R.id.button_setTimerStartStop);
        mStartStopButton.setOnClickListener(mOnStartStopListener);

        // NOTE: although the wear app auto starts set timer here the mobile app does not
    }

    // onStart

    @Override
    public void onResume() {
        super.onResume();

        // initialize settings values from resource file
        initSettingsValues();

        // read/default settings values
        //readSettingsValues(); // TODO: fix this so it doesn't crash and can be called
    }


    // onPause

    // onStop

    // onDestroyView

    // onDestroy

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // ****************************************************************************************** //

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    // ****************************************************************************************** //

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
