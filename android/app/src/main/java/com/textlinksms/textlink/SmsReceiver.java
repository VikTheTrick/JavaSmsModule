package com.textlinksms.textlink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_DELIVER_ACTION)) {
            String messageBody = "";
            String sender = "";

            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                messageBody += smsMessage.getMessageBody();
                sender = smsMessage.getDisplayOriginatingAddress();
            }

            final String phoneNumber = sender;
            final String text = messageBody;
            if (sender.matches("^\\+?\\d+$")) {
                int subscriptionId = intent.getIntExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, -1);
    
                if (subscriptionId != -1) {
                    SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
                    SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(subscriptionId);
    
                    if (subscriptionInfo != null) {
                        // Here you get the carrier name
                        final String simCardName = subscriptionInfo.getDisplayName().toString();
                        // TREBA I ID DEVICE
                        new Thread(() -> new SMSController().sendReceivePostRequest(context, phoneNumber, text, simCardName, MyFirebaseMessagingService.getToken(context))).start();
                    }
                    else{
                        new Thread(() -> new SMSController().sendReceivePostRequest(context, phoneNumber, text, "", MyFirebaseMessagingService.getToken(context))).start();
                    }
                }
                else{
                    new Thread(() -> new SMSController().sendReceivePostRequest(context, phoneNumber, text, "", MyFirebaseMessagingService.getToken(context))).start();
                }
            }
        }
    }
}