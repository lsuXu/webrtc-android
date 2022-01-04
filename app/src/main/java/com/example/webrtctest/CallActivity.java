package com.example.webrtctest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.webrtctest.bean.CallSession;
import com.example.webrtctest.bean.WSMessage;
import com.example.webrtctest.eng.CallStatusListener;
import com.example.webrtctest.eng.IMessageChannel;
import com.example.webrtctest.eng.IWebRtc;
import com.example.webrtctest.util.GsonUtil;

import org.webrtc.EglBase;
import org.webrtc.PeerConnection;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallActivity extends BaseActivity {

    private static final String TAG = CallActivity.class.getSimpleName();

    private ViewGroup localRendView,remoteRendView;

    WSClient wsClient ;

    private EditText remoteIdView;

    private TextView localIdView;

    ExecutorService executor = Executors.newFixedThreadPool(3);

    private String localId;

    private EglBase eglBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initWebSocket();
        //初始化
        WebRtcManager.init(this);
        //添加服务
        WebRtcManager.getInstance().addIceServer(PeerConnection.IceServer
                .builder("turn:turn.codewakeup.org:5349")
                .setUsername("shit")
                .setPassword("shit123")
                .createIceServer());
        //添加消息发送通道
        WebRtcManager.setMessageChannel(new IMessageChannel() {
            @Override
            public void send(Object msg) {
                if(!wsClient.isOpen()){
                    showToast("socket未连接");
                }else{
                    HashMap map = GsonUtil.toObject(GsonUtil.toJson(getWsMessage(msg)),HashMap.class);
                    Log.d(TAG, "send: " + GsonUtil.toJson(map));
                    wsClient.send(GsonUtil.toJson(map));
                }
            }

            @Override
            public void onMessage(String msg) {

            }
        });
        WebRtcManager.getInstance().setListener(new CallStatusListener() {
            @Override
            public void onCallSessionCrate(CallSession callSession) {
                remoteRendView.addView(callSession.getRemoteRendView());
                localRendView.addView(callSession.getLocalRendView());
            }

            @Override
            public void onReceiverCall() {
                new AlertDialog.Builder(CallActivity.this)
                        .setMessage(String.format("收到来自%s的通话邀请",remoteIdView.getText()))
                        .setNegativeButton("拒绝", (dialogInterface, i) -> dialogInterface.cancel())
                        .setPositiveButton("接收", (dialogInterface, i) -> WebRtcManager.getInstance().acceptCall(IWebRtc.CallType.SINGLE,false)).show();
            }
        });
    }

    private void initView(){
        eglBase = EglBase.create();
        localIdView = findViewById(R.id.tv_local_id);
        remoteIdView = findViewById(R.id.et_remote_id);
        localRendView = findViewById(R.id.view_local);
        remoteRendView = findViewById(R.id.view_remote);
        //拨号
        findViewById(R.id.btn_call).setOnClickListener(view -> {
            if(!wsClient.isOpen()){
                showToast("socket建立连接失败");
            }else{
                WebRtcManager.getInstance().startCall(IWebRtc.CallType.SINGLE,false);
            }
        });

        findViewById(R.id.btn_call_end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebRtcManager.getInstance().callEnd();
            }
        });

        localId = UUID.randomUUID().toString().substring(0,4);
        localIdView.setText(localId);
    }

    /**
     * 初始化长链接通道
     */
    private void initWebSocket(){

        executor.execute(() -> {
            URI uri = URI.create("wss://192.168.50.63/websocket/" + localId);

            wsClient = new WSClient(uri){
                @Override
                public void onWSMessage(WSMessage message) {
                    super.onWSMessage(message);
                    runOnUiThread(() -> {
                        //收到消息，记录发送人的用户ID
                        remoteIdView.setText(message.getUserId());
                        if(message.getMessage() instanceof String){
                            //将收到的消息通知到WebRtc模块
                            WebRtcManager.getInstance().onMessageReceive((String) message.getMessage());
                        }else{
                            //将收到的消息通知到WebRtc模块
                            WebRtcManager.getInstance().onMessageReceive(GsonUtil.toJson(message.getMessage()));
                        }

                    });
                }
            };

            try {
                wsClient.connectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "initWebSocket: " + wsClient.isOpen());

        });
    }

    private WSMessage getWsMessage(Object obj){
        String remoteId = remoteIdView.getText().toString();
        if(remoteId.isEmpty()){
            showToast("请先输入目标ID");
            return null;
        }else{
            return new WSMessage(localId,remoteId,obj);
        }
    }

}