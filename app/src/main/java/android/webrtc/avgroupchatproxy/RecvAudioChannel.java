package android.webrtc.avgroupchatproxy;

import android.content.Context;
import android.util.Log;
import android.webrtc.activity.R;
import android.webrtc.core.VoiceEngine;

/**
 * Created by kai on 2016/10/26.
 */
public class RecvAudioChannel {
    private final String TAG = "RecvAudioChannel";
    private int audioChannel;
    private boolean voeRecving;
    private int audioRxPort;
    private VoiceEngine voe;
    private Context context;

    public RecvAudioChannel(Context context){
        voeRecving = false;
        audioChannel = -1;
        this.context = context;
    }

    public int getChannel(){
        return audioChannel;
    }

    public boolean isVoiceRecving(){
        return voeRecving;
    }

    public void setVoiceEngine(VoiceEngine voe){
        this.voe = voe;
    }

    public int createChannel(){
        if(voe==null){
            Log.d(TAG,"voe is not init, Voice createChannel failed");
            return -1;
        }

        audioChannel = voe.createChannel();
        AVGC.publicCheck(audioChannel >= 0, "Failed voe CreateVoiceChannel");
        return audioChannel;
    }

    public void startVoiceRecv(){

        AVGC.publicCheck(!voeRecving, "VoE already started");
        if(voe==null){
            Log.d(TAG,"voe is not init, startVoiceRecv failed");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, startVoiceRecv failed");
            return;
        }

        if(context==null){
            Log.d(TAG,"context is invalid, startVoiceRecv failed");
            return;
        }

        if(voeRecving==false){
            //目前暂时关闭RTCP功能
            AVGC.publicCheck(voe.SetVoiceRTCPEnable(audioChannel,
                    context.getResources().getBoolean(R.bool.audio_rtcp_enable_default)) == 0,"Failed SetVoiceRTCPEnable");

            AVGC.publicCheck(voe.startListen(audioChannel) == 0, "Failed StartListen");
            AVGC.publicCheck(voe.startPlayout(audioChannel) == 0, "VoE start playout failed");
            voeRecving = true;
        }
    }

    public void stopVoiceRecv(){
        AVGC.publicCheck(voeRecving, "VoE is not Running");
        if(voe==null){
            Log.d(TAG,"voe is not init, stopVoiceRecv failed");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, stopVoiceRecv failed");
            return;
        }

        if(voeRecving==true){
            AVGC.publicCheck(voe.stopPlayout(audioChannel) == 0, "VoE stop playout failed");
            AVGC.publicCheck(voe.stopListen(audioChannel) == 0, "VoE stop listen failed");
            voeRecving = false;
        }
    }

    public void deleteChannel(){
        if(voe==null){
            Log.d(TAG,"voe is not init, deleteVideoChannel failed");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, deleteVideoChannel failed");
            return;
        }
        AVGC.publicCheck(voe.deleteChannel(audioChannel) == 0, "RemoveRenderer");
        audioChannel = -1;
    }

    public void setVoiceRxPort(int voiceRxPort){
        if(voe==null){
            Log.d(TAG,"voe is not init, setVoiceRxPort failed");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, setVoiceRxPort failed");
            return;
        }

        if(voiceRxPort<0){
            Log.d(TAG,"voiceRxPort is invalid, setVoiceRxPort failed");
            return;
        }

        this.audioRxPort = voiceRxPort;
        AVGC.publicCheck(voe.setLocalReceiver(audioChannel, audioRxPort)==0,"setVoiceRxPort failed");
    }

    public void dispose(){
        if(audioChannel>=0){
            deleteChannel();
        }

        if(voe!=null){
            voe = null;
        }

        if(context!=null){
            context = null;
        }

        return;
    }
}
