package com.example.qrcodereader.decoder;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.qrcodereader.camera.CameraPreviewCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class PreviewDecoder extends Handler {

    private static final String TAG = "PreviewDecoder";

    public static final int FRAME_DECODE_FAIL = 1;
    public static final int FRAME_DECODE_SUCCESS = 2;

    private final Map<DecodeHintType,Object> hints;

    private Handler mHandler;

    public PreviewDecoder(Handler mHandler) {
        this.mHandler = mHandler;

        Collection<BarcodeFormat> decodeFormats;
        decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.QR_CODE));

        hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CameraPreviewCallback.PREVIEW_FRAME:
                decode((byte[])msg.obj, msg.arg1, msg.arg2);
                break;
            default:
        }
    }

    synchronized private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode in " + (end - start) + " ms");
            Message result = Message.obtain(mHandler, FRAME_DECODE_SUCCESS);
            result.obj = rawResult;
            result.sendToTarget();

        } else {
            Message.obtain(mHandler, FRAME_DECODE_FAIL).sendToTarget();

        }
    }

}
