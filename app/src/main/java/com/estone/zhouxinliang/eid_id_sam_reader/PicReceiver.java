package com.estone.zhouxinliang.eid_id_sam_reader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PicReceiver extends BroadcastReceiver {
    private String TAG = "PicReceiver";

    public OnpiDateListener mOnpiDateListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if ("com.estone.pic".contains(intent.getAction())) {
           LUtils.d(TAG, "-------------");
            mOnpiDateListener.OnIpiDateData();
        }

    }

    public  interface OnpiDateListener {
        void OnIpiDateData();
    }

    public void setOnpiDateListener(OnpiDateListener listener) {
        this.mOnpiDateListener = listener;
    }
}
