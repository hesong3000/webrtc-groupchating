package android.webrtc.proxy;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by kai on 2016/8/1.
 */
public class WebRTCProxy {

    private static WebRTCProxy proxy = new WebRTCProxy();

    public StateContext getState_context() {
        return state_context;
    }

    private StateContext state_context = null;

    public static WebRTCProxy getInstance(){
        return proxy;
    }

    private WebRTCProxy(){
    }

    public void onWebRTCInit(Context context, LinearLayout primarySurace, LinearLayout secondarySurface){
        state_context = new StateContext(context, primarySurace, secondarySurface);
    }

    public void onWebRTCInit(Context context){
        state_context = new StateContext(context);
    }

    public void onWebRTCDepose(){
        if(state_context!=null) {
            Log.d("mysurface1","state_context.onWebRTCDepose");
            state_context.onWebRTCDepose();
            state_context = null;
        }
    }

    public String startVideoCallAction(Bundle params){
        return state_context.getCurrent().startVideoCall(state_context, params);
    }

    public String stopVideoCallAction(){
        return state_context.getCurrent().stopVideoCall(state_context);
    }

    public String startAudioCallAction(Bundle params){
        return state_context.getCurrent().startAudioCall(state_context, params);
    }

    public String stopAudioCallAction(){
        return state_context.getCurrent().stopAudioCall(state_context);
    }

    public String startAudioRecordAction(Bundle params){
        return state_context.getCurrent().startAudioRecord(state_context, params);
    }

    public String stopAudioRecordAction(){
        return state_context.getCurrent().stopAudioRecord(state_context);
    }

    public String startAudioPlayoutAction(Bundle params){
        return state_context.getCurrent().startAudioPlayout(state_context, params);
    }

    public String stopAudioPlayoutAction(){
        return state_context.getCurrent().stopAudioPlayout(state_context);
    }

    public String setMicMuteAction(){
        return state_context.getCurrent().setMicMute(state_context);
    }

    public String resumeMicMuteAction(){
        return state_context.getCurrent().resumeMicMute(state_context);
    }

    public String setRenderMute(){
        return state_context.getCurrent().setRenderMute(state_context);
    }

    public String resumeRenderMute(){
        return state_context.getCurrent().resumeRenderMute(state_context);
    }

    public String switchCameraFacing(){
        return state_context.getCurrent().switchCameraFacing(state_context);
    }

    public String switchRenderView(){
        return state_context.getCurrent().switchRenderView(state_context);
    }

    public String switchToAudioCall(){
        return state_context.getCurrent().switchToAudioCall(state_context);
    }

    public String setLoudSpeakerOn(){
        return state_context.getCurrent().setLoudSpeakerOn(state_context);
    }

    public String setLoudSpeakerOff(){
        return state_context.getCurrent().setLoudSpeakerOff(state_context);
    }

    public String startLocalCapture(){
        return state_context.getCurrent().startLocalCapture(state_context);
    }

    public String stopLocalCapture(){
        return state_context.getCurrent().stopLocalCapture(state_context);
    }
}
