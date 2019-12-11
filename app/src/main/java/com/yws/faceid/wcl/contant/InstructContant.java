package com.yws.faceid.wcl.contant;

/**
 * Created by wangcl on 2019/3/20.
 */

public class InstructContant {
    public static final int CHECK_CARD_TYPE = 0;
    public static final int CHOOSE_CARD_TYPE = 1;
    public static final int READ_CARD_TYPE = 2;


    public static final int DIST_ELECERT_CARD_TYPE = 0;


    public static final int DIST_READ_CARD_TYPE = 1;

    public static final byte[] ELECERT_CHECK_CARD_INSTRUCT = {(byte) 0x00, (byte) 0xf2, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    public static final byte[] ELECERT_CHOOSE_CARD_INSTRUCT = {(byte) 0x00, (byte) 0xf2, (byte) 0x02, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    public static final byte[] ELECERT_READ_CARD_INSTRUCT = {(byte) 0x00, (byte) 0xf3, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x10};


    public static final byte[] CHECK_CARD_INSTRUCT = {(byte) 0x00, (byte) 0xfb, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};


    public static final byte[] CHOOSE_CARD_INSTRUCT = {(byte) 0x00, (byte) 0xfb, (byte) 0x02, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};


    public static final byte[] READ_CARD_INSTRUCT = {(byte) 0x00, (byte) 0xfc, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x10};
}
