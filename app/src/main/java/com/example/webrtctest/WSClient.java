package com.example.webrtctest;

import android.util.Log;

import com.example.webrtctest.bean.WSMessage;
import com.example.webrtctest.util.GsonUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class WSClient extends WebSocketClient {

    private static final String TAG = WSClient.class.getSimpleName();

    public WSClient(URI serverUri) {
        super(serverUri);
        initSSL();
    }

    private void initSSL() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            this.setSocketFactory(factory);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "onOpen: ");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);
        onWSMessage(GsonUtil.toObject(message,WSMessage.class));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.d(TAG, "onError: " + ex.getMessage());
    }

    public void onWSMessage(WSMessage message){

    }
}
