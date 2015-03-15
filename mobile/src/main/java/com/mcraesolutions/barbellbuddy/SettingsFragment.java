package com.mcraesolutions.barbellbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

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

                if (key.equals("prepare_phase_length_preference_key")) {
                    // TODO: save value
                    updatePreparePhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString("prepare_phase_length_key", "0"))); // TODO: fix default value
                }
                else if (key.equals("prepare_phase_vibrate_on_preference_key")) {
                    // TODO: save value
                }
                else if (key.equals("lift_phase_length_preference_key")) {
                    // TODO: save value
                    updateLiftPhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString("prepare_phase_length_key", "0"))); // TODO: fix default value
                }
                else if (key.equals("lift_phase_vibrate_on_preference_key")) {
                    // TODO: save value
                }
                else if (key.equals("wait_phase_length_preference_key")) {
                    // TODO: save value
                    updateWaitPhaseLengthPreferenceSummary(Integer.parseInt(sharedPreferences.getString("prepare_phase_length_key", "0"))); // TODO: fix default value
                }
                else if (key.equals("wait_phase_vibrate_on_preference_key")) {
                    // TODO: save value
                }
            }
        });
    }

    // onCreateView

    // onActivityCreated

    // onStart

    @Override
    public void onResume() {
        super.onResume();

        // initialize settings values
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
        // TODO: read settings to class variables
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
