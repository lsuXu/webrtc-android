package com.example.webrtctest.eng;

import com.example.webrtctest.bean.CallSession;

public interface CallStatusListener {

    void onCallSessionCrate(CallSession callSession);

    void onReceiverCall();
}
