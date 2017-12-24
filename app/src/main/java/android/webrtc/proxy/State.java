package android.webrtc.proxy;

import android.os.Bundle;

/**
 * Created by kai on 2016/8/1.
 */
public interface State {

    public static final String stateError = "StateError";
    public static final String stateOK = "StateOK";

    public String startVideoCall(StateContext context, Bundle params);
    public String stopVideoCall(StateContext context);
    public String startAudioCall(StateContext context, Bundle params);
    public String stopAudioCall(StateContext context);
    public String startAudioRecord(StateContext context, Bundle params);
    public String stopAudioRecord(StateContext context);
    public String startAudioPlayout(StateContext context, Bundle params);
    public String stopAudioPlayout(StateContext context);
    public String setMicMute(StateContext context);
    public String resumeMicMute(StateContext context);
    public String setLoudSpeakerOn(StateContext context);
    public String setLoudSpeakerOff(StateContext context);
    public String setRenderMute(StateContext context);
    public String resumeRenderMute(StateContext context);
    public String switchCameraFacing(StateContext context);
    public String switchRenderView(StateContext context);
    public String switchToAudioCall(StateContext context);
    public String forceTransToIdleState(StateContext context);
    public String invokeStateInit(StateContext context);
    public String startLocalCapture(StateContext context);
    public String stopLocalCapture(StateContext context);
}
