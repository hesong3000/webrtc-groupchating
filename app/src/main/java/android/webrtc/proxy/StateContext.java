package android.webrtc.proxy;

import android.content.Context;
import android.util.Log;
import android.webrtc.core.MediaEngine;
import android.webrtc.core.NativeWebRtcContextRegistry;
import android.widget.LinearLayout;


/**
 * Created by kai on 2016/8/1.
 */
public class StateContext {

    private LinearLayout primarySurace = null;
    private LinearLayout secondarySurface = null;
    private Context activityContext = null;
    private NativeWebRtcContextRegistry contextRegistry = null;
    private MediaEngine mediaEngine = null;
    private State mediaState = null;

    public final State STATE_IDLE = new StateIdle();
    public final State STATE_AUDIO = new StateAudio();
    public final State STATE_VIDEO = new StateVideo();
    public final State STATE_RECORD = new StateRecord();
    public final State STATE_PLAYOUT = new StatePlayout();
    public final State STATE_VIDEO_AUDIO = new StateVideo_Audio();

    public StateContext(Context context, LinearLayout primarySurace, LinearLayout secondarySurface){
        this.activityContext = context;
        this.primarySurace = primarySurace;
        this.secondarySurface = secondarySurface;

        Log.d("mysurface","StateContext b---------");
        Log.d("mysurface","primarySurace:"+primarySurace.toString());
        Log.d("mysurface","secondarySurface:"+secondarySurface.toString());
        Log.d("mysurface","StateContext e---------");

        onWebRTCInit();
        mediaState = STATE_IDLE;
    }

    public StateContext(Context context){
        this.activityContext = context;
        this.primarySurace = null;
        this.secondarySurface = null;

        onWebRTCInit();
        mediaState = STATE_IDLE;
    }

    public MediaEngine getEngine() {
        return mediaEngine;
    }

    public void setCurrent(State state){
        this.mediaState = state;
    }

    public State getCurrent(){
        return mediaState;
    }

    private void StateForceToIDLE(){
        getCurrent().forceTransToIdleState(this);
    }

    private void onWebRTCInit(){
        // State.
        // Must be instantiated before MediaEngine.

        contextRegistry = new NativeWebRtcContextRegistry();
        contextRegistry.register(activityContext);

        // Load all settings dictated in xml.
        mediaEngine = new MediaEngine(activityContext);
    }

    public void onWebRTCDepose(){
        if (getEngine().isRunning()) {
            Log.d("mysurface1","getEngine().isRunning StateForceToIDLE");
            StateForceToIDLE();
            getEngine().stop();
        }

        getEngine().dispose();
        contextRegistry.unRegister();
        contextRegistry = null;
        mediaEngine = null;
        primarySurace = null;
        secondarySurface = null;
    }

    public LinearLayout getSecondarySurface() {
        return secondarySurface;
    }

    public LinearLayout getPrimarySurace() {
        return primarySurace;
    }

    public Context getActivityContext() {
        return activityContext;
    }
}
