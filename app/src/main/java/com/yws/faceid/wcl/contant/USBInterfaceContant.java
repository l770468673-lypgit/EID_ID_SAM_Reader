package com.yws.faceid.wcl.contant;

/**
 * Created by wangcl on 2019/3/20.
 */

public class USBInterfaceContant {
    public static final int usb_ok = 0;//open success

    public static final int usb_permission_ok = 10001; //USB has premission
    public static final int usb_permission_fail = 10002;//USB no premission
    public static final int usb_find_this_fail = 10003;// no devices
    public static final int usb_find_all_fail = 10004;//no any devices
    public static final int usb_open_fail = 10005;//USB openfailed
    public static final int usb_passway_fail = 10006;//US tongdao  open failed

    public static final int usb_send_data_ok = 10007;//USB send date success
    public static final int usb_send_data_fail = 10008;//USBsend date failed
}
