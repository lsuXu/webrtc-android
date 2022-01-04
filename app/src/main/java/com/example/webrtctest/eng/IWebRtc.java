package com.example.webrtctest.eng;

import com.example.webrtctest.bean.CallSession;

public interface IWebRtc {

    //接听
    void acceptCall(CallType callType,boolean isAudio);

    //拨号
    CallSession startCall(CallType callType, boolean isAudio);

    //挂断
    void callEnd();

    //切换到语音通话
    void switchToAudio();

    //切换相机
    void switchCamera();

    //开启预览
    void startPreview();

    //关闭摄像头
    void stopPreview();

    //静音
    void audioSilent(boolean silent);

    public enum CallType{
        //单聊
        SINGLE,
        //群聊
        GROUP;
    }
}
