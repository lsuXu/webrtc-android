package com.example.webrtctest.adapter;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SdpObserverAdapter implements SdpObserver {

    private static final String TAG = SdpObserverAdapter.class.getSimpleName();

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "onCreateSuccess: " + sessionDescription.type);
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess: ");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "onCreateFailure: " +s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "onSetFailure: " + s);
    }
}
