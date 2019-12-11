package com.yws.faceid.wcl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.estone.zhouxinliang.eid_id_sam_reader.LUtils;
import com.yws.faceid.wcl.contant.InstructContant;
import com.zkteco.android.IDReader.IDPhotoHelper;

import java.io.UnsupportedEncodingException;

/**
 * Created by wangcl on 2019/3/21.
 */

public class WclIDCard implements USBHelper.onUsbPermissionListener, USBHelper.OnSendData2DeviceListener {
    private final String TAG = "WclIDCard";
    private boolean m_bOpen = false;
    private String m_Name;
    private String m_Sex;
    private String m_Addr;
    private String m_Birth;
    private String m_ID = "";
    private String m_Depart;
    private String m_Nation;
    private String m_ValidTime;
    private Bitmap m_Photo;
    private boolean haspermission = false;
    private boolean m_bStoped = false;
    private Context m_Context = null;
    private final String ACTION_USB_PERMISSION = "com.example.scarx.idcardreader.USB_PERMISSION";
    public WclIDCard.IDCardCallBack m_Callback = null;
    private String m_Msg;
//    private String packageNames = "com.estone.zhouxinliang.eid_id_sam_reader.IDVerifyActivity";
//    private String packages = "com.estone.zhouxinliang.eid_id_sam_reader";

    public WclIDCard(Context var1 ) {
        this.m_Context = var1;
        this.RequestUsbPermission(var1);
    }

    public boolean closeIDCard() {
      LUtils.e(TAG, "closeIDCard");
        if (this.m_bOpen) {
            this.m_bOpen = false;
//            USBHelper.getInstance(m_Context).closeConn();
        }
        stopIDCarder();
       LUtils.d(TAG,"close read loop success");

        return true;
    }

    public void unregisterReceiver() {
     LUtils.e(TAG, "unregisterReceiver");
        USBHelper.getInstance(m_Context).closeConn();
        USBHelper.getInstance(m_Context).unregisterReceiver();
    }

    public void stopIDCarder() {
        m_bStoped = true;
    }


    public synchronized boolean startIDCardReader() {
       LUtils.i(TAG, "IDCARD start... " + m_bOpen + "  " + haspermission);
        if (m_bOpen) {
           LUtils.i(TAG, " connect success ");
            return false;
        } else {
           LUtils.i(TAG, " connect   ");
            this.m_bStoped = false;
            this.m_bOpen = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        USBHelper.getInstance(m_Context).sendData(InstructContant.CHECK_CARD_TYPE, WclIDCard.this);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                       LUtils.d(TAG,"0000000000000000000");
                    }
                }
            }).start();
        }

        return true;
    }


    public String getName() {
        return this.m_Name;
    }

    public String getSex() {
        return this.m_Sex;
    }

    public String getAddr() {
        return this.m_Addr;
    }

    public String getBirth() {
        return this.m_Birth;
    }

    public String getID() {
        return this.m_ID;
    }

    public String getDepart() {
        return this.m_Depart;
    }

    public String getNation() {
        return this.m_Nation;
    }

    public String getValidTime() {
        return this.m_ValidTime;
    }

    public Bitmap getPhoto() {
        return this.m_Photo;
    }

    private void RequestUsbPermission(Context var1) {

      LUtils.e(TAG, " will  to  RequestUsbPermission");
        USBHelper.getInstance(m_Context).requestUsbPermission(this);

        int state = USBHelper.getInstance(m_Context).connection(USBHelper.VendorID, USBHelper.ProductID);
       LUtils.e(TAG, " =  state  ===" + state );
       LUtils.e(TAG, " =  state  ===" + state );
        if (state == 0) {
           LUtils.e(TAG, " =connect success =" + state );
            WclIDCard.this.callMsgCallback(" device connect success ");
//            startOther(var1);
        } else{
           LUtils.e(TAG, " =  device connect failed ===" + state );
            WclIDCard.this.callMsgCallback("connect failed ，state：" + state);

        }
    }

//    private void startOther(Context var1) {
//
//       LUtils.d(TAG, " MainActivity  -startOther");
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        ComponentName cn = new ComponentName(packages, packageNames);
//        intent.setComponent(cn);
//        var1.startActivity(intent);
//    }

    public void callMsgCallback(String var1) {
        this.m_Msg = var1;
        if (this.m_Callback != null) {
            this.m_Callback.onIDCardMsg(this.m_Msg);
        }

    }

    public void returnResultCallback() {
        if (this.m_Callback != null) {
            this.m_Callback.onIDCardResult2();
        }

    }

    public void setIDCardCallback(WclIDCard.IDCardCallBack var1) {
        this.m_Callback = var1;
    }

//    public void notifyFaceVerityOver() {
//        this.m_ID = "0000";
//    }

    @Override
    public void onUsbPermissionSuccess() {
      LUtils.e(TAG, "onUsbPermissionSuccess");
        haspermission = true;
        startIDCardReader();
    }

    @Override
    public void onUsbPermissionFailed() {
      LUtils.e(TAG, "onUsbPermissionFailed");
        this.callMsgCallback("IDcard OR  usb  No premission ");
    }

    @Override
    public void onCheckCard(String tag, int type) {
        if (m_bStoped)
            return;
      LUtils.e(TAG, "onCheckCard: " + tag);
        if ("9F00".equals(tag)) {
            USBHelper.getInstance(m_Context).sendData(InstructContant.CHOOSE_CARD_TYPE, this);
        } else
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        USBHelper.getInstance(m_Context).sendData(InstructContant.CHECK_CARD_TYPE, WclIDCard.this);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    @Override
    public void onChooseCard(String tag) {
        if (m_bStoped)
            return;
      LUtils.e(TAG, "onChooseCard: " + tag);
        if ("9000".equals(tag)) {
            USBHelper.getInstance(m_Context).sendData(InstructContant.READ_CARD_TYPE, this);
        } else
            USBHelper.getInstance(m_Context).sendData(InstructContant.READ_CARD_TYPE, this);
    }

    @Override
    public void onReadCard(String tag, String data, int type) {
        if (m_bStoped)
            return;
      LUtils.e(TAG, "onReadCard: " + tag);
        if ("9000".equals(tag)) {
            parseCardInfo(data.toUpperCase(), type);
        }
//        else
//            startIDCardReader();
    }

    private void parseCardInfo(String data, int type) {
      LUtils.e(TAG, "parseCardInfo_---  jiexi shenfenzheng ");
        IDCardInfo idCardInfo = new IDCardInfo();
        try {
          LUtils.e(TAG,"duka leixing ------" + type);
            if (type == InstructContant.DIST_ELECERT_CARD_TYPE)
                idCardInfo.decodeCardInfo(ByteUtil.hexStringToByte(data.substring(30, 30 + 512)), ByteUtil.hexStringToByte(data.substring(30 + 512, 30 + 512 + 2048)));
            else
                idCardInfo.decodeCardInfo(ByteUtil.hexStringToByte(data.substring(30 - 8, 30 + 512 - 8)), ByteUtil.hexStringToByte(data.substring(30 + 512 - 8, 30 + 512 + 2048 - 8)));

            WclIDCard.this.m_Name = idCardInfo.getName();
            WclIDCard.this.m_Nation = idCardInfo.getNation();
            WclIDCard.this.m_Addr = idCardInfo.getAddress();
            WclIDCard.this.m_ID = idCardInfo.getId();
            WclIDCard.this.m_Depart = idCardInfo.getDepart();
            WclIDCard.this.m_Birth = idCardInfo.getBirth();
            WclIDCard.this.m_Sex = idCardInfo.getSex();
            WclIDCard.this.m_ValidTime = idCardInfo.getValidityTime();
          LUtils.e(TAG, "parseCardInfo:  jiexi shenfenzheng Succ " + toString());
            if (idCardInfo.getPhoto() != null) {
                WclIDCard.this.m_Photo = IDPhotoHelper.Bgr2Bitmap(idCardInfo.getPhoto());
            }
          LUtils.e(TAG, "parseCardInfo:  jiexi shenfenzhengSucc");
            this.m_bOpen = false;
            this.m_bStoped = false;
            returnResultCallback();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(int type) {
        if (m_bStoped)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    callMsgCallback("Reset the IDCard or phone");
                    Thread.sleep(1000);
                    USBHelper.getInstance(m_Context).sendData(InstructContant.CHECK_CARD_TYPE, WclIDCard.this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void notifyFaceVerityOver() {
        startIDCardReader();
    }

    public interface IDCardCallBack {
        void onIDCardResult2();

        void onIDCardMsg(String var1);
    }

    @Override
    public String toString() {
        return "WclIDCard{" +
                "m_Name='" + m_Name + '\'' +
                ", m_Sex='" + m_Sex + '\'' +
                ", m_Addr='" + m_Addr + '\'' +
                ", m_Birth='" + m_Birth + '\'' +
                ", m_ID='" + m_ID + '\'' +
                ", m_Depart='" + m_Depart + '\'' +
                ", m_Nation='" + m_Nation + '\'' +
                ", m_ValidTime='" + m_ValidTime + '\'' +
                '}';
    }
}
