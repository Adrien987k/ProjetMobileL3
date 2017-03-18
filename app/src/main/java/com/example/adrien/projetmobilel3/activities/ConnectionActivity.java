package com.example.adrien.projetmobilel3.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.adrien.projetmobilel3.R;

import java.util.ArrayList;

/**
 * The ConnectionActivity is started at the beginning of the application
 * to select the connection mode and to show information about the network status.
 * With some devices, this activity may not start when the WIFI is disabled.
 * In this case, the local mode will be enable and the draw activity will start.
 * This activity must be launch with the method DrawActivity.startConnectionActivity to
 * make sure that required information are put in the bundle.
 */
public class ConnectionActivity extends Activity {

    private boolean isWifiP2pEnabled;
    private boolean peerFound = true;
    private boolean groupFormed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        Intent parameters = getIntent();

        RadioButton radioButton = ((RadioButton) findViewById(R.id.radioButton));
        isWifiP2pEnabled = parameters.getBooleanExtra("isWifiP2pEnabled",false);
        radioButton.setChecked(isWifiP2pEnabled);

        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    buttonView.setTextColor(Color.GREEN);
                else
                    buttonView.setTextColor(Color.RED);
            }
        });

        String[] peersName = parameters.getStringArrayExtra("peersName");

        ListView lv = (ListView) findViewById(R.id.peersList);
        ArrayAdapter<String> as = new ArrayAdapter<>(this, R.layout.peer_item_adapter);
        lv.setAdapter(as);

        as.addAll(peersName);

        if (peersName.length == 0) {
            as.add("No device found.");
            peerFound = false;
        }

        String[] groupInfo = parameters.getStringArrayExtra("groupInformation");

        lv = (ListView) findViewById(R.id.groupInfo);
        as = new ArrayAdapter<>(this, R.layout.peer_item_adapter);
        lv.setAdapter(as);

        as.addAll(groupInfo);

        if(groupInfo.length == 0) {
            as.add("You are not part of a group.");
        }
    }

    /**
     * Return the activity, asking to connect with the device selected.
     * Do nothing if the wifi is disabled or if the user clicked on status message.
     */
    public void onClickPeerDiscovered(View v) {
        if(((TextView) v).getText().toString().equals(R.string.no_device_found)
                || ((TextView) v).getText().toString().equals(R.string.no_group)
                || !isWifiP2pEnabled
                || !peerFound
                || !groupFormed)
            return;
        setResult(RESULT_OK,new Intent()
                .putExtra("deviceName",((TextView) v).getText().toString())
                .putExtra("serverMode",false)
                .putExtra("localMode",false));
        finish();
    }

    /**
     * Return the activity, launching the server mode.
     * This feature is for group owner, then group's members
     * will be able to connect to his server.
     */
    public void onClickServerMode(View v) {
        setResult(RESULT_OK, new Intent()
                .putExtra("serverMode",true)
                .putExtra("localMode",false));
        finish();
    }

    /**
     * Return the activity, launching the local mode.
     */
    public void onClickLocalMode(View v) {
        setResult(RESULT_OK, new Intent().
                putExtra("localMode",true));
        finish();
    }

    /**
     * Return the activity, starting a new discovery.
     * This will launch this activity again.
     */
    public void onClickRefresh(View v) {
        setResult(RESULT_OK, new Intent().
                putExtra("refresh",true));
        finish();
    }

    /**
     * Do nothing on back pressed.
     */
    @Override
    public void onBackPressed() {

    }
}

