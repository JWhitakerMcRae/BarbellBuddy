package com.mcraesolutions.barbellbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.mcraesolutions.utils.ColorPickerPreference;
import com.mcraesolutions.utils.YesNoPreference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    private ActivityCallbackInterface mCallback;
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

    /*
     * Keys Without Intents
     */

    public String KEY_RESET_DEFAULT_VALUES; // initialize by initSettingsValues()

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

    // ****************************************************************************************** //

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    static SettingsFragment sSettingsFragment;

    public static SettingsFragment singleton() {
        if (sSettingsFragment == null) {
            sSettingsFragment = new SettingsFragment();
        }
        return sSettingsFragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    // ****************************************************************************************** //

    @Override
    public void onAttach(Activity activity) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onAttach");
        //}
        super.onAttach(activity);

        try {
            mCallback = (ActivityCallbackInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ActivityCallbackInterface");
        }

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onCreate");
        //}
        super.onCreate(savedInstanceState);

        // load preferences layout from XML
        addPreferencesFromResource(R.xml.preferences);
    }

    // onCreateView

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onActivityCreated");
        //}
        super.onActivityCreated(savedInstanceState);

        // NOTE: although the wear app initializes settings values here the mobile app does this in onResume
    }

    // onStart

    @Override
    public void onResume() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onResume");
        //}
        super.onResume();

        // initialize settings values from resource file
        initSettingsValues();

        // register preferences onChange listeners
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onPause");
        //}
        super.onPause();

        // unregister preferences onChange listeners
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    // onStop

    // onDestroyView

    // onDestroy

    @Override
    public void onDetach() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onDetach");
        //}
        super.onDetach();

        mListener = null;
    }

    // ****************************************************************************************** //

    // SharedPreferences.OnSharedPreferenceChangeListener

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onSharedPreferenceChanged");
        //}

        if (key.equals(EXTRA_PREPARE_PHASE_LENGTH_MS)) {
            int value = Integer.parseInt(sharedPreferences.getString(EXTRA_PREPARE_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.prepare_phase_length_ms))));
            updatePreparePhaseLengthPreferenceSummary(value);

            // sync preference value
            syncIntPreferenceToString(PATH_PREPARE_PHASE_LENGTH_MS, EXTRA_PREPARE_PHASE_LENGTH_MS, value);
        }
        else if (key.equals(EXTRA_LIFT_PHASE_LENGTH_MS)) {
            int value = Integer.parseInt(sharedPreferences.getString(EXTRA_LIFT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.lift_phase_length_ms))));
            updateLiftPhaseLengthPreferenceSummary(value);

            // sync preference value
            syncIntPreferenceToString(PATH_LIFT_PHASE_LENGTH_MS, EXTRA_LIFT_PHASE_LENGTH_MS, value);

        }
        else if (key.equals(EXTRA_WAIT_PHASE_LENGTH_MS)) {
            int value = Integer.parseInt(sharedPreferences.getString(EXTRA_WAIT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.wait_phase_length_ms))));
            updateWaitPhaseLengthPreferenceSummary(value);

            // sync preference value
            syncIntPreferenceToString(PATH_WAIT_PHASE_LENGTH_MS, EXTRA_WAIT_PHASE_LENGTH_MS, value);

        }
        else if (key.equals(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR)) {
            int value = sharedPreferences.getInt(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.prepare_background_color));

            // sync preference value
            syncIntPreference(PATH_PREPARE_PHASE_BACKGROUND_COLOR, EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, value);
        }
        else if (key.equals(EXTRA_LIFT_PHASE_BACKGROUND_COLOR)) {
            int value = sharedPreferences.getInt(EXTRA_LIFT_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.lift_background_color));

            // sync preference value
            syncIntPreference(PATH_LIFT_PHASE_BACKGROUND_COLOR, EXTRA_LIFT_PHASE_BACKGROUND_COLOR, value);
        }
        else if (key.equals(EXTRA_WAIT_PHASE_BACKGROUND_COLOR)) {
            int value = sharedPreferences.getInt(EXTRA_WAIT_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.wait_background_color));

            // sync preference value
            syncIntPreference(PATH_WAIT_PHASE_BACKGROUND_COLOR, EXTRA_WAIT_PHASE_BACKGROUND_COLOR, value);
        }
        else if (key.equals(EXTRA_PREPARE_PHASE_START_ALERT_ON)) {
            boolean value = sharedPreferences.getBoolean(EXTRA_PREPARE_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.prepare_phase_start_alert_on));

            // sync preference value
            syncBooleanPreference(PATH_PREPARE_PHASE_START_ALERT_ON, EXTRA_PREPARE_PHASE_START_ALERT_ON, value);
        }
        else if (key.equals(EXTRA_LIFT_PHASE_START_ALERT_ON)) {
            boolean value = sharedPreferences.getBoolean(EXTRA_LIFT_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.lift_phase_start_alert_on));

            // sync preference value
            syncBooleanPreference(PATH_LIFT_PHASE_START_ALERT_ON, EXTRA_LIFT_PHASE_START_ALERT_ON, value);
        }
        else if (key.equals(EXTRA_WAIT_PHASE_START_ALERT_ON)) {
            boolean value = sharedPreferences.getBoolean(EXTRA_WAIT_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.wait_phase_start_alert_on));

            // sync preference value
            syncBooleanPreference(PATH_WAIT_PHASE_START_ALERT_ON, EXTRA_WAIT_PHASE_START_ALERT_ON, value);
        }
        else if (key.equals(KEY_RESET_DEFAULT_VALUES)) {
            boolean value = sharedPreferences.getBoolean(KEY_RESET_DEFAULT_VALUES, false);

            if (value) {
                resetDefaultValues();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_RESET_DEFAULT_VALUES, false);
                editor.apply();
            }

        }
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
        public void onFragmentInteraction(Uri uri);
    }

    // ****************************************************************************************** //

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void syncPreferences() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "syncPreferences");
        //}

        try {
            syncPreferences(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()));
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // ****************************************************************************************** //

    private void initSettingsValues() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "initSettingsValues");
        //}

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

            /*
             * Keys Without Intents
             */

            KEY_RESET_DEFAULT_VALUES = getResources().getString(R.string.key_reset_default_values);

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

            // settings data - set phase alert on/off status (value should be boolean, true = on, false = off)
            PATH_PREPARE_PHASE_START_ALERT_ON = getResources().getString(R.string.path_prepare_phase_start_alert_on);
            PATH_LIFT_PHASE_START_ALERT_ON = getResources().getString(R.string.path_lift_phase_start_alert_on);
            PATH_WAIT_PHASE_START_ALERT_ON = getResources().getString(R.string.path_wait_phase_start_alert_on);
        }
        catch (NullPointerException e) {
            e.printStackTrace(); // most likely getting called too soon, before Resources object is created
        }

        // initialize summary strings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        updatePreparePhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString(EXTRA_PREPARE_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.prepare_phase_length_ms)))));
        updateLiftPhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString(EXTRA_LIFT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.lift_phase_length_ms)))));
        updateWaitPhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString(EXTRA_WAIT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.wait_phase_length_ms)))));
    }

    private void updatePreparePhaseLengthPreferenceSummary(int value) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "updatePreparePhaseLengthPreferenceSummary");
        //}

        ListPreference preparePhaseLengthPreference = (ListPreference) findPreference(EXTRA_PREPARE_PHASE_LENGTH_MS);
        try {
            preparePhaseLengthPreference.setSummary(value/1000 + " " + getResources().getString(R.string.unit_seconds));
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void updateLiftPhaseLengthPreferenceSummary(int value) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "updateLiftPhaseLengthPreferenceSummary");
        //}

        ListPreference liftPhaseLengthPreference = (ListPreference) findPreference(EXTRA_LIFT_PHASE_LENGTH_MS);
        try {
            liftPhaseLengthPreference.setSummary(value/1000 + " " + getResources().getString(R.string.unit_seconds));
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void updateWaitPhaseLengthPreferenceSummary(int value) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "updateWaitPhaseLengthPreferenceSummary");
        //}

        ListPreference waitPhaseLengthPreference = (ListPreference) findPreference(EXTRA_WAIT_PHASE_LENGTH_MS);
        try {
            waitPhaseLengthPreference.setSummary(value/1000 + " " + getResources().getString(R.string.unit_seconds));
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void resetDefaultValues() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "resetDefaultValues");
        //}

        // clear shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPreferences.edit().clear().commit();

        // reset shared preference default values
        //resetPreferences();

        // sync shared preferences
        syncPreferences();

        // TODO: find way to update UI without closing fragment here
        getFragmentManager().popBackStack();
    }

    private void resetPreferences() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "resetPreferences");
        //}

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        //SharedPreferences.Editor editor = sharedPreferences.edit();

        //editor.putString(EXTRA_PREPARE_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.prepare_phase_length_ms)));
        //editor.putString(EXTRA_LIFT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.lift_phase_length_ms)));
        //editor.putString(EXTRA_WAIT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.wait_phase_length_ms)));

        //editor.putInt(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.prepare_background_color));
        //editor.putInt(EXTRA_WAIT_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.wait_background_color));
        //editor.putInt(EXTRA_LIFT_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.lift_background_color));

        //editor.putBoolean(EXTRA_PREPARE_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.prepare_phase_start_alert_on));
        //editor.putBoolean(EXTRA_WAIT_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.wait_phase_start_alert_on));
        //editor.putBoolean(EXTRA_LIFT_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.lift_phase_start_alert_on));

        //editor.putBoolean(KEY_RESET_DEFAULT_VALUES, false);
        //editor.commit();

        ListPreference preparePhaseLength_ms = (ListPreference) findPreference(EXTRA_PREPARE_PHASE_LENGTH_MS);
        preparePhaseLength_ms.setValue(Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.prepare_phase_length_ms)));
        ListPreference liftPhaseLength_ms = (ListPreference) findPreference(EXTRA_LIFT_PHASE_LENGTH_MS);
        liftPhaseLength_ms.setValue(Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.lift_phase_length_ms)));
        ListPreference waitPhaseLength_ms = (ListPreference) findPreference(EXTRA_WAIT_PHASE_LENGTH_MS);
        waitPhaseLength_ms.setValue(Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.wait_phase_length_ms)));

        ColorPickerPreference preparePhaseBackgroundColor = (ColorPickerPreference) findPreference(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR);
        preparePhaseBackgroundColor.setValue(getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.prepare_background_color));
        ColorPickerPreference liftPhaseBackgroundColor = (ColorPickerPreference) findPreference(EXTRA_LIFT_PHASE_BACKGROUND_COLOR);
        liftPhaseBackgroundColor.setValue(getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.lift_background_color));
        ColorPickerPreference waitPhaseBackgroundColor = (ColorPickerPreference) findPreference(EXTRA_WAIT_PHASE_BACKGROUND_COLOR);
        waitPhaseBackgroundColor.setValue(getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.wait_background_color));

        CheckBoxPreference preparePhaseStartAlertOn = (CheckBoxPreference) findPreference(EXTRA_PREPARE_PHASE_START_ALERT_ON);
        preparePhaseStartAlertOn.setChecked(getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.prepare_phase_start_alert_on));
        CheckBoxPreference liftPhaseStartAlertOn = (CheckBoxPreference) findPreference(EXTRA_LIFT_PHASE_START_ALERT_ON);
        liftPhaseStartAlertOn.setChecked(getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.lift_phase_start_alert_on));
        CheckBoxPreference waitPhaseStartAlertOn = (CheckBoxPreference) findPreference(EXTRA_WAIT_PHASE_START_ALERT_ON);
        waitPhaseStartAlertOn.setChecked(getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.wait_phase_start_alert_on));

        YesNoPreference resetDefaultValues = (YesNoPreference) findPreference(KEY_RESET_DEFAULT_VALUES);
        resetDefaultValues.setValue(false);
    }

    private void syncPreferences(SharedPreferences sharedPreferences) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "syncPreferences: " + sharedPreferences);
        //}

        /*if (sharedPreferences.contains(EXTRA_PREPARE_PHASE_LENGTH_MS))*/ {
            int value = Integer.parseInt(sharedPreferences.getString(EXTRA_PREPARE_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.prepare_phase_length_ms))));
            updatePreparePhaseLengthPreferenceSummary(value);

            // sync preference value
            syncIntPreferenceToString(PATH_PREPARE_PHASE_LENGTH_MS, EXTRA_PREPARE_PHASE_LENGTH_MS, value);
        }
        /*else if (key.equals(EXTRA_LIFT_PHASE_LENGTH_MS))*/ {
            int value = Integer.parseInt(sharedPreferences.getString(EXTRA_LIFT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.lift_phase_length_ms))));
            updateLiftPhaseLengthPreferenceSummary(value);

            // sync preference value
            syncIntPreferenceToString(PATH_LIFT_PHASE_LENGTH_MS, EXTRA_LIFT_PHASE_LENGTH_MS, value);
        }
        /*else if (key.equals(EXTRA_WAIT_PHASE_LENGTH_MS))*/ {
            int value = Integer.parseInt(sharedPreferences.getString(EXTRA_WAIT_PHASE_LENGTH_MS, Integer.toString(getResources().getInteger(com.mcraesolutions.watchfacelibrary.R.integer.wait_phase_length_ms))));
            updateWaitPhaseLengthPreferenceSummary(value);

            // sync preference value
            syncIntPreferenceToString(PATH_WAIT_PHASE_LENGTH_MS, EXTRA_WAIT_PHASE_LENGTH_MS, value);
        }
        /*else if (key.equals(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR))*/ {
            int value = sharedPreferences.getInt(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.prepare_background_color));

            // sync preference value
            syncIntPreference(PATH_PREPARE_PHASE_BACKGROUND_COLOR, EXTRA_PREPARE_PHASE_BACKGROUND_COLOR, value);
        }
        /*else if (key.equals(EXTRA_LIFT_PHASE_BACKGROUND_COLOR))*/ {
            int value = sharedPreferences.getInt(EXTRA_LIFT_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.lift_background_color));

            // sync preference value
            syncIntPreference(PATH_LIFT_PHASE_BACKGROUND_COLOR, EXTRA_LIFT_PHASE_BACKGROUND_COLOR, value);
        }
        /*else if (key.equals(EXTRA_WAIT_PHASE_BACKGROUND_COLOR))*/ {
            int value = sharedPreferences.getInt(EXTRA_WAIT_PHASE_BACKGROUND_COLOR, getResources().getColor(com.mcraesolutions.watchfacelibrary.R.color.wait_background_color));

            // sync preference value
            syncIntPreference(PATH_WAIT_PHASE_BACKGROUND_COLOR, EXTRA_WAIT_PHASE_BACKGROUND_COLOR, value);
        }
        /*else if (key.equals(EXTRA_PREPARE_PHASE_START_ALERT_ON))*/ {
            boolean value = sharedPreferences.getBoolean(EXTRA_PREPARE_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.prepare_phase_start_alert_on));

            // sync preference value
            syncBooleanPreference(PATH_PREPARE_PHASE_START_ALERT_ON, EXTRA_PREPARE_PHASE_START_ALERT_ON, value);
        }
        /*else if (key.equals(EXTRA_LIFT_PHASE_START_ALERT_ON))*/ {
            boolean value = sharedPreferences.getBoolean(EXTRA_LIFT_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.lift_phase_start_alert_on));

            // sync preference value
            syncBooleanPreference(PATH_LIFT_PHASE_START_ALERT_ON, EXTRA_LIFT_PHASE_START_ALERT_ON, value);
        }
        /*else if (key.equals(EXTRA_WAIT_PHASE_START_ALERT_ON))*/ {
            boolean value = sharedPreferences.getBoolean(EXTRA_WAIT_PHASE_START_ALERT_ON, getResources().getBoolean(com.mcraesolutions.watchfacelibrary.R.bool.wait_phase_start_alert_on));

            // sync preference value
            syncBooleanPreference(PATH_WAIT_PHASE_START_ALERT_ON, EXTRA_WAIT_PHASE_START_ALERT_ON, value);
        }
        /*else if (key.equals(KEY_RESET_DEFAULT_VALUES))*/ {
            boolean value = sharedPreferences.getBoolean(KEY_RESET_DEFAULT_VALUES, false);

            // do nothing
        }
    }

    // *** start sync individual preference data ***

    private PendingResult<DataApi.DataItemResult> syncIntPreference(String path, String key, int value) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "syncIntPreference: " + path + ", " + key + ", 0x" + Integer.toHexString(value));
        //}

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();

        // broadcast shared preference (in case activity running)
        Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
        intent.putExtra(key, value);
        getActivity().sendBroadcast(intent);

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putInt(key, value);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mCallback.getGoogleApiClient(), putDataReq);
        return pendingResult;
    }

    private PendingResult<DataApi.DataItemResult> syncIntPreferenceToString(String path, String key, int value) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "syncIntPreference: " + path + ", " + key + ", 0x" + Integer.toHexString(value));
        //}

        // TODO: not all int preferences should be stored as strings, need to handle this better than just creating a duplicate function that converts preference value to string
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, Integer.toString(value));
        editor.commit();

        // broadcast shared preference (in case activity running)
        Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
        intent.putExtra(key, value);
        getActivity().sendBroadcast(intent);

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putInt(key, value);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mCallback.getGoogleApiClient(), putDataReq);
        return pendingResult;
    }

    private PendingResult<DataApi.DataItemResult> syncBooleanPreference(String path, String key, boolean value) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "syncBooleanPreference: " + path + ", " + key + ", " + value);
        //}

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();

        // broadcast shared preference (in case activity running)
        Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
        intent.putExtra(key, value);
        getActivity().sendBroadcast(intent);

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putBoolean(key, value);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mCallback.getGoogleApiClient(), putDataReq);
        return pendingResult;
    }

    private PendingResult<DataApi.DataItemResult> syncStringPreference(String path, String key, String value) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "syncStringPreference: " + path + ", " + key + ", " + value);
        //}

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();

        // broadcast shared preference (in case activity running)
        Intent intent = new Intent(BROADCAST_UPDATE_SETTINGS_VALUES);
        intent.putExtra(key, value);
        getActivity().sendBroadcast(intent);

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putString(key, value);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mCallback.getGoogleApiClient(), putDataReq);
        return pendingResult;
    }

    // ****************************************************************************************** //

    public interface ActivityCallbackInterface {
        public GoogleApiClient getGoogleApiClient();
    }
}
