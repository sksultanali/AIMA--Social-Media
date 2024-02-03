package com.developerali.aima.Interfaces;

import android.view.View;

public class DoubleClickHandler implements View.OnClickListener{

    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Time between double-clicks in milliseconds
    private long lastClickTime = 0;
    private DoubleClickListener doubleClickListener;

    public DoubleClickHandler(DoubleClickListener listener) {
        this.doubleClickListener = listener;
    }


    @Override
    public void onClick(View view) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            doubleClickListener.onDoubleClick(view);
        }
        lastClickTime = clickTime;
    }
}
