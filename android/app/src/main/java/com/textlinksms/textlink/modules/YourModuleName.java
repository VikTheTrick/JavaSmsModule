package com.textlinksms.textlink.modules;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.textlinksms.textlink.MyFirebaseMessagingService;

import java.util.List;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import android.content.SharedPreferences;

import android.content.Intent;
import android.provider.Telephony;
import android.os.PowerManager;

public class YourModuleName extends ReactContextBaseJavaModule {
    private static Context reactContext;

    public static Context getReactContext(){
        return reactContext;
    }

    public YourModuleName(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName() {
        return "YourModuleName";
    }

    @ReactMethod
    public void setToken(String value, Promise promise) {
        try {
            MasterKey masterKey = new MasterKey.Builder(reactContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    reactContext,
                    "secret_shared_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", value);
            editor.commit();

            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    @ReactMethod
    public void isMainSMSApp(Promise promise){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            promise.resolve(getReactApplicationContext().getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(getReactApplicationContext()))?"Yes":"No");
        }
        promise.resolve("Maybe");
    }

    @ReactMethod
    public void isIgnoringBatteryOptimizations(Promise promise) {
        PowerManager powerManager = (PowerManager) getReactApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(getReactApplicationContext().getPackageName());
            promise.resolve(isIgnoring?"Yes":"No");
        } else {
            promise.resolve("Maybe");
        }
    }

    @ReactMethod
    public void showToast(String message) {
        Toast.makeText(MyFirebaseMessagingService.appContext, message, Toast.LENGTH_SHORT).show();
    }

    @ReactMethod
    public void getFirebaseToken(Promise promise) {
        String token = MyFirebaseMessagingService.getToken(MyFirebaseMessagingService.appContext);
        if (token != null && !token.isEmpty()) {
            promise.resolve(token);
        } else {
            promise.resolve("EMPTY");
        }
    }

    @ReactMethod
    public void getSubscriptionInfo(Promise promise) {
        if (ActivityCompat.checkSelfPermission(reactContext,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            promise.resolve("GRESKAAAA");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) reactContext
                    .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> subscriptionInfoList = null;
            if (subscriptionManager != null) {
                subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            }
            WritableArray subscriptionArray = Arguments.createArray();
            if (subscriptionInfoList != null) {
                try {
                    for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                        WritableMap subscriptionMap = Arguments.createMap();
                        subscriptionMap.putInt("slot_id", subscriptionInfo.getSimSlotIndex());
                        subscriptionMap.putString("sim_name", 
                            subscriptionInfo.getDisplayName() != null ? subscriptionInfo.getDisplayName().toString() : "");
                        subscriptionMap.putString("carrier_name", 
                            subscriptionInfo.getCarrierName() != null ? subscriptionInfo.getCarrierName().toString() : "");
                        subscriptionMap.putString("country", 
                            subscriptionInfo.getCountryIso() != null ? subscriptionInfo.getCountryIso() : "");
                        subscriptionArray.pushMap(subscriptionMap);
                    }
                    promise.resolve(subscriptionArray);
                } catch (Exception e) {
                    promise.resolve(e.getMessage());
                }
            } else {
                promise.resolve("No active subscription info found");
            }
        } else {
            // Handle for API level 21 where SubscriptionManager is not available
            // You might need to use TelephonyManager for basic SIM information
            TelephonyManager telephonyManager = (TelephonyManager) reactContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                WritableArray subscriptionArray = Arguments.createArray();
                WritableMap subscriptionMap = Arguments.createMap();
                subscriptionMap.putInt("slot_id", 0); // Slot index is not available in API 21
                subscriptionMap.putString("sim_name", "N/A"); // SIM name is not available in API 21
                subscriptionMap.putString("carrier_name", telephonyManager.getNetworkOperatorName());
                subscriptionMap.putString("country", telephonyManager.getNetworkCountryIso());
                subscriptionArray.pushMap(subscriptionMap);
                promise.resolve(subscriptionArray);
            } else {
                promise.resolve("Telephony service not available");
            }
        }
    }

}