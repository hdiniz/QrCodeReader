package com.example.qrcodereader.camera;


import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CameraPreviewCallback implements Camera.PreviewCallback {

    public static final int PREVIEW_FRAME = 1;

    private static final String TAG = "CameraPreviewCallback";

    private Handler mHandler;

    public CameraPreviewCallback() {
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, "onPreviewFrame");
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        Message reply = Message.obtain(mHandler, PREVIEW_FRAME, previewSize.width, previewSize.height, data);
        reply.sendToTarget();
    }
}
