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


package com.kalewa.nfc.base;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.kalewa.nfc.R;

public class BaseNfcActivity extends AppCompatActivity {
    public static NfcAdapter mNfcAdapter;
    public static PendingIntent mPendingIntent;

    @Override
    protected void onStart() {
        super.onStart();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mNfcAdapter==null){//check NFC function
            Toast.makeText(this,getString(R.string.no_nfc),Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()){//check for NFC enabled
            Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(setNfc);

            Toast.makeText(this,getString(R.string.nfc_on),Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }


    @Override
    public void onResume() {
        super.onResume();
        //
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }


    @Override
    public void onPause() {
        super.onPause();
        //
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

}
