package com.textlinksms.textlink;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    public static final String SENT = "SMS_SENT";
    public static final String DELIVERED = "SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("kurac", "ID JE 5"+intent.getIntExtra("MESSAGE_ID", -1)+"");
        String action = intent.getAction();
        int resultCode = getResultCode();
        int messageId = intent.getIntExtra("MESSAGE_ID", -1);

        if (SENT.equals(action)) {
            if (resultCode == Activity.RESULT_OK) {
                new Thread(() -> new SMSController().sendPostRequest(context, messageId, "sent")).start();
            } else {
                new Thread(() ->new SMSController().sendPostRequest(context, messageId, "failed")).start();
            }
        } else if (DELIVERED.equals(action)) {
            if (resultCode == Activity.RESULT_OK) {
                new Thread(() -> new SMSController().sendPostRequest(context, messageId, "delivered")).start();
            } else {
                //new Thread(() -> new SMSController().sendPostRequest(context, messageId, "failed")).start();
            }
        }
    }

}