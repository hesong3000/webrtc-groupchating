package android.webrtc.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webrtc.proxy.WebRTCProxy;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by kai on 2016/7/22.
 */
public class MyWebRTCDemo extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MyWebRTCDemoActivity";

    private LinearLayout primarySurace;
    private LinearLayout secondarySurface;
    private LinearLayout ll_top_container;
    private LinearLayout ll_surface_baseline;
    private ImageView makeVideoCall_btn;
    private ImageView switchCamera_btn;
    private ImageView switchToAudio_btn;
    private ImageView dropVideoCall_btn;
    private Bundle paramsBundle;

    private boolean isControlsShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_call);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Log.d("mysurface","MyWebRTC onCreate");
        primarySurace = (LinearLayout) findViewById(R.id.videocall_primary_surface);
        secondarySurface = (LinearLayout)findViewById(R.id.videocall_secondray_surface);
        ll_top_container = (LinearLayout)findViewById(R.id.ll_top_container);
        ll_surface_baseline = (LinearLayout)findViewById(R.id.ll_surface_baseline);

        makeVideoCall_btn = (ImageView)findViewById(R.id.btn_answer_call);
        switchCamera_btn = (ImageView)findViewById(R.id.btn_switch_camera);
        switchToAudio_btn = (ImageView)findViewById(R.id.btn_switchto_audio);
        dropVideoCall_btn = (ImageView)findViewById(R.id.btn_refuse_call);

        secondarySurface.setOnClickListener(this);
        makeVideoCall_btn.setOnClickListener(this);
        switchCamera_btn.setOnClickListener(this);
        switchToAudio_btn.setOnClickListener(this);
        primarySurace.setOnClickListener(this);
        dropVideoCall_btn.setOnClickListener(this);

        paramsBundle = this.getIntent().getExtras();

        isControlsShow = true;

        WebRTCProxy.getInstance().onWebRTCInit(this,primarySurace,secondarySurface);
        //WebRTCProxy.getInstance().startLocalCapture();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("webrtc","MyWebRTCDemo onStart()");
        WebRTCProxy.getInstance().startLocalCapture();
    }

    private int toggleStartVideoCall(Bundle bundle) {
        if(WebRTCProxy.getInstance().startVideoCallAction(bundle).equals("StateOK")){
            return 0;
        }
        else{
            return -1;
        }
    }

    private int toggleStartAudioCall(Bundle bundle){
        if(WebRTCProxy.getInstance().startAudioCallAction(bundle).equals("StateOK")){
            return 0;
        }
        else{
            return -1;
        }
    }

    public void toggleStopCall() {
        WebRTCProxy.getInstance().stopVideoCallAction();
        finish();
    }

    //WebRTC资源析构b-----------
    public void onWebRTCDepose() {
        WebRTCProxy.getInstance().onWebRTCDepose();
    }
    //WebRTC资源析构e-----------

    //点击事件实现b---------
    private void makeVideoCallClickAction() {
        if (WebRTCProxy.getInstance().getState_context().getEngine().isRunning()) {
            //toggleStopCall();
            //makeVideoCall_btn.setImageResource(R.drawable.icon_answer_call);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("remoteIP", paramsBundle.getString("remoteIP"));
            bundle.putInt("localRecvPort_A", paramsBundle.getInt("localPort_A"));
            bundle.putInt("remoteSendPort_A", paramsBundle.getInt("remotePort_A"));
            bundle.putInt("ssrc_A", paramsBundle.getInt("AudioSSRC"));
            bundle.putInt("localRecvPort_V", paramsBundle.getInt("localport_V"));
            bundle.putInt("remoteSendPort_V", paramsBundle.getInt("remotePort_V"));
            bundle.putInt("ssrc_V", paramsBundle.getInt("VideoSSRC"));

            WebRTCProxy.getInstance().stopLocalCapture();

            if (toggleStartVideoCall(bundle) < 0) {
                Log.d("exception", "Fail to startVideoCall");
                return;
            }

            if (toggleStartAudioCall(bundle) < 0) {
                Log.d("exception", "Fail to startAudioCall");
                return;
            }

            //makeVideoCall_btn.setImageResource(R.drawable.icon_refuse_call);
            ll_top_container.setVisibility(View.GONE);
            //ll_surface_baseline.setVisibility(View.GONE);
            //isControlsShow = false;
        }
    }

    private void switchCameraClickAction() {
        WebRTCProxy.getInstance().switchCameraFacing();
    }

    private void switchViewClickAction(){
        WebRTCProxy.getInstance().switchRenderView();
    }

    private void switchToAudioClickAction(){
        WebRTCProxy.getInstance().switchToAudioCall();
    }
    //点击事件实现e---------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_answer_call:
                makeVideoCallClickAction();
                break;
            case R.id.btn_switch_camera:
                switchCameraClickAction();
                break;
            case R.id.videocall_secondray_surface:
                switchViewClickAction();
                break;
            case R.id.btn_switchto_audio:
                switchToAudioClickAction();
                break;
            case R.id.btn_refuse_call:
                toggleStopCall();
                break;
            case R.id.videocall_primary_surface:
                /*
                isControlsShow = !isControlsShow;
                if(isControlsShow==true) {
                    ll_top_container.bringToFront();
                    ll_top_container.setVisibility(View.VISIBLE);
                    ll_surface_baseline.bringToFront();
                    ll_surface_baseline.setVisibility(View.VISIBLE);
                }
                else{
                    ll_top_container.setVisibility(View.GONE);
                    ll_surface_baseline.setVisibility(View.GONE);
                }
                */
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("mysurface","MyWebRTCDemo onDestroy");
        onWebRTCDepose();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d("mysurface","MyWebRTCDemo onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("mysurface","MyWebRTCDemo onStop");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
