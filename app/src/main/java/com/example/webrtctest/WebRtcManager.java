package com.example.webrtctest;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.webrtctest.adapter.IceCandidateRemote;
import com.example.webrtctest.adapter.PeerConnectObserverAdapter;
import com.example.webrtctest.adapter.SdpObserverAdapter;
import com.example.webrtctest.bean.CallSession;
import com.example.webrtctest.eng.CallStatusListener;
import com.example.webrtctest.eng.IMessageChannel;
import com.example.webrtctest.eng.IWebRtc;
import com.example.webrtctest.util.GsonUtil;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRtcManager implements IWebRtc {

    public static final String TAG = WebRtcManager.class.getSimpleName();

    private static EglBase eglBase;

    private static PeerConnectionFactory mPeerConnectionFactory;

    private static WebRtcManager INSTANCE;

    private static Context context;

    //消息通道
    private static IMessageChannel messageChannel;

    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    //当前会话产生到连接
    private PeerConnection peerConnection;
    //媒体流
    private MediaStream localMediaStream;
    //当会话
    private CallSession currentSession;

    private CallStatusListener listener;

    public static WebRtcManager getInstance() {
        if (INSTANCE == null) {
            synchronized (WebRtcManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WebRtcManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化Webrtc相关内容
     *
     * @param context
     */
    public static void init(Context context) {

        WebRtcManager.context = context;
        WebRtcManager.eglBase = EglBase.create();

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        //创建audio device module
        AudioDeviceModule audioDeviceModule = JavaAudioDeviceModule.builder(context)
                .setSamplesReadyCallback(null)
                .setUseHardwareNoiseSuppressor(true)//噪音消除
                .setAudioRecordErrorCallback(null)
                .setAudioTrackErrorCallback(null)
                .setUseHardwareAcousticEchoCanceler(true)//回声消除
                .createAudioDeviceModule();

        final boolean enableH264HighProfile = true;
        //视频编码器
        final VideoEncoderFactory encoderFactory;
        //视频解码器
        final VideoDecoderFactory decoderFactory;

        //编码器
        encoderFactory = new DefaultVideoEncoderFactory(
                eglBase.getEglBaseContext(), false /* enableIntelVp8Encoder */, enableH264HighProfile);
        //解码器
        decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());

        //创建PeerConnectionFactory
        mPeerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(audioDeviceModule)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        audioDeviceModule.release();
    }


    @Override
    public void acceptCall(CallType callType, boolean isAudio) {
        currentSession = createSession(isAudio);
        if(listener != null){
            listener.onCallSessionCrate(currentSession);
        }
        Map data = new HashMap();
        data.put("type", "start");

        sendMessage(data);
    }

    @Override
    public CallSession startCall(CallType callType, boolean isAudio) {
        currentSession = createSession(isAudio);
        if(listener != null){
            listener.onCallSessionCrate(currentSession);
        }
        Map type = new HashMap();
        type.put("type", "connect");
        sendMessage(type);
        return currentSession;
    }

    //结束通话
    @Override
    public void callEnd() {
        while (!localMediaStream.videoTracks.isEmpty()) {
            localMediaStream.videoTracks.remove(0).dispose();
        }
        peerConnection.removeStream(localMediaStream);
        peerConnection.close();
        peerConnection = null;
    }

    @Override
    public void switchToAudio() {

    }

    @Override
    public void switchCamera() {

    }

    @Override
    public void startPreview() {

    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void audioSilent(boolean silent) {

    }


    /**
     * 消息接收
     *
     * @param msg
     */
    public void onMessageReceive(String msg) {
        Map<String, String> data = GsonUtil.toObject(msg, HashMap.class);
        switch (data.get("type").toLowerCase()) {
            case "start":
                //创建请求连接
                createOfferConnect();
                break;
            case "icecandidate":
                IceCandidateRemote remote =
                        GsonUtil.toObject(GsonUtil.toJson(data.get("icecandidate")), IceCandidateRemote.class);
                boolean result = peerConnection.addIceCandidate(remote.toIceCandidate());
                Log.d(TAG, "addIceCandidate: " + result);
                break;
            case "connect":
                if(listener != null){
                    listener.onReceiverCall();
                }
                break;
            case "offer":
                //响应应答
                createAnswerConnect(data.get("sdp"));
                break;
            case "answer":
                peerConnection.setRemoteDescription(new SdpObserverAdapter(), new SessionDescription(SessionDescription.Type.ANSWER, data.get("sdp")));
                break;
            default:
                break;
        }
    }

    /**
     * 消息发送
     *
     * @param msg
     */
    public void sendMessage(Object msg) {
        if(messageChannel != null){
            messageChannel.send(msg);
        }
    }

    public void setListener(CallStatusListener listener) {
        this.listener = listener;
    }

    public static void setMessageChannel(IMessageChannel messageChannel) {
        WebRtcManager.messageChannel = messageChannel;
    }

    public void addIceServer(String uri, String username, String password) {
        addIceServer(PeerConnection.IceServer
                .builder(uri)
                .setUsername(username)
                .setPassword(password)
                .createIceServer());
    }

    public boolean addIceServer(PeerConnection.IceServer iceServer) {
        return this.iceServers.add(iceServer);
    }

    public boolean removeIceServer(PeerConnection.IceServer iceServer) {
        return this.iceServers.remove(iceServer);
    }

    public CallSession getCurrentSession() {
        return currentSession;
    }

    /**
     * 创建请求连接
     */
    private void createOfferConnect() {
        peerConnection = createConnect();
        peerConnection.createOffer(new SdpObserverAdapter() {

            SessionDescription sessionDescription;

            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                this.sessionDescription = sessionDescription;
                peerConnection.setLocalDescription(this, sessionDescription);
            }

            @Override
            public void onSetSuccess() {
                super.onSetSuccess();
                HashMap data = new HashMap();
                data.put("type", sessionDescription.type.toString().toLowerCase());
                data.put("sdp", sessionDescription.description);
                sendMessage(data);
            }
        }, new MediaConstraints());

    }

    /**
     * 创建应答连接
     *
     * @param sdp
     */
    private void createAnswerConnect(String sdp) {
        peerConnection = createConnect();
        peerConnection.setRemoteDescription(new SdpObserverAdapter() {
            @Override
            public void onSetSuccess() {
                super.onSetSuccess();
                peerConnection.createAnswer(this, new MediaConstraints());
            }

            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                peerConnection.setLocalDescription(new SdpObserverAdapter() {
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        Log.d(TAG, "onSetSuccess: 设置成功");
                    }
                }, sessionDescription);
                HashMap data = new HashMap();
                data.put("type", sessionDescription.type.toString().toLowerCase());
                data.put("sdp", sessionDescription.description);
                sendMessage(data);
            }
        }, new SessionDescription(SessionDescription.Type.OFFER, sdp));

    }

    /**
     * 创建连接
     * @return
     */
    private PeerConnection createConnect() {

        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnection peerConnection = mPeerConnectionFactory.createPeerConnection(configuration, new PeerConnectObserverAdapter() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Map data = new HashMap();
                data.put("type", "icecandidate");
                data.put("icecandidate", IceCandidateRemote.getIceCandidateRemote(iceCandidate));
                sendMessage(GsonUtil.toJson(data));
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                if (!mediaStream.videoTracks.isEmpty()) {
                    mediaStream.videoTracks.get(0).addSink(currentSession.getRemoteRendView());
                }
            }
        });
        localMediaStream = mPeerConnectionFactory.createLocalMediaStream("localMediaStream");
        AudioTrack audioTrack = createAudioTrack();
        VideoTrack videoTrack = createVideoTrack();

        localMediaStream.addTrack(videoTrack);
        localMediaStream.addTrack(audioTrack);

//        peerConnection.addTrack(audioTrack);
//        peerConnection.addTrack(videoTrack);
        peerConnection.addStream(localMediaStream);
        return peerConnection;
    }

    private CallSession createSession(boolean isAudio) {
        //语音通话不需要创建渲染画布
        CallSession session = new CallSession();
        if (!isAudio) {
            //本地视频
            SurfaceViewRenderer localView = new SurfaceViewRenderer(context);
            localView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            localView.init(eglBase.getEglBaseContext(),null);
            session.setLocalRendView(localView);
            //远端视频
            SurfaceViewRenderer remoteView = new SurfaceViewRenderer(context);
            remoteView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            remoteView.init(eglBase.getEglBaseContext(),null);
            session.setRemoteRendView(remoteView);
        }
        return session;
    }

    /**
     * VideoSource为视频源，通过核心类PeerConnectionFactory创建，
     * VideoTrack是对VideoSource的包装，可以方便的将视频源在本地进行播放，
     * 添加到MediaStream中进行网络传输。
     *
     * @return
     */
    private VideoTrack createVideoTrack() {
        //参数说明是否为屏幕录制
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(true);
        CameraVideoCapturer videoCapture = CommonUtils.createVideoCapture(context);
        if (videoCapture != null) {
            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglBase.getEglBaseContext());
            videoSource = mPeerConnectionFactory.createVideoSource(videoCapture.isScreencast());
            videoCapture.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
            videoCapture.startCapture(1080, 1920, 60);
        }
        VideoTrack videoTrack = mPeerConnectionFactory.createVideoTrack("videoTrack", videoSource);
        videoTrack.addSink(currentSession.getLocalRendView());
        videoTrack.setEnabled(true);
        return videoTrack;
    }

    private AudioTrack createAudioTrack() {
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        return mPeerConnectionFactory.createAudioTrack("audiotrack", audioSource);
    }

}
