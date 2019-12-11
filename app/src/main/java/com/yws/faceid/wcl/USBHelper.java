package com.yws.faceid.wcl;

/**
 * Created by wangcl on 2019/3/20.
 */

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.estone.zhouxinliang.eid_id_sam_reader.LUtils;
import com.yws.faceid.wcl.contant.USBInterfaceContant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.yws.faceid.wcl.ByteUtil.byte2Int;
import static com.yws.faceid.wcl.contant.InstructContant.CHECK_CARD_INSTRUCT;
import static com.yws.faceid.wcl.contant.InstructContant.CHECK_CARD_TYPE;
import static com.yws.faceid.wcl.contant.InstructContant.CHOOSE_CARD_INSTRUCT;
import static com.yws.faceid.wcl.contant.InstructContant.CHOOSE_CARD_TYPE;
import static com.yws.faceid.wcl.contant.InstructContant.ELECERT_CHECK_CARD_INSTRUCT;
import static com.yws.faceid.wcl.contant.InstructContant.ELECERT_CHOOSE_CARD_INSTRUCT;
import static com.yws.faceid.wcl.contant.InstructContant.ELECERT_READ_CARD_INSTRUCT;
import static com.yws.faceid.wcl.contant.InstructContant.READ_CARD_INSTRUCT;
import static com.yws.faceid.wcl.contant.InstructContant.READ_CARD_TYPE;
import static com.yws.faceid.wcl.contant.USBInterfaceContant.usb_permission_ok;
import static com.zkteco.android.biometric.core.utils.ToolUtils.bytesToHexString;


/**
 * Created by YuanYe on 18/6/21.
 * Description: 封装Usb接口通信的工具类
 * <p>
 * 使用USB设备：
 * 1.添加权限：
 * <uses-feature  android:name="android.hardware.usb.host" android:required="true">
 * </uses-feature>
 * 2.Manifest中添加以下<intent-filter>，获取USB操作的通知：
 * <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
 * 3.添加设备过滤信息，气筒usb_xml可以自由修改：
 * <meta-data  android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
 * android:resource="@xml/usb_xml"></meta-data>
 * 4.根据目标设备的vendorId和productId过滤USB设备,拿到UsbDevice操作对象
 * 5.获取设备通讯通道
 * 6.连接
 */
public class USBHelper {
    private onUsbPermissionListener onUsbPermissionListener;

    private static final String TAG = "USBDeviceUtil";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int GROUP_SIZE = 136;
    private static final int TIMEOUT = 3000;
    private static USBHelper util;
    private static Context mContext;

    public static final int VendorID = 8898;
    public static final int ProductID = 9473;

    private UsbDevice usbDevice; //目标USB设备
    private UsbManager usbManager;
    /**
     * 中断端点
     */
    private UsbEndpoint epIntEndpointIn;
    private PendingIntent intent; //意图
    private UsbDeviceConnection conn = null;
    private OnFindListener listener;
    private int statue = USBInterfaceContant.usb_ok;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                statue = usb_permission_ok;
                LUtils.e(TAG, "获取权限成功");
                onUsbPermissionListener.onUsbPermissionSuccess();
            } else {
                LUtils.e(TAG, "获取权限失败");
                statue = USBInterfaceContant.usb_permission_fail;
                onUsbPermissionListener.onUsbPermissionFailed();
            }
        }
    };

    public static USBHelper getInstance(Context _context) {
        if (util == null) util = new USBHelper(_context);
        mContext = _context;
        return util;
    }

    private USBHelper(Context _context) {
        this.mContext = _context;

    }

    public void requestUsbPermission(onUsbPermissionListener listener) {
        LUtils.e(TAG, "requestUsbPermission");
        this.onUsbPermissionListener = listener;
        intent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(ACTION_USB_PERMISSION));
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public UsbManager getUsbManager() {
        return usbManager;
    }

    /**
     * 找到自定设备
     */
    public UsbDevice getUsbDevice(int vendorId, int productId) {
        //1)创建usbManager
        if (usbManager == null)
            usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        //2)获取到所有设备 选择出满足的设备
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            LUtils.i(TAG, "vendorID--" + device.getVendorId() + "ProductId--" + device.getProductId());
            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
                return device; // 获取USBDevice
            }
        }
        statue = USBInterfaceContant.usb_find_this_fail;
        return null;
    }

    /**
     * 查找本机所有的USB设备
     */
    public List<UsbDevice> getUsbDevices() {
        //1)创建usbManager
        if (usbManager == null)
            usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        //2)获取到所有设备 选择出满足的设备
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        //创建返回数据
        List<UsbDevice> lists = new ArrayList<>();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            LUtils.i(TAG, "vendorID--" + device.getVendorId() + "ProductId--" + device.getProductId());
            lists.add(device);
        }
        return lists;
    }


    /**
     * 根据指定的vendorId和productId连接USB设备
     *
     * @param vendorId  产商id
     * @param productId 产品id
     */
    public int connection(int vendorId, int productId) {
        usbDevice = getUsbDevice(vendorId, productId);
        //3)查找设备接口
        if (usbDevice == null) {

            LUtils.e(TAG, "NO find  product ，vendorId " + vendorId + "and product id "
                    + productId + "is ok ");
            return statue;
        }
        UsbInterface usbInterface = null;
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            //一个设备上面一般只有一个接口，有两个端点，分别接受和发送数据
            usbInterface = usbDevice.getInterface(i);
            break;
        }
        //4)获取usb设备的通信通道endpoint
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            switch (ep.getType()) {
                case UsbConstants.USB_ENDPOINT_XFER_INT://中断
                    if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                        epIntEndpointIn = ep;
                        LUtils.e(TAG, "find the InterruptEndpointIn:" + "index:" + i + "," + epIntEndpointIn.getEndpointNumber());
                    }
                    break;
                default:
                    break;
            }
        }
        LUtils.e(TAG, "11111111111");
        //5)打开conn连接通道
        if (usbManager.hasPermission(usbDevice)) {
            LUtils.e(TAG, "2222222222");
            //有权限，那么打开
            conn = usbManager.openDevice(usbDevice);
        } else {
            LUtils.e(TAG, "333333333");
            usbManager.requestPermission(usbDevice, intent);
            if (usbManager.hasPermission(usbDevice)) { //权限获取成功
                conn = usbManager.openDevice(usbDevice);
            } else {
                LUtils.e(TAG, "NO premission to read ");
                statue = USBInterfaceContant.usb_permission_fail;
            }
        }
        if (null == conn) {
            LUtils.e(TAG, "don not connect devices");
            statue = USBInterfaceContant.usb_open_fail;
            return statue;
        }
        //打开设备
        if (conn.claimInterface(usbInterface, true)) {
            if (conn != null)// 到此你的android设备已经连上zigbee设备
                LUtils.i(TAG, " dakai  shebei chenggong  ");
            final String mySerial = conn.getSerial();
            LUtils.i(TAG, "serial number：" + mySerial);
            statue = USBInterfaceContant.usb_ok;
        } else {
            LUtils.i(TAG, "do not open connect  ");
            statue = USBInterfaceContant.usb_passway_fail;
            conn.close();
        }
        return statue;
    }

    // 指令拼装
    private byte[] sendAssembleData(byte[] instruct) {
        byte[] assembleData = new byte[GROUP_SIZE];
        for (int i = 0; i < assembleData.length; i++) {
            if (i < instruct.length) {
                assembleData[i] = instruct[i];
            } else
                assembleData[i] = 0x00;
        }
        return assembleData;
    }

    byte[][] checkCardList = {ELECERT_CHECK_CARD_INSTRUCT, CHECK_CARD_INSTRUCT};
    byte[][] chooseCardList = {ELECERT_CHOOSE_CARD_INSTRUCT, CHOOSE_CARD_INSTRUCT};
    byte[][] readCardList = {ELECERT_READ_CARD_INSTRUCT, READ_CARD_INSTRUCT};

    int poision = 0;

    /**
     * 通过USB发送数据
     */
    public void sendData(final int type, final OnSendData2DeviceListener listener) {
        LUtils.i(TAG, "type: " + type);
        byte[] buffer;
        switch (type) {
            case CHECK_CARD_TYPE:
                poision = poision % 2 + 1;
                buffer = checkCardList[poision % 2];
                break;
            case CHOOSE_CARD_TYPE:
                buffer = chooseCardList[poision % 2];
                break;
            case READ_CARD_TYPE:
                buffer = readCardList[poision % 2];
                break;
            default:
                buffer = null;
                break;
        }
        LUtils.e(TAG, "type: " + type + " poision: " + poision + " buffer: " + ByteUtil.bytesToHexString(buffer));
        byte[] assembleData = sendAssembleData(buffer);
        if (conn == null) return;
        if (conn.controlTransfer(33, 9, 768, 0, assembleData, assembleData.length, TIMEOUT) >= 0) {
            //0 或者正数表示成功
            LUtils.i(TAG, "send  success");
            statue = usb_permission_ok;
        } else {
            LUtils.i(TAG, "send  failed");
            statue = USBInterfaceContant.usb_permission_fail;
        }

        final int finalPoision = poision;
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] tag = new byte[2];
                byte[] outData = new byte[GROUP_SIZE];
                boolean flag = true;
                boolean isContinueRead = false;
                byte[] readCardData = new byte[0];
                int readCardDataLen = 0;
                int cursorCardDataLen = 0;

                while (flag) {
                    if (conn.bulkTransfer(epIntEndpointIn, outData, outData.length, 3000) >= 0) {
                        //LUtils.i(TAG, "接收成功");
                        System.arraycopy(outData, 0, tag, 0, 2);
                        LUtils.i(TAG, "tag: ====" + ByteUtil.bytesToHexString(tag).toLowerCase());
                        if ("4100" .equals(ByteUtil.bytesToHexString(tag).toLowerCase())
                         || "410d".equals( ByteUtil.bytesToHexString(tag).toLowerCase())
//                                ||(ByteUtil.bytesToHexString(tag).toLowerCase()).contains("24")
                         )
                        {
                            listener.onFail(type);

                            break;
                        }else {
                            switch (type) {
                                case CHECK_CARD_TYPE:
                                    listener.onCheckCard(ByteUtil.bytesToHexString(tag), finalPoision % 2);
                                    flag = false;
                                    break;
                                case CHOOSE_CARD_TYPE:
                                    listener.onChooseCard(ByteUtil.bytesToHexString(tag));

                                    flag = false;
                                    break;
                                case READ_CARD_TYPE:
                                    if (!isContinueRead && "9000".equals(ByteUtil.bytesToHexString(tag))) {
                                        LUtils.i(TAG, "read success");
                                        isContinueRead = true;
                                        flag = true;
                                        readCardDataLen = ((byte) outData[2]) * 256 + byte2Int((byte) outData[3]);
                                        LUtils.i(TAG, "crad count length" + readCardDataLen);
                                        readCardData = new byte[readCardDataLen + GROUP_SIZE];
                                        System.arraycopy(outData, 0, readCardData, cursorCardDataLen, outData.length);
                                        cursorCardDataLen += outData.length;
                                        LUtils.i(TAG, "reading length" + cursorCardDataLen);
                                    } else if (isContinueRead) {
                                        cursorCardDataLen += outData.length;
                                        if (cursorCardDataLen < readCardDataLen) {
                                            LUtils.i(TAG, "go on reading");
                                            isContinueRead = true;
                                            flag = true;
                                            System.arraycopy(outData, 0, readCardData, cursorCardDataLen - outData.length, outData.length);
                                        } else {
                                            LUtils.i(TAG, "reading over ");
                                            System.arraycopy(outData, 0, readCardData, cursorCardDataLen - outData.length, outData.length);
                                            LUtils.i(TAG, "read end date:" + bytesToHexString(readCardData));
                                            listener.onReadCard("9000", ByteUtil.bytesToHexString(readCardData), finalPoision % 2);
                                            isContinueRead = false;
                                            flag = false;
                                        }
                                    }
                                    break;
                            }
                            LUtils.i(TAG, "jieshou shuju  " + ByteUtil.bytesToHexString(outData));
                        }

                    } else {
                        listener.onFail(type);
                        flag = false;
                        LUtils.i(TAG, "jieshou  shibai ");
                    }
                }
            }
        }
        ).start();
    }

    /**
     * 关闭USB连接
     */

    public void closeConn() {
        if (conn != null) { //关闭USB设备
            conn.close();
            conn = null;
        }
    }

    public void unregisterReceiver() {
        if (mContext != null && broadcastReceiver != null) {
            mContext.unregisterReceiver(broadcastReceiver);
        }
    }

    /**
     * 是否找到设备回调
     */
    public interface OnFindListener {
        void onFindSuccess(UsbDevice usbDevice, UsbManager usbManager);

        void onFailFailed(String error);
    }

    /**
     * 接收数据回调
     */
    public interface OnSendData2DeviceListener {

        void onCheckCard(String tag, int type);

        void onChooseCard(String tag);

        void onReadCard(String tag, String data, int type);

        void onFail(int type);
    }


    /**
     * 接收数据回调
     */
    public interface onUsbPermissionListener {

        void onUsbPermissionSuccess();

        void onUsbPermissionFailed();

    }
}

