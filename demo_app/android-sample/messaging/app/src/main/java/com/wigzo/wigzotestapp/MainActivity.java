/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wigzo.wigzotestapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.helpers.OrganizationEvents;
import wigzo.sdk.model.EventInfo;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ORG_TOKEN = "56065c5b-db30-4b89-bd76-0a9c2938c90b";

    SharedPreferences pref;
    SharedPreferences.Editor edit;
    Button eventClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventClick = (Button) findViewById(R.id.event);

        //Check for google play services availability
        checkGooglePlayServicesAvailability();

        //To generate token without adding subscription
        Intent i = new Intent(MainActivity.this, MyFirebaseInstanceIDService.class);
        startService(i);

        //FirebaseMessaging.getInstance().subscribeToTopic("MyTopic");

        /*Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(1000);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();*/

        getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE).edit().putString("ORG_TOKEN", ORG_TOKEN).apply();

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }

            if (getIntent().getExtras().get("notification") != null)
            {
                eventClick.setText(getIntent().getExtras().get("notification").toString());
            }
        }
        // [END handle_data_extras]

        Button subscribeButton = (Button) findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic("MyTopic");
                // [END subscribe_topics]
                // Log and toast
                String msg = getString(R.string.msg_subscribed);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        Button logTokenButton = (Button) findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.e("getId________", FirebaseInstanceId.getInstance().getId());
                Log.e("getId________", FirebaseInstanceId.getInstance().getToken());
                Log.e("getId________", FirebaseInstanceId.getInstance().getToken());

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        eventClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventInfo event = new EventInfo ( OrganizationEvents.Events.VIEW.key,"NewSdkTestCall");
                event.saveEvent();

            }
        });

        /*if (pref.getBoolean("APP_FIRST_RUN", true)) {

            subscribeButton.performClick();

            edit = pref.edit().putBoolean("APP_FIRST_RUN", false);
            edit.apply();

            WigzoSDK.getInstance().initializeWigzoData(getApplicationContext(), ORG_TOKEN, FirebaseInstanceId.getInstance().getToken());
            Log.e("FIRSTRUN_____", "" + pref.getBoolean("APP_FIRST_RUN", true));
        }

        else {
            Log.e("FIRSTRUN_____", "" + pref.getBoolean("APP_FIRST_RUN", true));
        }*/
    }

    private void checkGooglePlayServicesAvailability()
    {
        int googlePlayServicesResultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

        if (googlePlayServicesResultCode != ConnectionResult.SUCCESS)
        {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(googlePlayServicesResultCode))
            {
                GoogleApiAvailability.getInstance().getErrorDialog(this, googlePlayServicesResultCode, 1001);
            }
            else
            {

            }
        }
    }

}
