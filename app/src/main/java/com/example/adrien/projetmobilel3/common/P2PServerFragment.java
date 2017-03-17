package com.example.adrien.projetmobilel3.common;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.adrien.projetmobilel3.activities.MainActivity;
import com.example.adrien.projetmobilel3.server.ServerP2P;

/**
 * Created by MrkJudge on 17/03/2017.
 */

public class P2PServerFragment extends Fragment {


    private MainActivity mainActivity;
    private ServerP2P server;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public void setServer(ServerP2P server) {
        this.server = server;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return null;
    }
}
