package com.textlinksms.textlink;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.Calendar;
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static Context appContext;
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("kurac", "DOBIJENTOKEN");
        Log.e("newToken", s);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.d("kurac", "Dobio poruku. ");
        Calendar calendar = Calendar.getInstance();

        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        Log.d("kurac", "Vreme poruke "+minutes+":"+seconds);
        if (message.getData().size() <= 0)
            return;

        String messageIdString = message.getData().get("message_id");
        String phoneNumber = message.getData().get("phone_number");
        String text = message.getData().get("text");
        String simCardName = message.getData().get("sim_card_name");

        if (simCardName==null || messageIdString == null || phoneNumber == null || text == null)
            return;

        Integer messageId = Integer.parseInt(messageIdString);


        QueuedSMS sms = new QueuedSMS(messageId, phoneNumber, text, simCardName);

        new SMSController().sendSMS(this, sms);
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }
}
