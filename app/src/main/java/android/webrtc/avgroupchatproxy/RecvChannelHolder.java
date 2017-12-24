package android.webrtc.avgroupchatproxy;

import android.content.Context;
import android.util.Log;
import android.webrtc.core.AVGroupEngine;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by kai on 2016/10/26.
 */
public class RecvChannelHolder {
    private final String TAG = "RecvChannelHolder";
    private RecvVideoChannel vic;
    private RecvAudioChannel voc;
    private RelativeLayout parentLayout = null;
    private UserInfo mUserInfo = null;

    public RecvChannelHolder(){
        vic = null;
        voc = null;
        parentLayout = null;
        mUserInfo = null;
    }

    public void setUserInfo(UserInfo userInfo){
        mUserInfo = new UserInfo(userInfo);
    }

    public int createVideoChannel(AVGroupEngine mediaEngine, Context context){
        Log.d(TAG,"createVideoChannel");
        vic = new RecvVideoChannel(context);
        vic.setVideoEngine(mediaEngine.getVie());
        return vic.createChannel();
    }

    public int createVoiceChannel(AVGroupEngine mediaEngine, Context context){
        Log.d(TAG,"createVoiceChannel");
        voc = new RecvAudioChannel(context);
        voc.setVoiceEngine(mediaEngine.getVoe());
        return voc.createChannel();
    }

    public void connectVoiceChannel(){
        Log.d(TAG,"connectVoiceChannel");
        if((vic!=null)&&(voc!=null)&&(vic.getChannel()>=0)&&(voc.getChannel()>=0)){
            vic.connectVoiceChannel(voc.getChannel());
        }
    }

    public void setParentLayout(RelativeLayout layout){
        parentLayout = layout;
    }

    public RelativeLayout getParentLayout(){
        return parentLayout;
    }

    public void startVideoRecv(){
        if((vic!=null)&&(parentLayout!=null)) {
            Log.d(TAG,"startVideoRecv");
            vic.startVideoRecv();
            vic.addView(parentLayout);
        }
    }

    public void stopVideoRecv(){
        if(vic!=null) {
            Log.d(TAG,"stopVideoRecv");
            vic.stopVideoRecv();
            vic.removeView();
        }
    }

    public void dispose(){
        if(vic!=null){
            if(vic.isVideoRecving()==true){
                stopVideoRecv();
            }
            vic.dispose();
            vic = null;
        }

        if(voc!=null){
            if(voc.isVoiceRecving()==true){

            }
            voc.dispose();
            voc = null;
        }

        if(parentLayout!=null){
            parentLayout = null;
        }

        if(mUserInfo!=null){
            mUserInfo = null;
        }
    }

    public void startAudioRecv(){
        if(voc!=null) {
            Log.d(TAG,"startAudioRecv");
            voc.startVoiceRecv();
        }
    }

    public void stopAudioRecv(){
        if(voc!=null) {
            Log.d(TAG,"stopVoiceRecv");
            voc.stopVoiceRecv();
        }
    }

    public void deleteAudioChannel(){
        if((voc!=null)&&(voc.getChannel()>=0)) {
            Log.d(TAG,"deleteAudioChannel");
            voc.deleteChannel();
        }
    }

    public void deleteVideoChannel(){
        if((vic!=null)&&(vic.getChannel()>=0)) {
            Log.d(TAG,"deleteVideoChannel");
            vic.deleteChannel();
        }
    }

    public void setVideoParams(){
        if(vic==null){
            Log.d(TAG,"setVideoParams failed, vic is not init!!");
            return;
        }

        if(mUserInfo==null){
            Log.d(TAG,"setVideoParams failed, userInfo is not init!!");
            return;
        }

        if(mUserInfo.getVideoPort()<=0){
            Log.d(TAG,"setVideoParams failed, userInfo params invalid!!");
            return;
        }

        Log.d(TAG,"setVideoParams");
        vic.setVideoRxPort(mUserInfo.getVideoPort());
    }

    public void setVoiceParams(){
        if(voc==null){
            Log.d(TAG,"setVoiceParams failed, voc is not init!!");
            return;
        }

        if(mUserInfo==null){
            Log.d(TAG,"setVoiceParams failed, userInfo i not init!!");
            return;
        }

        if(mUserInfo.getAudioPort()<=0){
            Log.d(TAG,"setVoiceParams failed, userInfo params invalid!!");
            return;
        }

        Log.d(TAG,"setVoiceParams");
        voc.setVoiceRxPort(mUserInfo.getAudioPort());
    }

    public void setVideoRenderer(Context context){
        if(vic!=null) {
            Log.d(TAG,"setVideoRenderer");
            vic.setSurfaceView(context);
        }
    }
}
