package com.mcraesolutions.barbellbuddy;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mcraesolutions.watchfacelibrary.WatchfaceLayout;


public class SetTimerActivity extends ActionBarActivity
        implements SetTimerFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener, SettingsFragment.ActivityCallbackInterface, HelpFragment.OnFragmentInteractionListener {

    private static final String TAG = "BarbellBuddy";

    // fragments to control
    private SetTimerFragment mSetTimerFragment; // initialized by initFragments()
    private SettingsFragment mSettingsFragment; // initialized by initFragments()
    private HelpFragment mHelpFragment; // initialized by initFragments()

    // Google API client
    GoogleApiClient mGoogleApiClient;
    boolean mGoogleApiClientConnected = false;

    // ****************************************************************************************** //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onCreate");
        //}
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_timer);

        // initialize fragments
        initFragments();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mSetTimerFragment)
                    .commit();
        }
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
    protected void onStart() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onStart");
        //}
        super.onStart();

        // initialize Google API client
        initGoogleApi();
    }

    // onResume

    // onFreeze

    // onPause

    @Override
    protected void onStop() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onStop");
        //}
        super.onStop();

        // disconnect Google API Client
        disconnectGoogleApi();
    }

    // onDestroy

    // ****************************************************************************************** //

    // interface callback
    public void onFragmentInteraction(Uri uri) {
        // TODO: something here?
    }

    // ****************************************************************************************** //

    // getters/setters

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    // ****************************************************************************************** //

    @Override
    public void onBackPressed() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "onBackPressed");
        //}

        if (getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
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

    // ****************************************************************************************** //

    private void initFragments() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "initFragments");
        //}

        mSetTimerFragment = new SetTimerFragment(); // TODO: have each fragment track its own singleton
        mSettingsFragment = new SettingsFragment(); // TODO: have each fragment track its own singleton
        mHelpFragment = new HelpFragment(); // TODO: have each fragment track its own singleton
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

        // add on top of SetTimerFragment (rather than replace)
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mSettingsFragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    private boolean openHelp() {
        //if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "openHelp");
        //}

        // add on top of SetTimerFragment (rather than replace)
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mHelpFragment)
                .addToBackStack(null)
                .commit();
        return true;
    }
}
