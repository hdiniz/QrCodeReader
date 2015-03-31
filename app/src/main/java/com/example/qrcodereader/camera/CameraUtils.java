package com.example.qrcodereader.camera;

import android.hardware.Camera;
import android.util.Log;

public class CameraUtils {

    private static final String TAG = "CameraUtils";
    public static final int ANY_CAMERA_ID = -1;

    public static Camera open(int id) {
        Camera camera = null;
        int cameraId = 0;

        int nroCameras = Camera.getNumberOfCameras();

        if (id == ANY_CAMERA_ID) {
            for (int i = 0; i < nroCameras; i++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraId = i;
                }
            }
        } else {
            if (id > nroCameras) {
                Log.d(TAG, "requested id(" + id + ") does not exist");
                return null;
            }
            cameraId = id;
        }

        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            Log.d(TAG, "failed to open camera: " + cameraId);
            e.printStackTrace();
        }

        return camera;
    }

}
