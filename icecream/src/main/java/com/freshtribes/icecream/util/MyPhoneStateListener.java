package com.freshtribes.icecream.util;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.widget.ImageView;

import com.freshtribes.icecream.R;

/**
 * Created by Thinkpad on 2017/6/24.
 */

public class MyPhoneStateListener extends PhoneStateListener {
    private ImageView view;
    public static int signallevel = 0;

    public MyPhoneStateListener() {
        super();
    }

    public MyPhoneStateListener(ImageView view) {
        super();
        this.view = view;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
//        if (signalStrength.getGsmSignalStrength() <= 99 && signalStrength.getGsmSignalStrength() >= 79) {
//            //79-99
//            view.setImageResource(R.drawable.ic_state_5);
//        } else if (signalStrength.getGsmSignalStrength() < 79 && signalStrength.getGsmSignalStrength() >= 59) {
//            //59-79
//            view.setImageResource(R.drawable.ic_state_4);
//        } else if (signalStrength.getGsmSignalStrength() < 59 && signalStrength.getGsmSignalStrength() >= 39) {
//            //39-59
//            view.setImageResource(R.drawable.ic_state_3);
//        } else if (signalStrength.getGsmSignalStrength() < 39 && signalStrength.getGsmSignalStrength() >= 19) {
//            //19-39
//            view.setImageResource(R.drawable.ic_state_2);
//        } else if (signalStrength.getGsmSignalStrength() < 19 && signalStrength.getGsmSignalStrength() >= 0) {
//            //0-19
//            view.setImageResource(R.drawable.ic_state_1);
//        } else {
//            //0
//            view.setImageResource(R.drawable.ic_state_0);
//        }

        if (!signalStrength.isGsm()) {
            int dBm = signalStrength.getCdmaDbm();
            //LogUtils.i("dBm="+dBm);
            if (dBm >= -75) {
                //79-99
                view.setImageResource(R.drawable.ic_state_5);
                signallevel = 5;
            } else if (dBm >= -85) {
                //59-79
                view.setImageResource(R.drawable.ic_state_4);
                signallevel = 4;
            } else if (dBm >= -95) {
                //39-59
                view.setImageResource(R.drawable.ic_state_3);
                signallevel = 3;
            } else if (dBm >= -100) {
                //19-39
                view.setImageResource(R.drawable.ic_state_2);
                signallevel = 2;
            } else if (dBm >= -105) {
                //19-39
                view.setImageResource(R.drawable.ic_state_1);
                signallevel = 1;
            } else {
                //0-19
                view.setImageResource(R.drawable.ic_state_0);
                signallevel = 0;
            }
        } else {
            int asu = signalStrength.getGsmSignalStrength();
            //LogUtils.i("asu="+asu);
            if (asu < 0 || asu >= 99){
                view.setImageResource(R.drawable.ic_state_0);
                signallevel = 0;
            }
            else if (asu >= 16){
                view.setImageResource(R.drawable.ic_state_5);
                signallevel = 5;
            }
            else if (asu >= 9) {
                view.setImageResource(R.drawable.ic_state_4);
                signallevel = 4;
            }
            else if (asu >= 6){
                view.setImageResource(R.drawable.ic_state_3);
                signallevel = 3;
            }
            else if (asu >= 3){
                view.setImageResource(R.drawable.ic_state_2);
                signallevel = 2;
            }
            else {
                view.setImageResource(R.drawable.ic_state_1);
                signallevel = 1;
            }
        }
    }
}