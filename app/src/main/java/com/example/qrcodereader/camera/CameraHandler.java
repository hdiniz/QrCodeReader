package com.example.qrcodereader.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraHandler extends Handler {

    private static final String TAG = "CameraHandler";

    public static final int ACQUIRE_CAMERA = 1;
    public static final int RELEASE_CAMERA = 2;
    public static final int START_PREVIEW = 3;
    public static final int STOP_PREVIEW = 4;
    public static final int STOP_PREVIEW_RELEASE_CAMERA = 5;
    public static final int REQUEST_ONE_SHOT_PREVIEW = 6;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CameraAutoFocus mAutoFocus;
    private boolean mPreviewStarted = false;

    private CameraPreviewCallback mPreviewCallback;

    public CameraHandler(SurfaceHolder mHolder) {
        this.mHolder = mHolder;
        mPreviewCallback = new CameraPreviewCallback();
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        Log.d(TAG, "msg.what="+msg.what);
        switch (what) {
            case ACQUIRE_CAMERA:
                acquireCamera();
                break;
            case RELEASE_CAMERA:
                releaseCamera();
                break;
            case START_PREVIEW:
                startPreview();
                break;
            case STOP_PREVIEW:
                stopPreview();
                break;
            case STOP_PREVIEW_RELEASE_CAMERA:
                releaseCamera();
                break;
            case REQUEST_ONE_SHOT_PREVIEW:
                requestOneShotPreviewFrame((Handler)msg.obj);
                break;
            default:
        }
        super.handleMessage(msg);
    }

    public void acquireCamera() {
        Log.d(TAG, "acquireCamera()");
        if (mCamera == null) {
            mCamera = CameraUtils.open(CameraUtils.ANY_CAMERA_ID);
        }
        if (mCamera == null) {
            Log.d(TAG, "Failed to open camera");
            return;
        }
        mAutoFocus = new CameraAutoFocus(mCamera);

        //TODO choose best preview size
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        Log.d(TAG, "previewSize={w:"+previewSize.width+",h:"+previewSize.height+"}");
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        /*for (Camera.Size size : sizes) {
            Log.d(TAG, "supportedPreviewSize={w:"+size.width+",h:"+size.height+"}");
        }*/
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.d(TAG, "Failed to set preview display");
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        Log.d(TAG, "releaseCamera()");
        if (mPreviewStarted) {
            stopPreview();
        }

        if (null == mCamera) {
            return;
        }
        mCamera.release();
        mCamera = null;
    }

    private void startPreview() {
        Log.d(TAG, "startPreview()");
        mCamera.startPreview();
        mAutoFocus.start();
        mPreviewStarted = true;
    }

    private void stopPreview() {
        Log.d(TAG, "stopPreview()");
        if (null == mCamera) {
            return;
        }
        mAutoFocus.stop();
        mCamera.stopPreview();
        mPreviewStarted = false;
    }

    public void requestOneShotPreviewFrame(Handler handler) {
        if (mCamera == null) return;
        mPreviewCallback.setHandler(handler);
        mCamera.setOneShotPreviewCallback(mPreviewCallback);
    }
}
