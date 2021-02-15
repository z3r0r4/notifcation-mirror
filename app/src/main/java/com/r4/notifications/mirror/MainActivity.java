package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MAIN";

    public NotificationManagerCompat notificationManager;
//TODO add logcat textview
//TODO show last received notification
//TODO add reply text textbox
//TODO add last reply list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*add on click to post test notification*/
        TestNotificationButtonSetOnClick();

        /*check if listener is connecteed or connect listener in switch click*/
        Switch swListenerStatus = (Switch) findViewById(R.id.swListenerPermission);
        swListenerStatus.setClickable(false);
        swListenerStatus.setChecked(checkListenerService());

        swListenerStatus.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        });

        SharedPreferences shPref = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = shPref.edit();
        Switch swMirrorState = (Switch) findViewById(R.id.swMirrorState);
        swMirrorState.setChecked(shPref.getBoolean("MirrorState", false));
        swMirrorState.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("MirrorState", isChecked);
            editor.apply();
            if (isChecked)
                Log.d(TAG, "onCreate: Mirroring now");
            if (!isChecked)
                Log.d(TAG, "onCreate: NOT Mirroring now");
        });

        /*handle replyintents from testnotification*/
        handleReplyIntent();

        TextView tvIP = (TextView) findViewById(R.id.tV_IP);
        TextView tvPORT = (TextView) findViewById(R.id.tV_PORT);
        tvIP.setText(shPref.getString("HOST_IP", "192.168.178.84"));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", 9001)));

        EditText etIP = (EditText) findViewById(R.id.eT_IP);
        etIP.addTextChangedListener(new TextWatcher() {
                                        public void afterTextChanged(Editable s) {
                                            editor.putString("HOST_IP", s.toString());
                                            editor.apply();
                                            Log.d(TAG, "changed HOST_IP to:" + s.toString());
                                        }

                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                        }

                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        }
                                    }
        );
        EditText etPORT = (EditText) findViewById(R.id.eT_Port);
        etPORT.addTextChangedListener(new TextWatcher() {
                                          public void afterTextChanged(Editable s) {
                                              try {
                                                  editor.putInt("HOST_PORT", Integer.parseInt(s.toString()));
                                                  editor.apply();
                                                  Log.d(TAG, "changed HOST_PORT to:" + Integer.parseInt(s.toString()));
                                              } catch (NumberFormatException e) {
                                                  Log.e(TAG, "NOT AN INT");
                                              }
                                          }

                                          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                          }

                                          public void onTextChanged(CharSequence s, int start, int before, int count) {
                                          }
                                      }
        );

        if (!checkListenerService()) return;
        /*add onclick to for Test Notification post */
        Button btnTestNotificationReceiverAccess = (Button) findViewById(R.id.btnTestBinding);
        btnTestNotificationReceiverAccess.setOnClickListener(v -> {
            try {
                Log.d("Test By extracting Last Notification", NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).toString());
            } catch (NullPointerException e) {
                Log.e(TAG, "no Noficications yet, or Listener broke");
            }
        });

        /*add onclick to reply to last notification */
        Button btnReply = (Button) findViewById(R.id.btnReply);
        btnReply.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Reply");
            try {
                NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).reply("AUTOREPLY", getApplicationContext());
            } catch (NullPointerException e) {
                Log.e(TAG, "no Noficications yet, or Listener broke");
            }
        });

        /*add onclick to mirror test notification*/
        Button btnNetTest = (Button) findViewById(R.id.btnNetTest);
        btnNetTest.setOnClickListener(v -> {
            try {
                Mirror mirror = new Mirror();
                mirror.execute(NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey));
            } catch (NullPointerException e) {
                Log.e(TAG, "no Noficications yet, or Listener broke");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*check if listener is connecteed*/
        Switch swListenerStatus = (Switch) findViewById(R.id.swListenerPermission);
        swListenerStatus.setChecked(checkListenerService());

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /*check if listener is connecteed*/
        Switch swListenerStatus = (Switch) findViewById(R.id.swListenerPermission);
        swListenerStatus.setChecked(checkListenerService());

        handleReplyIntent();
    }

    private boolean checkListenerService() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ListenerStatus", false);
    }

    /* TEST ONLY */
    @Deprecated
    private void handleReplyIntent() {
        Intent intent = this.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        Log.d(TAG, "handleReplyIntent: Trying to get Replied Input");
        try {//TODO dont react to every intent (use broadcasts?)
            String inputString = remoteInput.getCharSequence("reply").toString();

            TextView replyTV = (TextView) findViewById(R.id.tV_repliedtext);
            replyTV.setText(inputString);

            Notification repliedNotification =   //update Notifiaction to stop sending loading circle
                    new NotificationCompat.Builder(this, "TestChannel")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentText("Reply received")
                            .build();
            notificationManager.notify(9001, repliedNotification);
        } catch (NullPointerException e) {
            Log.e(TAG, "handleReplyIntent: couldn't get Reply text, maybe wrong Intent");// , e);
        }
    }

    @Deprecated
    private void TestNotificationButtonSetOnClick() {
        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(new NotificationChannel("TestChannel", "Test", NotificationManager.IMPORTANCE_HIGH));
        final MirrorNotification notification = new MirrorNotification("123456", "TestNotification", "Testing", "ReplyAction", this);

        Button btnMsgTest = (Button) findViewById(R.id.btnMsgTest);
        btnMsgTest.setOnClickListener(v -> {//TODO dont use lambda pass a actual callback function https://medium.com/@CodyEngel/4-ways-to-implement-onclicklistener-on-android-9b956cbd2928
            notification.post(notificationManager, getApplicationContext());
            Log.d(TAG, "onClick: msgTest");
        });
    }
}