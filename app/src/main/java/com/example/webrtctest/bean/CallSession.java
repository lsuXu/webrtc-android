package com.example.webrtctest.bean;

import org.webrtc.SurfaceViewRenderer;

public class CallSession {

    //本地视频流渲染目标
    private SurfaceViewRenderer localRendView;
    //远端视频流渲染目标
    private SurfaceViewRenderer remoteRendView;

    public SurfaceViewRenderer getLocalRendView() {
        return localRendView;
    }

    public void setLocalRendView(SurfaceViewRenderer localRendView) {
        this.localRendView = localRendView;
    }

    public SurfaceViewRenderer getRemoteRendView() {
        return remoteRendView;
    }

    public void setRemoteRendView(SurfaceViewRenderer remoteRendView) {
        this.remoteRendView = remoteRendView;
    }
}
