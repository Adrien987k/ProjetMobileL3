package com.example.adrien.projetmobilel3.common;

import android.view.MotionEvent;

import com.example.adrien.projetmobilel3.draw.Point;

import java.util.ArrayList;

/**
 * Created by MrkJudge on 08/03/2017.
 */

public interface PointTransmission {
    void addPointPacket(PointPacket pointPacket);
    void setStop(boolean stop);
}
