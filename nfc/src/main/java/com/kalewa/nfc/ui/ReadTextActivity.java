/*
 * Copyright (c),Shenzhen Kalewa IoT Technology Co.,Led.
 * (C)TDL 2019-2027
 * All rights are reserved. Reproduction in whole or in part is prohibited without
 * the written consent of the copyright owner. Kalewa reserves the right to make
 * changes without notice at any time. Kalewa makes no warranty, expressed, implied or
 * statutory, including but not limited to any implied warranty of merchantability
 * or fitness for any particular purpose, or that the use will not infringe any
 * third party patent, copyright or trademark. Kalewa must not be liable for any loss
 * or damage arising from its use.
 */
package com.kalewa.nfc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.kalewa.nfc.MainActivity;


import com.kalewa.nfc.base.BaseNfcActivity;
import com.kalewa.project.kalewatdl.tdl_cmd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.Thread.NORM_PRIORITY;

import com.kalewa.nfc.R;
/**
 * Created by gc on 2016/12/8.
 */
public class ReadTextActivity extends BaseNfcActivity {
    private TextView mNfcText;
    private TextView mDataText;
    private String mTagText;
    private String mTempText;
    private GraphView graph;

    private Button applyButton;
    private Button resetButton;
    private Button applyChange;
    private Button inputPin;

    private RadioButton plan1Radio;
    private RadioButton plan2Radio;

    private EditText givePassword;

    private boolean subThreadOn = false;
    String SDKVersion;

    Vibrator vibrator;

    private static char[] hexTable1 = "0123456789ABCDEF".toCharArray();

    ImageView eye;
    String mPassword = null;
    private boolean adminChangeEn = false;
    private boolean normalChangeEn = false;
    private byte[] mAdminPassword = new byte[8];
    private byte[] mNormalPassword = new byte[8];
    private boolean changeAlready = false;
    static boolean opened = false;

    tdl_cmd myTDL_CMD;

    //private int[] tData;

    private Intent nIntent;

    final int TAGERROR = -1;
    final int GETCONFIG = 0;
    final int SETCONFIG = 1;
    final int RESETTAG = 2;
    final int GETCONFIG2 = 3;
    final int DATAERROR = 4;
    final int SETERROR = 5;
    final int GETCONFIG3 = 6;

    final int CHANGESUCCESS = 0x60;
    final int CHANGEERROR = 0x61;

    final int EMPTY = 0;
    final int FINISH = 1;
    final int PROGRESS = 2;

    //private byte[] mineData = new byte[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_text);
        mNfcText = (TextView) findViewById(R.id.tv_nfcText);
        mDataText = findViewById(R.id.tv_dataText);
        mNfcText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mDataText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTagText = ""; //getString(R.string.app_demo_version) + "\n";
        mTempText = "";
        mNfcText.setText(mTagText);
        mDataText.setText(mTempText);

        givePassword =findViewById(R.id.password);

        applyButton = findViewById(R.id.button1);
        applyButton.setText(getString(R.string.apply));
        resetButton = findViewById(R.id.button2);
        resetButton.setText(getString(R.string.reset));
        applyChange = findViewById(R.id.button3);
        applyChange.setText("Change");
        inputPin = findViewById(R.id.button4);
        inputPin.setText("Input");

        plan1Radio = findViewById(R.id.radioButton1);
        plan2Radio = findViewById(R.id.radioButton2);
        plan1Radio.setText(getString(R.string.plan1));
        plan2Radio.setText(getString(R.string.plan2));

        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        graph = findViewById(R.id.graph);

        //graph.setBackgroundColor(0x77b5fdff);

        myTDL_CMD = new tdl_cmd();

        SDKVersion = myTDL_CMD.get_SDK_version();

        nIntent = null;

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (subThreadOn) {
                    Log.i("Another thread is ", "running.");
                    return;
                }
                Log.i("button clicked here!", "onCreat");

                Thread configThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        subThreadOn = true;
                        Message msg = handler.obtainMessage();
                        boolean methodResault;
                        if (plan1Radio.isChecked()) {
                            methodResault = myTDL_CMD.applyConfig(nIntent, 20, 20, 35, 10, 120);
                        } else {
                            methodResault = myTDL_CMD.applyConfig(nIntent, 300, 0, 0, 0xFFFFFFFF, 86400);
                        }
                        if (methodResault) {
                            myTDL_CMD.retrieved = 0;
                            msg.what = SETCONFIG;
                            handler.sendMessage(msg);
                        } else {
                            msg.what = SETERROR;
                            handler.sendMessage(msg);
                        }

                        subThreadOn = false;
                    }
                });
                configThread.setPriority(NORM_PRIORITY - 1);
                configThread.start();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subThreadOn) {
                    Log.i("Another thread is ", "running.");
                    return;
                }

                Thread resetThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        subThreadOn = true;
                        Message msg = handler.obtainMessage();
                        boolean methodResault;
                        methodResault = myTDL_CMD.applyConfig(nIntent, 0, 0, 0, 0, 0);
                        if (methodResault) {
                            myTDL_CMD.retrieved = 0;
                            msg.what = RESETTAG;
                            handler.sendMessage(msg);
                        } else {
                            msg.what = SETERROR;
                            handler.sendMessage(msg);
                        }
                        subThreadOn = false;
                    }
                });
                resetThread.setPriority(NORM_PRIORITY - 1);
                resetThread.start();
            }
        });

        inputPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ReadTextActivity.this);
                builder.setTitle("New Passwords Input");
                View view = LayoutInflater.from(ReadTextActivity.this).inflate(R.layout.password, null);
                builder.setView(view);

                final EditText adminPassword = (EditText) view.findViewById(R.id.admin_password_EditText);
                final EditText normalPassword = (EditText) view.findViewById(R.id.normal_password_EditText);
                final CheckBox adminEn = (CheckBox) view.findViewById(R.id.adminPassword_Setup_Switch);
                final CheckBox normalEn = (CheckBox) view.findViewById(R.id.normalPassword_Setup_Switch);

                eye = view.findViewById(R.id.iv_eye);
                eye.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(opened){
                            opened = false;
                            eye.setSelected(true);
                            adminPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            normalPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        }else{
                            opened = true;
                            eye.setSelected(false);
                            adminPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            normalPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                    }
                });

                builder.setPositiveButton("Ensure", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String pass = "";
                        int passCnt = 0;

                        String str;

                        if (adminEn.isChecked()) {
                            if(adminPassword.getText().toString().length() == 8) {
                                pass += "Admin password is set: " + adminPassword.getText().toString() + "\n";
                                passCnt += 1;
                                adminChangeEn = true;
                                str = adminPassword.getText().toString();
                                mAdminPassword = str.getBytes();
                                //pass += Util.ByteArrayToHexString(mAdminPassword, ' ') + "\n";
                            }else{
                                pass += "Admin Password length must be 8. Please try it again.";
                                adminChangeEn = false;
                            }
                        } else {
                            pass += "Admin password not set.\n";
                            adminChangeEn = false;
                        }

                        if (normalEn.isChecked()) {
                            if(normalPassword.getText().toString().length() == 8) {
                                pass += "Normal password is set: " + normalPassword.getText().toString() + "\n";
                                passCnt += 1;
                                normalChangeEn = true;
                                str = normalPassword.getText().toString();
                                mNormalPassword = str.getBytes();
                                //pass += Util.ByteArrayToHexString(mNormalPassword, ' ') + "\n";
                            }else{
                                pass += "Normal Password length must be 8. Please try it again.";
                                normalChangeEn = false;
                            }
                        } else {
                            pass += "Normal password not set.\n";
                            normalChangeEn = false;
                        }

                        if(passCnt == 1){
                            pass += "Please remember it!";
                        }else if(passCnt == 2){
                            pass += "Please remember them!";
                        }

                        mNfcText.setText(pass);

                        if(passCnt != 0) {
                            changeAlready = true;
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //adminChangeEn = false;
                        //normalChangeEn = false;
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        applyChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pass;
                if(nIntent == null) {
                    pass = "Must tap a tag and than can do password change.";
                    mNfcText.setText(pass);
                    return;
                }

                if(!changeAlready){
                    pass = "You should key in new passwords first.\n";
                    pass += "And tap a TDL, than press CHANGE to change password.";
                    mNfcText.setText(pass);
                    return;
                }
                changeAlready = false;

                if(myTDL_CMD.authority == myTDL_CMD.ADMIN) {
                    pass = "Will do password change soon.\n";
                    if(adminChangeEn){
                        pass += "Admin password will be changed.\n";
                    }
                    if(normalChangeEn){
                        pass += "Operator password will be changed ";
                    }
                    mNfcText.setText(pass);

                    Thread passwordThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("change pass thread", "start");
                            subThreadOn = true;
                            boolean methodResault = true;

                            methodResault = myTDL_CMD.changePassword(nIntent, adminChangeEn, mAdminPassword, normalChangeEn, mNormalPassword);
                            Message msg = handler.obtainMessage();

                            if (methodResault) {
                                Log.i("Change pass", "success!");
                                msg.what = CHANGESUCCESS;
                                handler.sendMessage(msg);
                            } else {
                                Log.i("Change pass error", "" + myTDL_CMD.error_message);
                                msg.what = CHANGEERROR;   //identify error
                                handler.sendMessage(msg);
                            }
                            Log.i("change pass thread", "end");
                            subThreadOn = false;
                        }
                    });
                    passwordThread.setPriority(NORM_PRIORITY - 1);
                    passwordThread.start();
                }else{
                    pass = "You must login with Admin to do password change.";
                    mNfcText.setText(pass);
                }
            }
        });

        restorePreferences();
        givePassword.setText(mPassword);

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == TAGERROR) {
                mNfcText.setText(mTagText);
                mDataText.setText(mTempText);
            } else if (msg.what == GETCONFIG2) {
                mTempText = getString(R.string.progress);
                mDataText.setText(mTempText);
                //update_data();
            } else if (msg.what == GETCONFIG) {
                update_inform_main();
                mDataText.setText(mTempText);
            } else if (msg.what == DATAERROR) {
                mTempText = getString(R.string.retrierr);
                mDataText.setText(mTempText);
            } else if (msg.what == SETCONFIG) {
                mTempText = getString(R.string.confwell);
                mTagText = getString(R.string.retap);
                mNfcText.setText(mTagText);
                mDataText.setText(mTempText);
            } else if (msg.what == RESETTAG) {
                mTempText = getString(R.string.tegres);
                mTagText = getString(R.string.retap);
                mNfcText.setText(mTagText);
                mDataText.setText(mTempText);
            } else if (msg.what == SETERROR) {
                mTempText = getString(R.string.reserr);
                mDataText.setText(mTempText);
            } else if (msg.what == GETCONFIG3) {
                update_data_main();
            } else if(msg.what == CHANGESUCCESS) {
                mTagText += "Password change success.";
                mNfcText.setText(mTagText);
            }else if(msg.what == CHANGEERROR) {
                mTagText += "Password change fail.";
                mNfcText.setText(mTagText);
            } else {
                Log.i("Message", "" + msg.what + " unknown.");
            }
            return false;
        }
    });

    @Override
    public void onNewIntent(Intent intent) {
        applyButton.setEnabled(false);
        resetButton.setEnabled(false);

        if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            //mTagText = getString(R.string.app_demo_version) + "\n" + getString(R.string.nosupport);
            mTagText = getString(R.string.nosupport);
            mTempText = "";
            mNfcText.setText(mTagText);
            mDataText.setText(mTempText);
            return;
        }

        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (detectedTag == null) {
            //mTagText = getString(R.string.app_demo_version) + "\n" + getString(R.string.nosupport);
            mTagText = getString(R.string.nosupport);
            mTempText = "";
            mNfcText.setText(mTagText);
            mDataText.setText(mTempText);
            return;
        }

        nIntent = intent;
        mTagText = "";
        mTempText = "";
        //create sub thread
        Thread identifyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                subThreadOn = true;
                boolean methodResault;
                Message msg = handler.obtainMessage();

                byte[] password = givePassword.getText().toString().getBytes();  //mPassword.getBytes();

                methodResault = myTDL_CMD.identify(nIntent, password);

                if (methodResault) {
                    vibrator.vibrate(100);

                    if ((myTDL_CMD.count > 0) && (myTDL_CMD.interval != 0)) {
                        mTempText = getString(R.string.nomove);
                        update_inform_sub();
                        msg.what = GETCONFIG;
                        handler.sendMessage(msg);
                        methodResault = myTDL_CMD.retrieve(nIntent);
                        Message msg1 = handler.obtainMessage();
                        if (methodResault) {
                            //update_inform_sub();
                            msg1.what = GETCONFIG2;
                            handler.sendMessage(msg1);
                            vibrator.vibrate(100);

                            methodResault = update_data_sub();
                            if (methodResault) {
                                addSeriesData();
                            }
                            Message msg2 = handler.obtainMessage();
                            msg2.what = GETCONFIG3;
                            handler.sendMessage(msg2);
                        } else {
                            msg1.what = DATAERROR;
                            handler.sendMessage(msg1);
                        }
                    } else {
                        mTempText = getString(R.string.nodata) + "\n";
                        msg.what = GETCONFIG;
                        handler.sendMessage(msg);
                    }
                } else {
                    mTagText = getString(R.string.idenError);
                    if (myTDL_CMD.error_message == myTDL_CMD.PASSWORD_ERROR) {
                        mTagText += " password.";
                    }
                    mTempText = "";
                    msg.what = TAGERROR;
                    handler.sendMessage(msg);
                }

                subThreadOn = false;
            }
        });
        identifyThread.setPriority(NORM_PRIORITY - 1);
        identifyThread.start();
    }

    private static String bytesToHexString(byte[] bytes, char separator) {
        String s = "0";
        StringBuilder hexString = new StringBuilder();
        if ((bytes != null) && (bytes.length > 0)) {
            for (byte b : bytes) {
                int n = b & 0xff;
                if (n < 0x10) {
                    hexString.append("0");
                }
                hexString.append(Integer.toHexString(n));
                if (separator != 0) {
                    hexString.append(separator);
                }
            }
            s = hexString.substring(0, hexString.length() - 1);
        }
        return s;
    }

    private void update_inform_sub() {
        mTagText = SDKVersion + "\n"; //getString(R.string.app_demo_version) + "\n";
        if (myTDL_CMD.uid != null) {
            mTagText += "Uid:" + bytesToHexString(myTDL_CMD.uid, ' ') + "\n";
        }
        mTagText += "FW:" + myTDL_CMD.version_Maj + "." + myTDL_CMD.version_Min + "\n";
        if (myTDL_CMD.batt_vol > 0) {
            mTagText += getString(R.string.battery) + myTDL_CMD.batt_vol + "mV\n";
        }
        if (myTDL_CMD.interval > 0) {
            mTagText += getString(R.string.inter) + myTDL_CMD.interval + "s\n";

            if (myTDL_CMD.startEpoch > 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd-HH:mm:ss");
                Date date = new Date(myTDL_CMD.startEpoch * 1000);
                mTagText += getString(R.string.start) + simpleDateFormat.format(date) + "\n";
            }

            if (!((myTDL_CMD.minimum == 0) && (myTDL_CMD.maximum == 0))) {
                mTagText += getString(R.string.min) + myTDL_CMD.minimum + "℃\n";
                mTagText += getString(R.string.max) + myTDL_CMD.maximum + "℃\n";
            }

            if (myTDL_CMD.count > 0) {
                mTagText += myTDL_CMD.count + getString(R.string.data_in) + "\n";

                if (!((myTDL_CMD.minimum == 0) && (myTDL_CMD.maximum == 0))) {
                    if (myTDL_CMD.valid == 0) {
                        mTagText += getString(R.string.have_fail) + "\n";
                    } else {
                        mTagText += getString(R.string.all_pass) + "\n";
                    }
                }
            }

            if (myTDL_CMD.MeasureFinish) {
                mTagText += getString(R.string.test_finish) + "\n";
            } else {
                mTagText += getString(R.string.test_in_progress) + "\n";
            }

            //if (myTDL_CMD.retrieved > 0) {
            //    mTagText += myTDL_CMD.retrieved + getString(R.string.data_ret) + "\n";
            //}
        } else {
            mTagText += getString(R.string.empty);
            graph.removeAllSeries();
        }
    }

    private void update_inform_main() {
        if (myTDL_CMD.authority == myTDL_CMD.ADMIN) {
            mTagText += " Admin\n";
            applyButton.setEnabled(true);
            resetButton.setEnabled(true);
        } else if (myTDL_CMD.authority == myTDL_CMD.NORMAL) {
            mTagText += " Operator\n";
            applyButton.setEnabled(false);
            resetButton.setEnabled(false);
        } else {
            mTagText += "unknown\n";
            applyButton.setEnabled(false);
            resetButton.setEnabled(false);
        }
        mNfcText.setText(mTagText);
    }

    private boolean update_data_sub() {

        if ((myTDL_CMD.interval > 0) && (myTDL_CMD.retrieved > 0)) {
            mTempText = getString(R.string.table_title) + "\n";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd-HH:mm:ss");
            Date date = new Date(myTDL_CMD.startEpoch * 1000);

            int temp, temp1, temp2;
            float temper;
            int rec;

            if (myTDL_CMD.retrieved <= 10) {
                for (int i = 0; i < myTDL_CMD.retrieved; i++) {
                    date = new Date((myTDL_CMD.startEpoch + myTDL_CMD.interval * i) * 1000);
                    //temp = myTDL_CMD.tData[i];
                    temper = myTDL_CMD.tData[i];
                    temper /= 10;

                    mTempText += simpleDateFormat.format(date);
                    rec = i + 1;
                    mTempText += String.format(Locale.US, " %.1f", temper);
                    mTempText += "℃ " + rec + "\n";
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    date = new Date((myTDL_CMD.startEpoch + myTDL_CMD.interval * i) * 1000);
                    //temp = myTDL_CMD.tData[i];
                    temper = myTDL_CMD.tData[i];
                    temper /= 10;

                    mTempText += simpleDateFormat.format(date);
                    rec = i + 1;
                    mTempText += String.format(Locale.US, " %.1f", temper);
                    mTempText += "℃ " + rec + "\n";
                }
                mTempText += "......\n";
                int i = myTDL_CMD.retrieved - 1;
                date = new Date((myTDL_CMD.startEpoch + myTDL_CMD.interval * i) * 1000);
                temp = myTDL_CMD.tData[i];
                temper = myTDL_CMD.tData[i];
                temper /= 10;
                mTempText += simpleDateFormat.format(date);
                rec = i + 1;
                mTempText += String.format(Locale.US, " %.1f", temper);
                mTempText += "℃ " + rec + "\n";
            }
            mTagText += getString(R.string.end) + simpleDateFormat.format(date);
            return true;
        } else {
            mTempText = getString(R.string.no_data);
            graph.removeAllSeries();
            return false;
        }
    }

    private void update_data_main() {
        mDataText.setText(mTempText);
        mNfcText.setText(mTagText);
        Log.i("Tag TDL", "finished");
    }

    private void addSeriesData() {
        Map<Date, Double> mMap = new HashMap();
        List<Date> mDateList = new ArrayList<>();
        List<Double> mTpList = new ArrayList<>();
        graph.removeAllSeries();
        float t;
        DataPoint[] dataPoints = new DataPoint[myTDL_CMD.retrieved];
        for (int i = 0; i < myTDL_CMD.retrieved; i++) {
            t = myTDL_CMD.tData[i];
            t = t / 10;
            dataPoints[i] = new DataPoint(i + 1, t);
        }
        LineGraphSeries lineGraphSeries = new LineGraphSeries(dataPoints);
        lineGraphSeries.setDrawDataPoints(true);
        lineGraphSeries.setDataPointsRadius(3.1f);
        lineGraphSeries.setDrawBackground(false);
        //lineGraphSeries.setBackgroundColor(0x77000000 | 0xb5fdff);
        lineGraphSeries.setColor(0xFFFF0000);
        lineGraphSeries.setAnimated(true);
        graph.addSeries(lineGraphSeries);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(myTDL_CMD.retrieved);
        //graph.setBackgroundColor(0x777bb1db);
    }

    private int get_tag_status() {
        int status;

        if (myTDL_CMD.interval == 0) {
            status = EMPTY;         //empty
        } else if (myTDL_CMD.maxCount == 0xFFFF) {
            status = FINISH;         //test finished
        } else {
            status = PROGRESS;         //test in progress
        }

        return status;
    }

    @Override
    protected void onStop() {
        if(!mPassword.equals(givePassword.getText().toString())) {
            mPassword = givePassword.getText().toString();
            storePreferences();
        }
        super.onStop();
    }

    private void storePreferences() {
        SharedPreferences preferences = getSharedPreferences("password",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Password", mPassword);
        editor.commit();
        Log.i("Password stored", mPassword);
    }

    private void restorePreferences() {
        SharedPreferences preferences = getSharedPreferences("password",Activity.MODE_PRIVATE);
        mPassword = preferences.getString("Password", "#1234567");
        Log.i("Password restored", mPassword);
    }
}
