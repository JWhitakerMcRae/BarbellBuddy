package com.mcraesolutions.barbellbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";

    private OnFragmentInteractionListener mListener;

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

    // ****************************************************************************************** //

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {
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

        // load preferences layout from XML
        addPreferencesFromResource(R.xml.preferences);

        // register preferences onChange listeners
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                if (key.equals(EXTRA_PREPARE_PHASE_LENGTH_MS)) {
                    // TODO: save value
                    updatePreparePhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString("prepare_phase_length_key", "0"))); // TODO: fix default value
                }
                else if (key.equals(EXTRA_LIFT_PHASE_LENGTH_MS)) {
                    // TODO: save value
                    updateLiftPhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString("prepare_phase_length_key", "0"))); // TODO: fix default value
                }
                else if (key.equals(EXTRA_WAIT_PHASE_LENGTH_MS)) {
                    // TODO: save value
                    updateWaitPhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString("prepare_phase_length_key", "0"))); // TODO: fix default value
                }
                else if (key.equals(EXTRA_PREPARE_PHASE_BACKGROUND_COLOR)) {
                    // TODO: save value
                }
                else if (key.equals(EXTRA_LIFT_PHASE_BACKGROUND_COLOR)) {
                    // TODO: save value
                }
                else if (key.equals(EXTRA_WAIT_PHASE_BACKGROUND_COLOR)) {
                    // TODO: save value
                }
                else if (key.equals(EXTRA_PREPARE_PHASE_START_ALERT_ON)) {
                    // TODO: save value
                }
                else if (key.equals(EXTRA_LIFT_PHASE_START_ALERT_ON)) {
                    // TODO: save value
                }
                else if (key.equals(EXTRA_WAIT_PHASE_START_ALERT_ON)) {
                    // TODO: save value
                }
            }
        });
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
        super.onResume();

        // initialize settings values from resource file
        initSettingsValues();
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

    // ****************************************************************************************** //

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
        // TODO: initialize summary strings
    }

    private void updatePreparePhaseLengthPreferenceSummary(int value) {

        ListPreference preparePhaseLengthPreference = (ListPreference) findPreference("prepare_phase_length_preference_key");
        try {
            preparePhaseLengthPreference.setSummary(value + " seconds"); // TODO: read unit string from strings.xml
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void updateLiftPhaseLengthPreferenceSummary(int value) {

        ListPreference liftPhaseLengthPreference = (ListPreference) findPreference("lift_phase_length_preference_key");
        try {
            liftPhaseLengthPreference.setSummary(value + " seconds"); // TODO: read unit string from strings.xml
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void updateWaitPhaseLengthPreferenceSummary(int value) {

        ListPreference waitPhaseLengthPreference = (ListPreference) findPreference("wait_phase_length_preference_key");
        try {
            waitPhaseLengthPreference.setSummary(value + " seconds"); // TODO: read unit string from strings.xml
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
