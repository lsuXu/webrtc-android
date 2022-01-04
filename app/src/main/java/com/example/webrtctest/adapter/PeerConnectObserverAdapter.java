package com.example.webrtctest.adapter;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

public class PeerConnectObserverAdapter implements PeerConnection.Observer {
    
    private static final String TAG = PeerConnectObserverAdapter.class.getSimpleName();

    protected PeerConnection.IceConnectionState state;
    
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "onSignalingChange: ");
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "onIceConnectionChange: " + iceConnectionState.toString());
        this.state = iceConnectionState;
        switch (iceConnectionState){
            case NEW://ICE 代理正在搜集地址或者等待远程候选可用
                break;
            case CHECKING://ICE 代理已收到至少一个远程候选，并进行校验，无论此时是否有可用连接。同时可能在继续收集候选。
                break;
            case CONNECTED://ICE代理至少对每个候选发现了一个可用的连接，此时仍然会继续测试远程候选以便发现更优的连接。同时可能在继续收集候选。
                break;
            case COMPLETED:// ICE代理已经发现了可用的连接，不再测试远程候选。
                break;
            case FAILED://ICE候选测试了所有远程候选没有发现匹配的候选。也可能有些候选中发现了一些可用连接。
                break;
            case DISCONNECTED://测试不再活跃，这可能是一个暂时的状态，可以自我恢复。
                break;
            case CLOSED:// ICE代理关闭，不再应答任何请求。
                break;

        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "onIceConnectionReceivingChange: ");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "onIceGatheringChange: ");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "onIceCandidate: " + iceCandidate.toString());
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, "onIceCandidatesRemoved: ");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "onAddStream: ");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "onRemoveStream: ");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG, "onDataChannel: ");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded: ");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG, "onAddTrack: ");
    }
}
