package com.estone.zhouxinliang.eid_id_sam_reader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yws.faceid.wcl.WclIDCard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IDVerifyActivity extends AppCompatActivity{
//        implements WclIDCard.IDCardCallBack {
    private static final String TAG = "IDVerifyActivity";
    final static public int DISPLAY_PREVIEW = 0x1000;
    final static public int DISPLAY_WELCOME = 0x1004;
    final static public int DISPLAY_COMPARE_FAILED = 0x1001;
    final static public int DISPLAY_COMPARE_SUCCEED = 0x1002;
    final static public int DISPLAY_NO_MONEY = 0x1003;
    final static public int CHANGEGPIO = 0x1008;
    private TextureView camTextureView;
    private SurfaceView rectSurfaceView;
    private ConstraintLayout welcomeLayout;
    private ConstraintLayout informationLayout;
    private TextView resultTextView;


  public WclIDCard myCard = null;
    IDVerifyPresenter idVerifyPresenter;

    Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DISPLAY_PREVIEW:
                    showPreview();
                    break;
                case CHANGEGPIO:
                    AccessoryController.ctlRelay(false);
                    AccessoryController.ctlLed(false, AccessoryController.LED_CAM_WHITE);
                   LUtils.d(TAG, "  AccessoryController.ctlRelay(false);");
                    break;
                case DISPLAY_WELCOME:
                    showWelcome();
                    break;
                case DISPLAY_COMPARE_FAILED:
                    AccessoryController.ctlLed(false, AccessoryController.LED_CAM_WHITE);
                    Toast.makeText(IDVerifyActivity.this,
                            "比对失败", Toast.LENGTH_LONG).show();
                    showResult(DISPLAY_COMPARE_FAILED);

                    uiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showWelcome();
                        }
                    }, 0);
                    break;
                case DISPLAY_NO_MONEY:
                    Toast.makeText(IDVerifyActivity.this,
                            "not enough money", Toast.LENGTH_LONG).show();
                    break;
                case DISPLAY_COMPARE_SUCCEED:
                    Bundle data = msg.getData();
                    Toast.makeText(IDVerifyActivity.this,
                            "比对成功", Toast.LENGTH_LONG).show();
                    showResult(DISPLAY_COMPARE_SUCCEED);
                    uiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            showWelcome();
                        }
                    }, 3000);
                    break;
            }
            return true;
        }
    });

    public TextureView getCamTextureView() {
        return camTextureView;
    }

    public SurfaceView getRectSurfaceView() {
        return rectSurfaceView;
    }

    public Handler getUIHandler() {
        return uiHandler;
    }

    void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       LUtils.i(TAG, "onCreate: ");
        requestPermissions(this);
        setContentView(R.layout.activity_idverify);
    myCard = new WclIDCard(this);
        welcomeLayout = findViewById(R.id.layout_welcome);
        informationLayout = findViewById(R.id.layout_infomation);
        resultTextView = findViewById(R.id.textView_result);
        camTextureView = findViewById(R.id.textureView_cam);
        rectSurfaceView = findViewById(R.id.surfaceView_rect);
        rectSurfaceView.setZOrderOnTop(true);
        rectSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        Toast.makeText(this, "识别功能启动", Toast.LENGTH_LONG).show();
        if (idVerifyPresenter == null) {
            idVerifyPresenter = new IDVerifyPresenter(this,myCard);
        } else {
           LUtils.i(TAG, "idVerifyPresenter!=null  ");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myCard != null) {
            myCard.setIDCardCallback(null);
            myCard.closeIDCard();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
       LUtils.d(TAG, "onStart ");

    }

    @Override
    protected void onPause() {
        idVerifyPresenter.stop();
        idVerifyPresenter.unregRec(this);
        super.onPause();
    }


    private void showWelcome() {
        welcomeLayout.setVisibility(View.VISIBLE);
        informationLayout.setVisibility(View.INVISIBLE);

    }

    private void showPreview() {
        welcomeLayout.setVisibility(View.INVISIBLE);
        informationLayout.setVisibility(View.INVISIBLE);
    }

    private void showResult(int result) {
        welcomeLayout.setVisibility(View.INVISIBLE);
        informationLayout.setVisibility(View.VISIBLE);
        switch (result) {
            case DISPLAY_COMPARE_FAILED:
                resultTextView.setText(R.string.compare_fail);
                break;
            case DISPLAY_COMPARE_SUCCEED:
                resultTextView.setText(R.string.compare_OK);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
