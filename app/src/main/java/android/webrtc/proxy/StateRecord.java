package android.webrtc.proxy;

import android.os.Bundle;
import android.util.Log;

/**
 * Created by kai on 2016/8/1.
 */
public class StateRecord implements State {
    @Override
    public String startVideoCall(StateContext context, Bundle params) {
        Log.d("webrtc","record state do not support startVideoCall");
        return stateError;
    }

    @Override
    public String stopVideoCall(StateContext context) {
        Log.d("webrtc","record state do not support stopVideoCall");
        return stateError;
    }

    @Override
    public String startAudioCall(StateContext context, Bundle params) {
        Log.d("webrtc","record state do not support startAudioCall");
        return stateError;
    }

    @Override
    public String stopAudioCall(StateContext context) {
        Log.d("webrtc","record state do not support stopAudioCall");
        return stateError;
    }

    @Override
    public String startAudioRecord(StateContext context, Bundle params) {
        Log.d("webrtc","record state do not support startAudioRecord");
        return stateError;
    }

    @Override
    public String stopAudioRecord(StateContext context) {
        Log.d("webrtc","StateRecord invoke stopAudioRecord");
        if(context == null){
            Log.d("webrtc","StateContext has non pointer");
            return stateError;
        }

        context.getEngine().stopAudioRecord();
        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String startAudioPlayout(StateContext context, Bundle params) {
        Log.d("webrtc","record state do not support startAudioPlayout");
        return stateError;
    }

    @Override
    public String stopAudioPlayout(StateContext context) {
        Log.d("webrtc","record state do not support stopAudioPlayout");
        return stateError;
    }

    @Override
    public String setMicMute(StateContext context) {
        Log.d("webrtc","record state do not support setMicMute");
        return stateError;
    }

    @Override
    public String resumeMicMute(StateContext context) {
        Log.d("webrtc","record state do not support resumeMicMute");
        return stateError;
    }

    @Override
    public String setRenderMute(StateContext context) {
        Log.d("webrtc","record state do not support setRenderMute");
        return stateError;
    }

    @Override
    public String resumeRenderMute(StateContext context) {
        Log.d("webrtc","record state do not support resumeRenderMute");
        return stateError;
    }

    @Override
    public String switchCameraFacing(StateContext context) {
        Log.d("webrtc","record state do not support switchCameraFacing");
        return stateError;
    }

    @Override
    public String switchRenderView(StateContext context) {
        Log.d("webrtc","record state do not support switchRenderView");
        return stateError;
    }

    @Override
    public String switchToAudioCall(StateContext context) {
        Log.d("webrtc","record state do not support switchToAudioCall");
        return stateError;
    }

    @Override
    public String forceTransToIdleState(StateContext context) {
        context.getEngine().stopAudioRecord();
        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String invokeStateInit(StateContext context) {
        return stateOK;
    }

    @Override
    public String startLocalCapture(StateContext context) {
        Log.d("webrtc","record state do not support startLocalCapture");
        return stateError;
    }

    @Override
    public String stopLocalCapture(StateContext context) {
        Log.d("webrtc","record state do not support stopLocalCapture");
        return stateError;
    }

    @Override
    public String setLoudSpeakerOn(StateContext context) {
        Log.d("webrtc","record state do not support setLoudSpeakerOn");
        return stateError;
    }

    @Override
    public String setLoudSpeakerOff(StateContext context) {
        Log.d("webrtc","record state do not support setLoudSpeakerOff");
        return stateError;
    }
}
