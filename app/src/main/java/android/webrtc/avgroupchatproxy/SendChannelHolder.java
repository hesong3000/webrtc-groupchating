package android.webrtc.avgroupchatproxy;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webrtc.core.AVGroupEngine;
import android.widget.RelativeLayout;

/**
 * Created by kai on 2016/10/31.
 */
public class SendChannelHolder extends ChannelHolder{
    private final String TAG = "SendChannelHolder";

    private SendVideoChannel vic;
    private SendAudioChannel voc;
    private RelativeLayout parentLayout = null;
    private UserInfo mUserInfo = null;

    public SendChannelHolder(){
        vic = null;
        voc = null;
        parentLayout = null;
        mUserInfo = new UserInfo();
    }

    public void setUserInfo(UserInfo userInfo){
        if(mUserInfo==null) {
            mUserInfo = new UserInfo(userInfo);
        }
        else{
            mUserInfo.updateUserInfo(userInfo);
        }
    }

    public UserInfo getUserInfo(){
        if(mUserInfo!=null){
            return mUserInfo;
        }
        else{
            return null;
        }
    }

    public void setParentLayout(RelativeLayout layout){
        parentLayout = layout;
    }

    public RelativeLayout getParentLayout(){
        return parentLayout;
    }

    public int createVideoChannel(AVGroupEngine mediaEngine, Context context){
        if(vic!=null) {
            return vic.getChannel();
        }
        Log.d(TAG,"createVideoChannel");
        vic = new SendVideoChannel(context);
        vic.setVideoEngine(mediaEngine.getVie());
        return vic.createChannel();
    }

    public int createVoiceChannel(AVGroupEngine mediaEngine, Context context){
        if(voc!=null){
            return voc.getChannel();
        }
        Log.d(TAG,"createVoiceChannel");
        voc = new SendAudioChannel(context);
        voc.setVoiceEngine(mediaEngine.getVoe());
        return voc.createChannel();
    }

    public void deleteVideoChannel(){
        if((vic!=null)&&(vic.getChannel()>=0)){
            Log.d(TAG,"deleteVideoChannel");
            vic.deleteChannel();
        }
    }

    public void deleteVoiceChannel(){
        if((voc!=null)&&(voc.getChannel()>=0)){
            Log.d(TAG,"deleteoiceChannel");
            voc.deleteChannel();
        }
    }

    public void setVideoParams(int resolution, int videoCodec){
        Log.d("SendVideoChannel","setVideoParams in");

        if(vic==null){
            Log.d(TAG,"setVideoParams failed, vic is not init!!");
            return;
        }

        if(mUserInfo==null){
            Log.d(TAG,"setVideoParams failed, userInfo is not init!!");
            return;
        }

        if((mUserInfo.getMediaIP().length()==0)||(mUserInfo.getVideoPort()<=0)){
            Log.d(TAG,"setVideoParams failed, userInfo params invalid!!");
            return;
        }

        vic.setDestionaton(mUserInfo.getMediaIP(),mUserInfo.getVideoPort());
        vic.setSSRC(mUserInfo.getUserSSRC_V());
        vic.setResolutionIndex(resolution);
        vic.setVideoCodec(videoCodec);
    }

    public void setVoiceParams(int audioCodec){
        if(voc==null){
            Log.d(TAG,"setVoiceParams failed, voc is not init!!");
            return;
        }

        if(mUserInfo==null){
            Log.d(TAG,"setVoiceParams failed, userInfo i not init!!");
            return;
        }

        if((mUserInfo.getMediaIP().length()==0)||(mUserInfo.getAudioPort()<=0)){
            Log.d(TAG,"setVoiceParams failed, userInfo params invalid!!");
            return;
        }

        Log.d(TAG,"setVoiceParams");
        voc.setDestionaton(mUserInfo.getMediaIP(),mUserInfo.getAudioPort());
        voc.setSSRC(mUserInfo.getUserSSRC_A());
        voc.setAudioCodec(audioCodec);
    }

    public void startVideoSend(){
        if((vic!=null)&&(parentLayout!=null)&&(vic.isVideoSending()==false)){
            parentLayout.removeAllViews();
            Log.d(TAG,"startVideoSend parentLayout:"+parentLayout.toString());
            vic.startVideoSend();
            vic.addView(parentLayout);
        }
    }

    public void startVoiceSend(){
        if(voc!=null){
            Log.d(TAG,"startVoiceSend");
            voc.startVoiceSend();
        }
    }

    public void stopVideoSend(){
        if((vic!=null)&&(vic.isVideoSending()==true)){
            Log.d(TAG,"stopVideoSend");
            vic.stopVideoSend();
        }
    }

    public void stopVoiceSend(){
        if(voc!=null){
            Log.d(TAG,"stopVoiceSend");
            voc.stopVoiceSend();
        }
    }

    public void setVideoRenderer(Context context){
        if(vic!=null){
            vic.setSurfaceView(context);
        }
    }

    public void dispose(){
        if(vic!=null){
            if(vic.isVideoSending()==true){
                stopVideoSend();
            }

            vic.dispose();
            vic = null;
        }

        if(voc!=null){
            if(voc.isVoiceSending()==true){
                stopVoiceSend();
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

    public void turnIntoInvitingState(Context context, Bitmap bitmap){
        if(context==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoInvitingState failed, context is not init!!");
            return;
        }

        if(bitmap==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoInvitingState failed, bitmap is not init!!");
            return;
        }

        if(mUserInfo==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoInvitingState failed, could not find userInfo!!");
            return;
        }

        if(parentLayout==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoInvitingState failed, parentLayout is not init!!");
            return;
        }

        if(!mUserInfo.getUserState().equals(UserState.invitingStat)){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoInvitingState failed, user is not on invitingstate");
            return;
        }

        Log.d(TAG,"turnIntoInvitingState Begin!!");
        turnIntoInvitingState(context,parentLayout,bitmap);
    }

    public void turnIntoAudioChatState(Context context, Bitmap bitmap){
        if(context==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoAudioChatState failed, context is not init!!");
            return;
        }

        if(bitmap==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoAudioChatState failed, bitmap is not init!!");
            return;
        }

        if(mUserInfo==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoAudioChatState failed, could not find userInfo!!");
            return;
        }

        if(parentLayout==null){
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoAudioChatState failed, parentLayout is not init!!");
            return;
        }

        //只有用户处在chatting和with-audio状态下正确
        if((!mUserInfo.getUserState().contains(UserState.invitingStat))&&(mUserInfo.getUserState().contains(UserState.chatingStat))
                &&(mUserInfo.getUserState().contains(UserState.withAudioStat))&&(!mUserInfo.getUserState().contains(UserState.withVideoStat))){
            Log.d(TAG,"turnIntoAudioChatState Begin!!");
            turnIntoAudioChatState(context, parentLayout, bitmap);
        }
        else{
            Log.d(TAG,"user:"+mUserInfo.getUserID()+" turnIntoAudioChatState failed, user is not on chatting和with-audio state");
            return;
        }
    }
}
