package com.textlinksms.textlink;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.Calendar;
import org.json.JSONObject;

public class SMSController {
    private static final String SERVIP = "https://textlinksms.com";

    public void sendPostRequest(Context context, Integer messageId, String status) {
        try {
            Log.d("kurac", "Saljem psotreq");
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secret_shared_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            String jwtToken = sharedPreferences.getString("token", "");
            Log.d("kurac", jwtToken + " jt");

            if (jwtToken.length() == 0) {
                return;
            }
            OkHttpClient client = new OkHttpClient();
            String json = "{\"message_id\": \"" + messageId + "\", \"status\": \"" + status + "\"}";

            Log.d("kurac", json);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(SERVIP + "/user/update-status")
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .post(body)
                    .build();

            Log.d("kurac", "Poslao req");

            try (Response response = client.newCall(request).execute()) {
                // Handle the response or log it
                String responseData = response.body().string();
                Log.d("kurac", responseData);
                // Log.d("POST Request", "Response: " + responseData);
            } catch (Exception e) {
                Log.d("kurac", e.toString());
                e.printStackTrace();
                // Handle the error
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendReceivePostRequest(Context context, String phoneNumber, String text, String simCardName,
            String firebaseToken) {
        try {
            Log.d("kurac", "Saljem psotreq");
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secret_shared_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            String jwtToken = sharedPreferences.getString("token", "");
            Log.d("kurac", jwtToken + " jt");

            if (jwtToken.length() == 0) {
                return;
            }
            OkHttpClient client = new OkHttpClient();

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("phone_number",  phoneNumber);
            bodyJson.put("text",          text);
            bodyJson.put("sim_name",      simCardName);
            bodyJson.put("firebase_token", firebaseToken);

            RequestBody body = RequestBody.create(
                bodyJson.toString(),
                MediaType.get("application/json; charset=utf-8")
            );
            Request request = new Request.Builder()
                    .url(SERVIP + "/user/receive-sms")
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .post(body)
                    .build();

            Log.d("kurac", "Poslao req");

            try (Response response = client.newCall(request).execute()) {
                // Handle the response or log it
                String responseData = response.body().string();
                Log.d("kurac", responseData);
                // Log.d("POST Request", "Response: " + responseData);
            } catch (Exception e) {
                Log.d("kurac", e.toString());
                e.printStackTrace();
                // Handle the error
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSMS(MyFirebaseMessagingService service, QueuedSMS sms) {
        if (ActivityCompat.checkSelfPermission(service,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        SubscriptionInfo customSimSubscriptionInfo = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) service
                    .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (subscriptionManager == null)
                return;

            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                if (subscriptionInfo.getDisplayName().equals(sms.getSimCardName())) {
                    customSimSubscriptionInfo = subscriptionInfo;
                    break;
                }
            }
        }

        Log.d("kurac", "SALJEM ID JE 4" + sms.getId());

        // sendPostRequest(service, sms.getId(), "sending");

        Intent sentIntent = new Intent(service, SMSBroadcastReceiver.class);
        sentIntent.setAction(SMSBroadcastReceiver.SENT);
        sentIntent.putExtra("MESSAGE_ID", sms.getId());

        Intent deliveredIntent = new Intent(service, SMSBroadcastReceiver.class);
        deliveredIntent.setAction(SMSBroadcastReceiver.DELIVERED);
        deliveredIntent.putExtra("MESSAGE_ID", sms.getId());

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent sentPI = PendingIntent.getBroadcast(service, sms.getId(), sentIntent, flags);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(service, sms.getId(), deliveredIntent, flags);

        SmsManager smsManager;

        // TODO OVO TREBA JOS DA SE ISTESTIRA I VIDI JEL POKRIVA SVE EDGE CASOVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && customSimSubscriptionInfo != null) {
            smsManager = SmsManager.getSmsManagerForSubscriptionId(customSimSubscriptionInfo.getSubscriptionId());
        } else {
            smsManager = SmsManager.getDefault();
        }

        // Split message if necessary and send multi-part SMS
        ArrayList<String> messageParts = smsManager.divideMessage(sms.getText());

        if (messageParts.size() > 1) {
            // Prepare lists for PendingIntents
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveredIntents = new ArrayList<>();

            for (int i = 0; i < messageParts.size(); i++) {
                sentIntents.add(sentPI);
                deliveredIntents.add(deliveredPI);
            }

            // Send multi-part SMS
            smsManager.sendMultipartTextMessage(sms.getPhoneNumber(), null, messageParts, sentIntents, deliveredIntents);
        } else {
            // If it's a single part message, send normally
            smsManager.sendTextMessage(sms.getPhoneNumber(), null, sms.getText(), sentPI, deliveredPI);
        }

        Calendar calendar = Calendar.getInstance();

        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        Log.d("kurac", "Vreme zvanja send " + minutes + ":" + seconds);
    }

}
