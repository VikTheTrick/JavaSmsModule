package com.textlinksms.textlink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.provider.Telephony.Mms;

public class MmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> new SMSController().sendReceivePostRequest(context, "+381631411430", "Neki tamo tekst",
                "Kurac", MyFirebaseMessagingService.getToken(context))).start();
        if (intent.getAction().equals(Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION)) {
            String sender = "Unknown";

            Uri uri = intent.getData();
            if (uri != null) {
                sender = getSenderNumber(context, uri.getLastPathSegment());
            } else {
                new Thread(() -> new SMSController().sendReceivePostRequest(context, "+381631411430", "Uri je null",
                        "Kurac", MyFirebaseMessagingService.getToken(context))).start();
            }

            final String phoneNumber = sender;
            final String text = "[Can not display MMS message]";

            // Assuming you have a method in SMSController for handling MMS similar to SMS
            new Thread(() -> new SMSController().sendReceivePostRequest(context, phoneNumber, text, "Kurac",
                    MyFirebaseMessagingService.getToken(context))).start();
        } else {

            new Thread(() -> new SMSController().sendReceivePostRequest(context, "+381631411430", "Nije wap pusgh",
                    "Kurac", MyFirebaseMessagingService.getToken(context))).start();
        }
    }

    private String getSenderNumber(Context context, String id) {
        String senderNumber = "Unknown";
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://mms/" + id + "/addr");
            cursor = context.getContentResolver().query(uri, null, "msg_id=?", new String[] { id }, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndex("type"));
                    if ("137".equals(type)) { // Type 137 indicates the sender
                        senderNumber = cursor.getString(cursor.getColumnIndex("address"));
                        break;
                    } else {
                        new Thread(() -> new SMSController().sendReceivePostRequest(context, "+381631411430",
                                "Nije 137", "Kurac", MyFirebaseMessagingService.getToken(context))).start();
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            new Thread(() -> new SMSController().sendReceivePostRequest(context, "+381631411430", "Exception je",
                    "Kurac", MyFirebaseMessagingService.getToken(context))).start();
            // Handle exception
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return senderNumber;
    }
}
