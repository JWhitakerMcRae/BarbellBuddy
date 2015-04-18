package com.mcraesolutions.watchfacelibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by jwhitakermcrae on 2/9/15.
 */
public class WatchfaceLayout extends FrameLayout {

    private static final String TAG = "WatchfaceLayout";

    // context
    private Context mContext;

    // constants
    private final int VIBRATION_DURATION = getResources().getInteger(R.integer.vibration_duration);

    // set phase lengths (ms) -- set from settings (phone)
    private int mPreparePhaseLength_ms = getResources().getInteger(R.integer.prepare_phase_length_ms);
    private int mLiftPhaseLength_ms = getResources().getInteger(R.integer.lift_phase_length_ms);
    private int mWaitPhaseLength_ms = getResources().getInteger(R.integer.wait_phase_length_ms);

    // set phase background colors -- set from settings (phone)
    private int mPreparePhaseBackgroundColor = getResources().getColor(R.color.prepare_background_color);
    private int mLiftPhaseBackgroundColor = getResources().getColor(R.color.lift_background_color);
    private int mWaitPhaseBackgroundColor = getResources().getColor(R.color.wait_background_color);

    // set phase alert on/off status -- set from settings (phone)
    private boolean mPreparePhaseStartAlertOn = getResources().getBoolean(R.bool.prepare_phase_start_alert_on);
    private boolean mLiftPhaseStartAlertOn = getResources().getBoolean(R.bool.lift_phase_start_alert_on);
    private boolean mWaitPhaseStartAlertOn = getResources().getBoolean(R.bool.wait_phase_start_alert_on);

    // set phase enumeration, including built in incrementer (with loopback)
    public enum SetPhaseEnum {
        PREPARE, // preparing for next set (getting into position)
        LIFT, // LIFTING!!!
        WAIT { // waiting in between sets
            @Override
            public SetPhaseEnum next() {
                return values()[0];
            }
        };

        public SetPhaseEnum next() {
            return values()[ordinal() + 1];
        }
    }

    // current set phase
    SetPhaseEnum mCurrentSetPhase = SetPhaseEnum.WAIT; // start in WAIT phase (walking into gym...)

    // current chronometer status
    boolean mChronometerRunning = false;
    long mChronometerBase = 0; // base difference value, used to correctly update chrono base for start/stop

    // view objects
    ImageView mSetTimerBackground;
    ProgressBar mSetTimerVisual;
    TextView mSetTimerTitleText;
    Chronometer mSetTimerChrono;

    // chronometer on tick listener
    Chronometer.OnChronometerTickListener mOnChronoTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "mOnChronoTickListener.onChronmeterTick");
            }

            // run current set phase to update UI
            switch (mCurrentSetPhase) {
                case WAIT:
                    runWait(getCurrentSetPhaseElapsedTime_ms());
                    break;
                case PREPARE:
                    runPrepare(getCurrentSetPhaseElapsedTime_ms());
                    break;
                case LIFT:
                    runLift(getCurrentSetPhaseElapsedTime_ms());
                    break;
            }
        }
    };

    // reset on click listener
    View.OnClickListener mOnResetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "mOnResetListener.onClick");
            }

            // reset current set phase
            if (mCurrentSetPhase == SetPhaseEnum.WAIT && getCurrentSetPhaseElapsedTime_ms() < 500) { // TODO: detect double tap better???
                setCurrentSetPhase(SetPhaseEnum.PREPARE);
            } else { // single tap
                setCurrentSetPhase(SetPhaseEnum.WAIT);
            }

            // reset chronometer
            resetChronometer();
        }
    };

    public WatchfaceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        // save context
        mContext = context;

        // inflate layout from XML
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.watchface_layout, this);

        // initialize view objects
        initViewObjects();

        // update current set phase UI
        updateCurrentSetPhaseUi();
    }

    private void initViewObjects() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "initViewObjects");
        }

        // find view objects
        mSetTimerBackground = (ImageView) findViewById(R.id.imageView_setTimerBackground);
        mSetTimerVisual = (ProgressBar) findViewById(R.id.progressBar_setTimerVisual);
        mSetTimerTitleText= (TextView) findViewById(R.id.textView_setTimerTitleText);
        mSetTimerChrono = (Chronometer) findViewById(R.id.chronometer_setTimerChrono);

        // set on click listeners to reset on touch of background (other views should be non-clickable)
        mSetTimerBackground.setOnClickListener(mOnResetListener);

        // set on tick listener as chrono tick driven callback for run functions
        mSetTimerChrono.setOnChronometerTickListener(mOnChronoTickListener);
    }

    private void initUiForCurrentSetPhase() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "initViewObjects");
        }

        // run current set phase to initialize UI
        switch (mCurrentSetPhase) {
            case PREPARE:
                runPrepare(getCurrentSetPhaseElapsedTime_ms());
                break;
            case LIFT:
                runLift(getCurrentSetPhaseElapsedTime_ms());
                break;
            case WAIT:
                runWait(getCurrentSetPhaseElapsedTime_ms());
                break;
        }
    }

    /**
     * Run PREPARE set phase.
     * <p/>
     * The timer view is updated and end of phase is checked. If the phase is
     * over the next phase is begun.
     * <p/>
     * This should be called on each chronometer tick whenever the current phase
     * is PREPARE.
     *
     * @param elapsedTime
     */
    void runPrepare(long elapsedTime) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "runPrepare: " + elapsedTime);
        }

        // update set timer visual view
        updateSetTimerVisualUi(elapsedTime);

        // at end of set phase phase go to next set phase
        if (elapsedTime >= mPreparePhaseLength_ms) {
            nextSetPhase();
        }
    }

    /**
     * Run LIFT set phase.
     * <p/>
     * The timer view is updated and end of phase is checked. If the phase is
     * over the next phase is begun.
     * <p/>
     * This should be called on each chronometer tick whenever the current phase
     * is LIFT.
     *
     * @param elapsedTime
     */
    void runLift(long elapsedTime) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "runLift: " + elapsedTime);
        }

        // update set timer visual view
        updateSetTimerVisualUi(elapsedTime);

        // at end of set phase phase go to next set phase
        if (elapsedTime >= mLiftPhaseLength_ms) {
            nextSetPhase();
        }
    }

    /**
     * Run WAIT set phase.
     * <p/>
     * The timer view is updated and end of phase is checked. If the phase is
     * over the next phase is begun and alerts issued.
     * <p/>
     * This should be called on each chronometer tick whenever the current phase
     * is WAIT.
     *
     * @param elapsedTime
     */
    void runWait(long elapsedTime) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "runWait: " + elapsedTime);
        }

        // update set timer visual view
        updateSetTimerVisualUi(elapsedTime);

        // at end of set phase phase go to next set phase
        if (elapsedTime >= mWaitPhaseLength_ms) {
            nextSetPhase();
        }
    }

    /**
     * Update the set timer view.
     */
    protected void updateSetTimerVisualUi(long elapsedTime) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "updateSetTimerVisualUi: " + elapsedTime);
        }

        // update progress bar
        int progress = 0;
        switch (mCurrentSetPhase) {
            case PREPARE:
                progress = (int) (100f * (float) elapsedTime / (float) mPreparePhaseLength_ms);
                break;
            case LIFT:
                progress = (int) (100f * (float) elapsedTime / (float) mLiftPhaseLength_ms);
                break;
            case WAIT:
                progress = (int) (100f * (float) elapsedTime / (float) mWaitPhaseLength_ms);
                break;
        }
        mSetTimerVisual.setProgress(progress);
    }

    /**
     * Update current set phase UI, which includes the set timer as well as
     * accompanying set timer text.
     * <p/>
     * This should be called whenever the set phase changes.
     */
    void updateCurrentSetPhaseUi() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "updateCurrentSetPhaseUi");
        }

        // update views
        switch (mCurrentSetPhase) {
            case PREPARE:
                mSetTimerBackground.setImageBitmap(createBackground());
                mSetTimerTitleText.setText(getResources().getString(R.string.prepare_phase_title));
                break;
            case LIFT:
                mSetTimerBackground.setImageBitmap(createBackground());
                mSetTimerTitleText.setText(getResources().getString(R.string.lift_phase_title));
                break;
            case WAIT:
                mSetTimerBackground.setImageBitmap(createBackground());
                mSetTimerTitleText.setText(getResources().getString(R.string.wait_phase_title));
                break;
        }
    }

    void issueCurrentSetPhaseAlerts() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "issueCurrentSetPhaseAlerts");
        }

        // issue alerts
        switch (mCurrentSetPhase) {
            case PREPARE:
                if (mPreparePhaseStartAlertOn) {
                    final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, VIBRATION_DURATION};
                    vibrator.vibrate(pattern, -1);
                }
                break;
            case LIFT:
                if (mLiftPhaseStartAlertOn) {
                    final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, VIBRATION_DURATION};
                    vibrator.vibrate(pattern, -1);
                }
                break;
            case WAIT:
                if (mWaitPhaseStartAlertOn) {
                    final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, VIBRATION_DURATION};
                    vibrator.vibrate(pattern, -1);
                }
                break;
        }
    }

    /**
     * Creates and returns a Bitmap object intended to be set to the background ImageView object.
     *
     * @return background Bitmap object based on the current set phase
     */
    private Bitmap createBackground() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "createBackground");
        }

        // TODO: make only watchface circle clickable, not watchface background

        // get screen size
        int width = 1080; // TODO: correctly get available space in a way that works on all devices
        int height = 1080; // TODO: correctly get available space in a way that works on all devices

        // create a mutable bitmap
        Bitmap bitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitMap = bitMap.copy(bitMap.getConfig(), true); // make mutable copy

        // construct a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(bitMap);

        // create a new paint with default settings.
        Paint paint = new Paint();

        // smooths out the edges of what is being drawn
        paint.setAntiAlias(true);

        // set color
        switch (mCurrentSetPhase) {
            case WAIT:
                paint.setColor(mWaitPhaseBackgroundColor);
                break;
            case PREPARE:
                paint.setColor(mPreparePhaseBackgroundColor);
                break;
            case LIFT:
                paint.setColor(mLiftPhaseBackgroundColor);
                break;
        }

        // set style
        paint.setStyle(Paint.Style.FILL);

        // draw circle with radius width/2
        canvas.drawCircle(width/2, height/2, width/2, paint);

        // return Bitmap object
        return bitMap;
    }

    //
    // watchface API calls
    //

    public void nextSetPhase() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "nextSetPhase");
        }

        setCurrentSetPhase(mCurrentSetPhase.next());

        // issue alerts for current set phase
        issueCurrentSetPhaseAlerts();

        // restart set timer for new set phase
        resetChronometer(); // NOTE: this results in call to updateSetTimerVisualUi()
    }

    public void prepareSetPhase() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "prepareSetPhase");
        }

        setCurrentSetPhase(SetPhaseEnum.PREPARE);

        // issue alerts for current set phase
        issueCurrentSetPhaseAlerts();

        // restart set timer for new set phase
        resetChronometer(); // NOTE: this results in call to updateSetTimerVisualUi()
    }

    public void liftSetPhase() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "liftSetPhase");
        }

        setCurrentSetPhase(SetPhaseEnum.PREPARE);

        // issue alerts for current set phase
        issueCurrentSetPhaseAlerts();

        // restart set timer for new set phase
        resetChronometer(); // NOTE: this results in call to updateSetTimerVisualUi()
    }

    public void waitSetPhase() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "waitSetPhase");
        }

        setCurrentSetPhase(SetPhaseEnum.PREPARE);

        // issue alerts for current set phase
        issueCurrentSetPhaseAlerts();

        // restart set timer for new set phase
        resetChronometer();
    }

    /**
     * Start the chronometer.
     */
    public void startChronometer() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "startChronometer");
        }

        if (!mChronometerRunning) {
            mSetTimerChrono.setBase(SystemClock.elapsedRealtime() - mChronometerBase);
        }
        mSetTimerChrono.start();
        mChronometerRunning = true;
    }

    /**
     * Stop the chronometer.
     */
    public void stopChronometer() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "stopChronometer");
        }

        if (mChronometerRunning) { // stopping chrono, save base difference
            mChronometerBase = SystemClock.elapsedRealtime() - mSetTimerChrono.getBase();
        }
        mSetTimerChrono.stop();
        mChronometerRunning = false;
    }

    /**
     * Reset the chronometer.
     */
    public void resetChronometer() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "resetChronometer");
        }
        mSetTimerChrono.setBase(SystemClock.elapsedRealtime());
    }

    public boolean isChronometerRunning() {
        return mChronometerRunning;
    }

    //
    // expose getters/setters for settings data, current set phase, chrono control
    //

    public int getPreparePhaseLength_ms() {
        return mPreparePhaseLength_ms;
    }

    public void setPreparePhaseLength_ms(int preparePhaseLength_ms) {
        mPreparePhaseLength_ms = preparePhaseLength_ms;
        if (mCurrentSetPhase == SetPhaseEnum.PREPARE) { // update current display
            updateCurrentSetPhaseUi();
        }
    }

    public int getLiftPhaseLength_ms() {
        return mLiftPhaseLength_ms;
    }

    public void setLiftPhaseLength_ms(int liftPhaseLength_ms) {
        mLiftPhaseLength_ms = liftPhaseLength_ms;
        if (mCurrentSetPhase == SetPhaseEnum.LIFT) { // update current display
            updateCurrentSetPhaseUi();
        }
    }

    public int getWaitPhaseLength_ms() {
        return mWaitPhaseLength_ms;
    }

    public void setWaitPhaseLength_ms(int waitPhaseLength_ms) {
        mWaitPhaseLength_ms = waitPhaseLength_ms;
        if (mCurrentSetPhase == SetPhaseEnum.WAIT) { // update current display
            updateCurrentSetPhaseUi();
        }
    }

    public int getPreparePhaseBackgroundColor() {
        return mPreparePhaseBackgroundColor;
    }

    public void setPreparePhaseBackgroundColor(int preparePhaseBackgroundColor) {
        mPreparePhaseBackgroundColor = preparePhaseBackgroundColor;
        if (mCurrentSetPhase == SetPhaseEnum.PREPARE) { // update current display
            mSetTimerBackground.setImageBitmap(createBackground());
        }
    }

    public int getLiftPhaseBackgroundColor() {
        return mLiftPhaseBackgroundColor;
    }

    public void setLiftPhaseBackgroundColor(int liftPhaseBackgroundColor) {
        mLiftPhaseBackgroundColor = liftPhaseBackgroundColor;
        if (mCurrentSetPhase == SetPhaseEnum.LIFT) { // update current display
            mSetTimerBackground.setImageBitmap(createBackground());
        }
    }

    public int getWaitPhaseBackgroundColor() {
        return mWaitPhaseBackgroundColor;
    }

    public void setWaitPhaseBackgroundColor(int waitPhaseBackgroundColor) {
        mWaitPhaseBackgroundColor = waitPhaseBackgroundColor;
        if (mCurrentSetPhase == SetPhaseEnum.WAIT) { // update current display
            mSetTimerBackground.setImageBitmap(createBackground());
        }
    }

    public boolean isPreparePhaseStartAlertOn() {
        return mPreparePhaseStartAlertOn;
    }

    public void setPreparePhaseStartAlertOn(boolean preparePhaseStartAlertOn) {
        mPreparePhaseStartAlertOn = preparePhaseStartAlertOn;
    }

    public boolean isLiftPhaseStartAlertOn() {
        return mLiftPhaseStartAlertOn;
    }

    public void setLiftPhaseStartAlertOn(boolean liftPhaseStartAlertOn) {
        mLiftPhaseStartAlertOn = liftPhaseStartAlertOn;
    }

    public boolean isWaitPhaseStartAlertOn() {
        return mWaitPhaseStartAlertOn;
    }

    public void setWaitPhaseStartAlertOn(boolean waitPhaseStartAlertOn) {
        mWaitPhaseStartAlertOn = waitPhaseStartAlertOn;
    }

    public SetPhaseEnum getCurrentSetPhase() {
        return mCurrentSetPhase;
    }

    public void setCurrentSetPhase(SetPhaseEnum currentSetPhase) {
        mCurrentSetPhase = currentSetPhase;

        // update current set phase UI
        updateCurrentSetPhaseUi();
    }

    public long getCurrentSetPhaseElapsedTime_ms() {
        return SystemClock.elapsedRealtime() - mSetTimerChrono.getBase();
    }
}
