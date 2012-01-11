package com.engineering.printer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;

public class ErrorCallback {

    private Activity mAct;
    private Handler mHand;

    public  ErrorCallback(Activity act) {
        mAct = act;
        mHand = new Handler();
    }
    
    public void error() {
        Runnable r = new Runnable() {
            public void run() {
                AlertDialog.Builder altb = new AlertDialog.Builder(mAct);
                altb.setMessage("Connection interrupted with server.  Try connecting again or verifying your connectivity to the network.");
                altb.create().show();
            }
        };
        mHand.post( r);
        //Intent myIntent = new Intent(mAct, EngineeringPrinter.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //mAct.startActivityForResult(myIntent, 0);
    }
    
}
