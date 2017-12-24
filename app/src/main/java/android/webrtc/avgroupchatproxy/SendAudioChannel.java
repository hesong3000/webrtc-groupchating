package android.webrtc.avgroupchatproxy;

import android.content.Context;
import android.util.Log;
import android.webrtc.core.CodecInst;
import android.webrtc.core.VoiceEngine;

/**
 * Created by kai on 2016/10/31.
 */
public class SendAudioChannel {
    private final String TAG = "SendVideoChannel";
    private Context context;
    private int audioChannel;
    private boolean voeSending;
    private VoiceEngine voe;
    private String remoteIp;
    private int remotePort;
    private int SSRC;
    private int audioCodecIndex;

    public SendAudioChannel(Context context){
        voeSending = false;
        audioChannel = -1;
        this.context = context;
    }

    public int getChannel(){
        return audioChannel;
    }

    public boolean isVoiceSending(){
        return voeSending;
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

    public void setAudioCodec(int codecNumber){

        if(voe==null){
            Log.d(TAG,"voe is not init, setAudioCodec failed");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, setAudioCodec failed");
            return;
        }
        audioCodecIndex = codecNumber;
        CodecInst codec = voe.getCodec(codecNumber);
        AVGC.publicCheck(voe.setSendCodec(audioChannel, codec) == 0, "Failed setSendCodec");
        codec.dispose();
    }

    public void setDestionaton(String remoteIP, int remotePort){
        this.remoteIp = remoteIP;
        this.remotePort = remotePort;

        if(voe==null){
            Log.d(TAG,"voe is not init, setDestionaton failed");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, setDestionaton failed");
            return;
        }

        AVGC.publicCheck(voe.setSendDestination(audioChannel, remotePort, remoteIp) == 0,
                "Failed setSendDestination");
    }

    public void setSSRC(int SSRC) {
        this.SSRC = SSRC;

        if(voe==null){
            Log.d(TAG,"voe is not init, setSSRC failed");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, setSSRC failed");
            return;
        }

        AVGC.publicCheck(voe.setLocalSSRC(audioChannel,SSRC) == 0,"Failed setVideoSSRC");
    }

    public void startVoiceSend(){
        if(voeSending==true){
            Log.d(TAG,"VoE is already sending, startVoiceSend failed!!");
            return;
        }

        if(voe==null){
            Log.d(TAG,"voe is not init, startVoiceSend failed!");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, startVoiceSend failed!");
            return;
        }

        AVGC.publicCheck(voe.startSend(audioChannel) == 0, "VoE start send failed");
        voeSending = true;
    }

    public void stopVoiceSend(){
        if(voeSending==false){
            Log.d(TAG,"VoE is not in sending, stopVoiceSend failed!");
            return;
        }

        if(voe==null){
            Log.d(TAG,"voe is not init, stopVoiceSend failed!");
            return;
        }

        if(audioChannel<0){
            Log.d(TAG,"audioChannel is invalid, stopVoiceSend failed!");
            return;
        }

        AVGC.publicCheck(voe.stopSend(audioChannel) == 0, "StopSend");
        voeSending = false;
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
