package com.example.webrtctest.adapter;

import org.webrtc.IceCandidate;

public class IceCandidateRemote {

    public String candidate;

    public int sdpMLineIndex;

    public String sdpMid;

    public IceCandidateRemote() {

    }

    public IceCandidateRemote(String candidate, int sdpMLineIndex, String sdpMid) {
        this.candidate = candidate;
        this.sdpMLineIndex = sdpMLineIndex;
        this.sdpMid = sdpMid;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(int sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public IceCandidate toIceCandidate(){
        return new IceCandidate(sdpMid,sdpMLineIndex,candidate);
    }

    public static IceCandidateRemote getIceCandidateRemote(IceCandidate candidate){
        return new IceCandidateRemote(candidate.sdp, candidate.sdpMLineIndex, candidate.sdpMid);
    }
}
