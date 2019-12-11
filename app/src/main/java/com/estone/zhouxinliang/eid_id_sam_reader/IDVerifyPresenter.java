package com.estone.zhouxinliang.eid_id_sam_reader;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.arcsoft.idcardveri.CompareResult;
import com.arcsoft.idcardveri.DetectFaceResult;
import com.arcsoft.idcardveri.IdCardVerifyError;
import com.arcsoft.idcardveri.IdCardVerifyListener;
import com.arcsoft.idcardveri.IdCardVerifyManager;
import com.yws.faceid.wcl.WclIDCard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class IDVerifyPresenter implements TextureView.SurfaceTextureListener, IdCardVerifyListener
        , PicReceiver.OnpiDateListener, WclIDCard.IDCardCallBack {
    private TextureView camTextureView;
    private SurfaceView rectSurfaceView;
    private Camera camera;
    private IDReader idReader;
    private Handler uiHandler;
    private boolean isCurrentReady = false;
    private boolean isIdCardReady = false;
    private IDVerifyActivity activity;
    final private int camId = 1;
    final private int displayOrientation = 90;
    final private double THRESHOLD = 0.70;
    public WclIDCard myCard = null;
    //    PicReceiver mPicReceiver;
    private static final String TAG = "IDVerifyPresenter";

    public IDVerifyPresenter(final IDVerifyActivity activity, WclIDCard myCard) {
        this.activity = activity;
        this.camTextureView = activity.getCamTextureView();
        this.rectSurfaceView = activity.getRectSurfaceView();
        this.uiHandler = activity.getUIHandler();
        this.idReader = new IDReader();
        this.myCard = myCard;
        //        if (mPicReceiver == null) {
        //            this.mPicReceiver = new PicReceiver();
        //            IntentFilter filter = new IntentFilter();
        //            filter.addAction("com.estone.pic");
        //            //注册广播接收
        //            activity.registerReceiver(mPicReceiver, filter);
        //    }
        //方法传入身份证图片数据进行人脸检测，若检测成功，则可得到一个最大人脸框。
        idReader.setOnIDCardDataListener(new IDReader.OnIDCardDataListener() {
            @Override
            public void OnIDCardData(byte[] data, int width, int height) {
                LUtils.i(TAG, "IDVerifyPresenter----OnIDCardData: " + data);
                uiHandler.sendEmptyMessage(IDVerifyActivity.DISPLAY_PREVIEW);
                DetectFaceResult result = IdCardVerifyManager.getInstance().inputIdCardData(data, width, height);
                LUtils.i(TAG, "OnIDCardData: " + result.getErrCode());
            }
        });
        //        mPicReceiver.setOnpiDateListener(this);

        /*****************************
         * 模拟读取到身份证*/
        //        activity.findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        ////                idReader.startReadLoop();
        //            }
        //        });
        /***************************/

        //Step1：调用IdCardVerifyManager的active方法激活设备，
        // 一个设备安装后仅需激活一次，卸载重新安装或者清除应用数据后需要重新激活。
        int ret = IdCardVerifyManager.getInstance().active(activity.getBaseContext(),
                Constans.APP_ID, Constans.SDK_KEY);

        if (ret != IdCardVerifyError.OK && ret != IdCardVerifyError.MERR_ASF_ALREADY_ACTIVATED) {
            LUtils.e(TAG, "IDVerifyPresenter: active sdk failed!" + ret); //
            return;
        }
        //方法初始化SDK，初始化成功后才能进一步使用SDK的功能。
        if (IdCardVerifyManager.getInstance().init(activity.getBaseContext(), this)
                != IdCardVerifyError.OK) {
            LUtils.e(TAG, "IDVerifyPresenter: init sdk failed!");
            return;
        }

        if (Camera.getNumberOfCameras() < 0) {
            LUtils.e(TAG, "IDVerifyPresenter: number of cameras < 0!");
            return;
        }

        camTextureView.setSurfaceTextureListener(this);
        if (this.myCard != null) {
            this.myCard.setIDCardCallback(this);
            this.myCard.startIDCardReader();
            LUtils.d(TAG, "  startIDCardReader ");
        }
    }

    void unregRec(Activity activity) {
        //        activity.unregisterReceiver(mPicReceiver);
        if (myCard != null) {
            myCard.unregisterReceiver();
        }
    }


    void start() {
        LUtils.d(TAG, "----start-----------");
        if (camTextureView.isAvailable()) {
            LUtils.d(TAG, "----start---------camTextureView.isAvailable()--" + camTextureView.isAvailable());
            startCameraPreview(camTextureView.getSurfaceTexture());
        }
        LUtils.d(TAG, "----start---------camTextureView.isAvailable()--" + camTextureView.isAvailable());
        //        if(idReader != null){
        //            idReader.startReadLoop();
        //        }
    }

    void stop() {
        releaseCamera();
        IdCardVerifyManager.getInstance().unInit();
        //        if(idReader != null){
        //            idReader.stopReadLoop();
        //        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LUtils.i(TAG, "onSurfaceTextureAvailable: ");

        startCameraPreview(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        LUtils.i(TAG, "onSurfaceTextureSizeChanged: ");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LUtils.i(TAG, "onSurfaceTextureDestroyed: ");
        releaseCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    //nPreviewResult回调获取身份证数据特征提取结果，
    @Override
    public void onPreviewResult(DetectFaceResult detectFaceResult, byte[] bytes, int i, int i1) {
        LUtils.i(TAG, "onPreviewResult: " + detectFaceResult.getErrCode());
        if (detectFaceResult.getErrCode() == IdCardVerifyError.OK) {
            isCurrentReady = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    compare();
                }
            }).start();
        } else {
            LUtils.i(TAG, "无人脸识别: ");
        }
    }

    //inputIdCardData 的 回调，若成功，且现场采集数据同样也提取成功，则可进行人证比对。
    @Override
    public void onIdCardResult(DetectFaceResult detectFaceResult, byte[] bytes, int i, int i1) {

        LUtils.i(TAG, "onIdCardResult ---回调" + detectFaceResult.getFaceRect());
        if (detectFaceResult.getErrCode() == IdCardVerifyError.OK) {
            //有无人脸
            LUtils.i(TAG, "onIdCardResult  有人脸");
            isIdCardReady = true;
        } else {
            LUtils.i(TAG, "onIdCardResult  无人脸");
        }
    }

    private void compare() {
        LUtils.i(TAG, "compare: ");
        for (int i = 0; i < 5; i++) {
            LUtils.i(TAG, "compare: isCurrentReady = " + isCurrentReady + "isIdCardReady = " + isIdCardReady);
            if (isCurrentReady && isIdCardReady) {
                break;
            }
            if (i == 4) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //人证比对
        CompareResult compareResult = IdCardVerifyManager.getInstance().compareFeature(THRESHOLD);
        LUtils.i(TAG, "compare: " + compareResult.getResult());
        if (compareResult.isSuccess()) {
            AccessoryController.ctlRelay(true);

            Message message = uiHandler.obtainMessage(IDVerifyActivity.DISPLAY_COMPARE_SUCCEED);
            Bundle bundle = new Bundle();
            bundle.putString("compareResult", compareResult.getResult() * 100 + "%");
            message.setData(bundle);//IDVerifyActivity.DISPLAY_COMPARE_SUCCEED
            uiHandler.sendEmptyMessageDelayed(IDVerifyActivity.CHANGEGPIO, 3000);
            uiHandler.sendMessage(message);
            LUtils.i(TAG, "compare: same person==" + compareResult.getResult() * 100 + "%");

        } else {
            uiHandler.sendEmptyMessage(IDVerifyActivity.DISPLAY_COMPARE_FAILED);
            LUtils.i(TAG, "compare: not same person");
        }
        isIdCardReady = false;
        isCurrentReady = false;

    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void startCameraPreview(SurfaceTexture surface) {
        //      AccessoryController.ctlLed(true, AccessoryController.LED_CAM_WHITE);
        camera = Camera.open(camId);
        camera.setDisplayOrientation(displayOrientation);
        Camera.Parameters params = camera.getParameters();
        //TODO 此处设置摄像头采集的分辨率，涉及效率问题
        final Camera.Size size = params.getPreviewSize();
        //final Camera.Size size = params.getSupportedPreviewSizes().get(10);
        //params.setPreviewSize(size.width, size.height);
        camera.setParameters(params);
        //final Camera.Size size = camera.getParameters().getPreviewSize();
        //       LUtils.i(TAG, "startCameraPreview: height = " + size.height + "width = " + size.width);

        try {
            camera.setPreviewTexture(surface);
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    DetectFaceResult result = IdCardVerifyManager.getInstance().
                            onPreviewData(data, size.width, size.height, true);
                    if (result.getErrCode() == IdCardVerifyError.OK && rectSurfaceView != null) {
                        Canvas canvas = rectSurfaceView.getHolder().lockCanvas();
                        if (canvas != null) {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            Rect rect = result.getFaceRect();
                            if (rect != null) {
                                Rect adjustedRect = DrawUtils.adjustRect(rect, size.width, size.height,
                                        canvas.getWidth(), canvas.getHeight(), displayOrientation, camId);
                                //画人脸框
                                DrawUtils.drawFaceRect(canvas, adjustedRect, Color.YELLOW, 5);
                                //                               LUtils.d(TAG," canvas.getWidth()"+ canvas.getWidth());
                                //                               LUtils.d(TAG," canvas.getHeight()"+ canvas.getHeight());
                                //                               LUtils.d(TAG,"  size.width"+  size.width);
                                //                               LUtils.d(TAG,"  size.height"+  size.height);
                            }
                            rectSurfaceView.getHolder().unlockCanvasAndPost(canvas);
                        }
                    }

                }
            });
            camera.startPreview();
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }

    }

    @Override
    public void OnIpiDateData() {
        //           AccessoryController.ctlLed(true, AccessoryController.LED_CAM_WHITE);
        //        idReader.startReadLoop();
    }

    @Override
    public void onIDCardResult2() {
        Bitmap mCardPhoto = myCard.getPhoto();

        if (mCardPhoto != null) {
            myCard.notifyFaceVerityOver();
            saveBitmap(mCardPhoto);
            //            AccessoryController.ctlLed(true, AccessoryController.LED_CAM_WHITE);
            idReader.startReadLoop();
            //            Intent intent = new Intent();
            //            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //            intent.setAction("com.estone.pic");
            //            activity.sendBroadcast(intent);
        }
    }

    @Override
    public void onIDCardMsg(final String var1) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, var1, Toast.LENGTH_LONG).show();
            }
        });

    }


    /**
     * 根据指定的宽度比例值和高度比例值进行缩放
     */
    public static Bitmap bitmapZoomByScale(Bitmap srcBitmap, float scaleWidth, float scaleHeight) {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap, 0, 0, width, height, matrix, true);
        if (bitmap != null) {
            return bitmap;
        } else {
            return srcBitmap;
        }
    }

    private void saveBitmap(Bitmap bitmap) {
        Bitmap bitmap1 = bitmapZoomByScale(bitmap, 2, 2);

        FileOutputStream out = null;
        File file = new File("/sdcard/lyps66.jpg");
        try {
            out = new FileOutputStream(file);

            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, out);
            System.out.println("___________保存的__sd___下_______________________");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

