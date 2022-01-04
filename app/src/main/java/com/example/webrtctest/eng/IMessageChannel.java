package com.example.webrtctest.eng;

public interface IMessageChannel {

    void send(Object msg);

    void onMessage(String msg);

}
