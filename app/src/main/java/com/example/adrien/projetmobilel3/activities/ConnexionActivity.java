package com.example.adrien.projetmobilel3.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.adrien.projetmobilel3.R;

import java.util.ArrayList;

public class ConnexionActivity extends Activity {

    private ArrayList<String> peers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        Intent parameters = getIntent();

        RadioButton radioButton = ((RadioButton) findViewById(R.id.radioButton));
        radioButton.setChecked(parameters.getBooleanExtra("isWifiP2pEnabled",false));

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

    public void onClickPeerDiscovered(View v) {
        setResult(RESULT_OK,new Intent()
                .putExtra("deviceName",((TextView) v).getText().toString())
                .putExtra("serverMode",false)
                .putExtra("localMode",false));
        finish();
    }

    public void onClickServerMode(View v) {
        setResult(RESULT_OK, new Intent()
                .putExtra("serverMode",true)
                .putExtra("localMode",false));
        finish();
    }

    public void onClickLocalMode(View v) {
        setResult(RESULT_OK, new Intent().
                putExtra("localMode",true));
        finish();
    }

    public void onClickRefresh(View v) {
        setResult(RESULT_OK, new Intent().
                putExtra("refresh",true));
        finish();
    }


    @Override
    public void onBackPressed() {

    }
}

