package android.webrtc.avgroupchatproxy;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.webrtc.core.VideoEngine;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.webrtc.videoengine.ViERenderer;

/**
 * Created by kai on 2016/10/26.
 */
public class RecvVideoChannel {
    private final String TAG = "RecvVideoChannel";
    private int videoChannel;
    private boolean vieRecving;
    private int videoRxPort;
    private SurfaceView svRemote;
    private VideoEngine vie;
    private Context context;

    public RecvVideoChannel(Context context){
        this.context=context;
        videoChannel = -1;
        vieRecving = false;
    }

    public int getChannel(){
        return videoChannel;
    }

    public boolean isVideoRecving(){
        return vieRecving;
    }

    public void setVideoEngine(VideoEngine vie){
        this.vie = vie;
    }

    public void setSurfaceView(Context context){
        svRemote = ViERenderer.CreateRenderer(context, true);
        svRemote.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG,"channel:"+String.valueOf(videoChannel)+" surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG,"channel:"+String.valueOf(videoChannel)+" surfaceChanged");
                Log.d(TAG,"surface:format:"+String.valueOf(format)+" width:"+String.valueOf(width)
                        +" height:"+String.valueOf(height));
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG,"channel:"+String.valueOf(videoChannel)+" surfaceDestroyed");
            }
        });
    }

    public void addView(RelativeLayout layout){
        if((svRemote!=null)&&(svRemote.getHolder()!=null)&&(svRemote.getParent()==null)) {
            layout.addView(svRemote);
        }
    }

    public void removeView(){
        if((svRemote!=null)&&(svRemote.getParent()!=null)) {
            ((RelativeLayout) svRemote.getParent()).removeView(svRemote);
            Log.d(TAG,"RecvVideoChannel removeView context:"+context);
        }
    }

    public int createChannel(){
        if(vie==null){
            Log.d(TAG,"ViE is not init, createChannel failed!");
            return -1;
        }
        videoChannel = vie.createChannel();
        AVGC.publicCheck(videoChannel >= 0, "Failed voe CreateVideoChannel");
        return videoChannel;
    }

    public void connectVoiceChannel(int audioChannel){
        if((videoChannel>=0)&&(audioChannel>=0)){
            AVGC.publicCheck(vie.connectAudioChannel(videoChannel, audioChannel) == 0,
                    "Failed ConnectAudioChannel");
        }
    }

    public void startVideoRecv(){
        AVGC.publicCheck(!vieRecving, "ViE already recving!");

        if(vie==null){
            Log.d(TAG,"vie is not init, startVideoRecv failed!");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, startVideoRecv failed!");
            return;
        }

        if (vieRecving==false){
            AVGC.publicCheck(vie.addRenderer(videoChannel, svRemote,
                    0, 0, 0, 1, 1) == 0, "Failed AddRenderer");
            AVGC.publicCheck(vie.startRender(videoChannel) == 0, "Failed StartRender");
            AVGC.publicCheck(vie.startReceive(videoChannel) == 0, "Failed StartReceive");
            vieRecving = true;
        }
    }

    public void stopVideoRecv(){
        AVGC.publicCheck(vieRecving, "ViE already stoped");

        if(vie==null){
            Log.d(TAG,"vie is not init, stopVideoRecv failed!");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, stopVideoRecv failed!");
            return;
        }

        if(vieRecving==true) {
            AVGC.publicCheck(vie.stopReceive(videoChannel) == 0, "StopReceive");
            AVGC.publicCheck(vie.stopRender(videoChannel) == 0, "StopRender");
            AVGC.publicCheck(vie.removeRenderer(videoChannel) == 0, "RemoveRenderer");
            vieRecving = false;
        }
    }

    public void deleteChannel(){
        if(vie==null){
            Log.d(TAG,"vie is not init, deleteVideoChannel failed");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, deleteVideoChannel failed");
            return;
        }
        AVGC.publicCheck(vie.deleteChannel(videoChannel) == 0, TAG+" deleteVideoChannel failed!");

        videoChannel = -1;
    }

    public void setVideoRxPort(int videoRxPort) {
        if(vie==null){
            Log.d(TAG,"vie is not init, setVideoRxPort failed");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, setVideoRxPort failed");
            return;
        }

        this.videoRxPort = videoRxPort;
        AVGC.publicCheck(vie.setLocalReceiver(videoChannel, videoRxPort) == 0,
                "Failed setLocalReceiver");
    }

    public void dispose(){

        if(videoChannel>=0){
            deleteChannel();
        }

        if(svRemote!=null) {
            svRemote = null;
        }

        if(vie!=null){
            vie = null;
        }

        if(context!=null){
            context = null;
        }
    }
}
