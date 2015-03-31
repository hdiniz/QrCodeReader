package com.example.qrcodereader.camera;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.qrcodereader.decoder.PreviewDecoder;
import com.google.zxing.Result;

import java.lang.ref.WeakReference;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";
    private final CameraHandler mCameraHandler;

    private SurfaceHolder mHolder;



    private PreviewDecoderHandler mPreviewDecoderHandler =
            new PreviewDecoderHandler(new WeakReference<CameraPreview>(this));

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mCameraHandler = new CameraHandler(mHolder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        Message msg = new Message();
        msg.what = CameraHandler.ACQUIRE_CAMERA;
        mCameraHandler.sendMessage(msg);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        Message msg = new Message();
        msg.what = CameraHandler.START_PREVIEW;
        mCameraHandler.sendMessage(msg);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        mHolder.removeCallback(this);
        Message.obtain(mCameraHandler, CameraHandler.STOP_PREVIEW_RELEASE_CAMERA).sendToTarget();
    }

    public void requestPreviewSurface() {
        Message msg = Message.obtain(mCameraHandler,
                CameraHandler.REQUEST_ONE_SHOT_PREVIEW,
                new PreviewDecoder(mPreviewDecoderHandler));
        msg.sendToTarget();
    }

    private void handleDecodeSuccess(Result rawResult) {
        Toast.makeText(getContext(), rawResult.toString(), Toast.LENGTH_LONG).show();
    }

    private static class PreviewDecoderHandler extends Handler {

        private WeakReference<CameraPreview> mWeakReference;

        private PreviewDecoderHandler(WeakReference<CameraPreview> mWeekReference) {
            this.mWeakReference = mWeekReference;
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "PreviewDecoderHandler.handleMessage");
            switch (msg.what) {
                case PreviewDecoder.FRAME_DECODE_FAIL:
                    mWeakReference.get().requestPreviewSurface();
                    break;
                case PreviewDecoder.FRAME_DECODE_SUCCESS:
                    mWeakReference.get().handleDecodeSuccess((Result)msg.obj);
                    break;
                default:
            }
        }
    }
}
