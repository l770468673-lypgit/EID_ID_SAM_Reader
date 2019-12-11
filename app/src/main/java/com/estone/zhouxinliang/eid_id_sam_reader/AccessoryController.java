package com.estone.zhouxinliang.eid_id_sam_reader;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class AccessoryController {
    public static int LED_RED = 0;
    public static int LED_GREEN = 1;
    public static int LED_CAM_WHITE = 2;
    public static int LED_CAM_RED = 3;
    public static int LED_WORKING = 4;

    private static final String TAG = "AccessoryController";
    private static final String LED_CTL_PATH = "/sys/class/zh_gpio_out/out";
    private static final String[] LED_OFF_VAL = {
            "2",
            "4",
            "6",
            "8",
            "12"
    };

    private static final String[] LED_ON_VAL = {
            "1",
            "3",
            "5",
            "7",
            "11"
    };

    private static final String RELAY_ON = "9";
    private static final String RELAY_OFF = "10";

    private static void ctlLedRelay(String val){
        File file = new File(LED_CTL_PATH);
        if(!file.exists() || !file.canWrite()){
           LUtils.e(TAG, "LED ctl path is fault! ");
        }
        try{
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter printWriter = new PrintWriter(fout);
            printWriter.println(val);
            printWriter.flush();
            printWriter.close();
            fout.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //e.x : AccessoryController.ctlLed(true, AccessoryController.LED_CAM_WHITE);
    public static void ctlLed(boolean light, int led_num){
        if(light){
            ctlLedRelay(LED_ON_VAL[led_num]);
        }else {
            ctlLedRelay(LED_OFF_VAL[led_num]);
        }
    }

    public static void ctlRelay(boolean on){
        if(on){
            ctlLedRelay(RELAY_ON);
        }else {
            ctlLedRelay(RELAY_OFF);
        }
    }

}
