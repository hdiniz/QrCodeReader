package com.example.qrcodereader.camera;


import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

public class CameraAutoFocus implements Camera.AutoFocusCallback {

    private static final String TAG = "CameraAutoFocus";

    private Camera mCamera;

    private AutoFocusAgainTask mOngoingTask;
    private boolean mStopped = false;

    private boolean mSupportContinuousFocus = false;

    public CameraAutoFocus(Camera mCamera) {
        this.mCamera = mCamera;
        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters.getSupportedFocusModes()
                .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            Log.d(TAG, "continuous auto focus supported");
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            mSupportContinuousFocus = true;
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.d(TAG, "onAutoFocus");
        if (!mSupportContinuousFocus) {
            autoFocusAgainLater();
        }
    }

    public synchronized void start() {
        Log.d(TAG, "start()");
        if (!mSupportContinuousFocus) {
            mCamera.autoFocus(this);
        }
        mOngoingTask = null;
        mStopped = false;
    }

    public synchronized void stop() {
        Log.d(TAG, "stop()");
        if (!mSupportContinuousFocus) {
            mCamera.cancelAutoFocus();
        }
        if (mOngoingTask != null &&
                mOngoingTask.getStatus() != AsyncTask.Status.FINISHED) {
            mOngoingTask.cancel(true);
        }
        mStopped = true;
        mOngoingTask = null;
    }

    private synchronized void autoFocusAgainLater() {
        Log.d(TAG, "autoFocusAgainLater()");
        if (!mStopped && mOngoingTask == null) {
            Log.d(TAG, "starting new AutoFocusAgainTask");
            AutoFocusAgainTask task = new AutoFocusAgainTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mOngoingTask = task;
        }
    }

    private class AutoFocusAgainTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted AutoFocusAgainTask");
            }
            start();
            return null;
        }
    }
}
