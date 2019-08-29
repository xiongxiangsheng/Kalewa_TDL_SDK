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
package com.kalewa.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kalewa.nfc.ui.ReadTextActivity;

import java.util.Locale;

/**
 * Created by gc on 2016/12/8.
 */
public class MainActivity extends AppCompatActivity {
    CheckBox mCheck;
    Button mBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchActivity(position);
            }
        });*/
        mBtn = findViewById(R.id.mStart);
        //mBtn = findViewById(R.id.mStart);
        mBtn.setText(R.string.agree);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ReadTextActivity.class);
                intent.putExtra("Kalewa", "NHS3100");
                MainActivity.this.startActivity(intent);
            }
        });

        TextView mText = findViewById(R.id.mLicense);
        String mDeclare = "Licensee hereby confirms to have read and understood the terms and conditions of the Kalewa Temperature Data Logger App Software Evaluation License Agreement.";
        mDeclare += "\nLicensee hereby agrees with the terms and conditions of the Kalewa Temperature Data Logger App Software Evaluation License Agreement.";
        mText.setText(mDeclare);

        mCheck = findViewById(R.id.checkBox);
        mCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCheck.isChecked()){
                    mBtn.setEnabled(true);
                }else{
                    mBtn.setEnabled(false);
                }
            }
        });
    }

    private void ChangeLanguage(int i) {
        //Context context = getBaseContext();
        Context context = getApplicationContext();

        // in APP config Language
        Resources resources = getResources();                    // get res object
        Configuration config = resources.getConfiguration();     // get setting object
        DisplayMetrics dm = resources.getDisplayMetrics();       // get screen parameters
        switch (i) {
            case 0:
                config.locale = Locale.ENGLISH;     // 英文
                break;
            case 1:
                config.locale = Locale.SIMPLIFIED_CHINESE;   // 简体中文
                break;
            case 2:
                config.locale = Locale.getDefault();         // 系统默认语言
                break;
            default:
                break;
        }
        // reboot Activiy
        resources.updateConfiguration(config, dm);
        ((Activity) context).finish();
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        context.startActivity(intent);
    }

}
