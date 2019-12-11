package com.estone.zhouxinliang.eid_id_sam_reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.YuvImage;
import android.util.Log;
import android.view.View;


import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class IDReader {
    private static final String TAG = "IDReader";
    private OnIDCardDataListener listener;
    private Thread readLoopThread;
    private boolean isReading = false;
    Timer timer;

    public IDReader() {

    }

    public int startReadLoop() {
       LUtils.i(TAG, "startReadLoop: ");
        //        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/zhou.jpg");
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/lyps66.jpg");
        final byte data[] = getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);
       LUtils.i(TAG, "startReadLoop: " + data + "==bitmap.getHeight()" + bitmap.getHeight() + ",==bitmap.getHeight()=" + bitmap.getHeight());
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        bitmap.recycle();
        isReading = true;
        //        timer = new Timer();
        //        TimerTask tt = new TimerTask() {
        //            @Override
        //            public void run() {
        //               LUtils.i(TAG, "run: tt onIDCardData");
        //                if(listener != null){
        //                    listener.OnIDCardData(data, width, height);
        //                }
        //            }
        //        };
        //        timer.scheduleAtFixedRate(tt, 0, 500);
        listener.OnIDCardData(data, width, height);
        return 0;
    }

    public int stopReadLoop() {
        if (timer != null) {
            timer.cancel();
        }
        isReading = false;
        return 0;
    }

    byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                index++;
            }
        }
    }


    public void setOnIDCardDataListener(OnIDCardDataListener listener) {
        this.listener = listener;
    }

    interface OnIDCardDataListener {
        void OnIDCardData(byte[] data, int width, int height);
    }
}
