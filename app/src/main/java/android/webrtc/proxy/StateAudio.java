package android.webrtc.proxy;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.webrtc.activity.R;

import java.util.List;

/**
 * Created by kai on 2016/8/1.
 */
public class StateAudio implements State {

    private boolean bLoudSpeaker = false;
    private boolean bMicMute = false;

    //摄像头操作b----------
    private Camera.CameraInfo cameras[];
    private Camera camera;
    private Camera.Parameters parameters;
    //摄像头操作e----------

    //RTCP模式b-----------
    private enum RTCP_MODE{
        kRtcpNone,
        kRtcpCompound_RFC4585,
        kRtcpCompound_RFC5506
    }
    //RTCP模式e-----------

    @Override
    public String startVideoCall(final StateContext context, Bundle params) {
        Log.d("webrtc","StateAudio invoke startVideoCall");

        //参数完整性校验b--------
        if ((!params.containsKey("localRecvPort_V")) ||(!params.containsKey("remoteSendPort_V")) ||
                (!params.containsKey("ssrc_V"))) {
            Log.d("webrtc", "startVideoCall lack of params");
            return stateError;
        }
        //参数完整性校验e--------

        //视频参数合理性校验b---------
        int localRecvPort_V = 0;
        localRecvPort_V = params.getInt("localRecvPort_V");
        if (localRecvPort_V <= 0) {
            Log.d("webrtc", "startVideoCall localRecvPort_V:" +
                    String.valueOf(localRecvPort_V) + " is invalid");
            return stateError;
        }

        int remoteSendPort_V = 0;
        remoteSendPort_V = params.getInt("remoteSendPort_V");
        if (remoteSendPort_V <= 0) {
            Log.d("webrtc", "startVideoCall remoteSendPort_V:"
                    + String.valueOf(remoteSendPort_V) + " is invalid");
            return stateError;
        }

        int ssrc_V = 0;
        ssrc_V = params.getInt("ssrc_V");
        //视频参数合理性校验e---------

        if(context.getEngine().isVieRunning()==true){
            Log.d("webrtc", "webrtc video already running!!!");
            return stateError;
        }

        //设置应用性能记录(false)
        context.getEngine().setDebuging(context.getActivityContext().getResources().getBoolean(
                R.bool.apm_debug_enabled_default));

        //使用openGl渲染
        /*context.getEngine().setViewSelection(context.getActivityContext().getResources().getInteger(
                R.integer.defaultView));*/

        //设置TRACE
        //context.getEngine().setTrace(context.getActivityContext().getResources().getBoolean(
         //       R.bool.trace_enabled_default));

        //视频设置b----------
        //设置视频接收可用(true)
        context.getEngine().setReceiveVideo(context.getActivityContext().getResources().getBoolean(
                R.bool.video_receive_enabled_default));
        //设置视频发送可用(true)
        context.getEngine().setSendVideo(context.getActivityContext().getResources().getBoolean(
                R.bool.video_send_enabled_default));
        //设置本地视频接收端口
        context.getEngine().setVideoRxPort(localRecvPort_V);
        //设置远端视频发送端口
        context.getEngine().setVideoTxPort(remoteSendPort_V);
        //设置视频SSRC
        context.getEngine().setVideoSSRC(ssrc_V);
        //设置分辨率(3) 640x480
        /*
        if(resolutionValid(context)<0) {
            Log.d("webrtc","resolutionIndex:"+R.integer.video_resolution_default+" is invalid");
            return stateError;
        }
        */
        context.getEngine().setResolutionIndex(context.getActivityContext().getResources().
                getInteger(R.integer.video_resolution_default));
        //设置视频编解码器，默认为vp8
        context.getEngine().setVideoCodec(context.getActivityContext().getResources().getInteger(
                R.integer.video_codec_default));
        //设置丢包重传，可能会出现延迟(false),由于此项依赖rtcp，暂时关闭
        context.getEngine().setNack(context.getActivityContext().getResources().getBoolean(
                R.bool.nack_enabled_default));
        //暂时关闭rtcp功能，设置为kRtcpNone
        context.getEngine().SetVideoRTCPMode(RTCP_MODE.kRtcpNone.ordinal());
        //视频设置e----------
        context.getEngine().startViE();
        context.setCurrent(context.STATE_VIDEO_AUDIO);
        context.getCurrent().invokeStateInit(context);

        return stateOK;
    }

    @Override
    public String stopVideoCall(StateContext context) {
        Log.d("webrtc","audio state do not support stopVideoCall");
        return stateError;
    }

    @Override
    public String startAudioCall(StateContext context, Bundle params) {
        Log.d("webrtc","audio state do not support startAudioCall");
        return stateError;
    }

    @Override
    public String stopAudioCall(StateContext context) {
        Log.d("webrtc","invoke StateAudio stopAudioCall");

        if(context.getEngine().isVoeRunning()==true){
            context.getEngine().stopVoe();
        }

        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String startAudioRecord(StateContext context, Bundle params) {
        Log.d("webrtc","audio state do not support startAudioRecord");
        return stateError;
    }

    @Override
    public String stopAudioRecord(StateContext context) {
        Log.d("webrtc","audio state do not support stopAudioRecord");
        return stateError;
    }

    @Override
    public String startAudioPlayout(StateContext context, Bundle params) {
        Log.d("webrtc","audio state do not support startAudioPlayout");
        return stateError;
    }

    @Override
    public String stopAudioPlayout(StateContext context) {
        Log.d("webrtc","audio state do not support stopAudioPlayout");
        return stateError;
    }

    @Override
    public String setMicMute(StateContext context) {
        Log.d("webrtc","StateAudio invoke setMicMute");

        if(bMicMute==false){
            context.getEngine().setMicMute(true);
            bMicMute = true;
        }
        return stateOK;
    }

    @Override
    public String resumeMicMute(StateContext context) {
        Log.d("webrtc","invoke StateAudio resumeMicMute");

        if(bMicMute==true){
            context.getEngine().setMicMute(false);
            bMicMute = false;
        }
        return stateOK;
    }

    @Override
    public String setRenderMute(StateContext context) {
        Log.d("webrtc","audio state do not support setRenderMute");
        return stateError;
    }

    @Override
    public String resumeRenderMute(StateContext context) {
        Log.d("webrtc","audio state do not support resumeRenderMute");
        return stateError;
    }

    @Override
    public String switchCameraFacing(StateContext context) {
        Log.d("webrtc","audio state do not support switchCameraFacing");
        return stateError;
    }

    @Override
    public String switchRenderView(StateContext context) {
        Log.d("webrtc","audio state do not support switchRenderView");
        return stateError;
    }

    @Override
    public String switchToAudioCall(StateContext context) {
        Log.d("webrtc","audio state do not support switchToAudioCall");
        return stateError;
    }

    @Override
    public String forceTransToIdleState(StateContext context) {
        Log.d("webrtc","invoke StateAudio forceTransToIdleState");

        if(context.getEngine().isVoeRunning()==true){
            context.getEngine().stopVoe();
        }

        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String setLoudSpeakerOn(StateContext context) {
        Log.d("webrtc","invoke StateAudio setLoudSpeakerOn");
        if(bLoudSpeaker==false){
            context.getEngine().loudSpeakerEnable(true);
            bLoudSpeaker = true;
        }
        return stateOK;
    }

    @Override
    public String setLoudSpeakerOff(StateContext context) {
        Log.d("webrtc","invoke StateAudio setLoudSpeakerOff");
        if(bLoudSpeaker==true){
            context.getEngine().loudSpeakerEnable(false);
            bLoudSpeaker = false;
        }
        return stateOK;
    }

    @Override
    public String invokeStateInit(StateContext context) {
        bLoudSpeaker = false;
        bMicMute = false;
        return stateOK;
    }

    @Override
    public String startLocalCapture(StateContext context) {
        Log.d("webrtc","audio state do not support startLocalCapture");
        return stateError;
    }

    @Override
    public String stopLocalCapture(StateContext context) {
        Log.d("webrtc","audio state do not support stopLocalCapture");
        return stateError;
    }

    //私有方法b---------
    //ip地址校验
    private boolean ipValid(String ipAddress) {
        //增加ip地址校验
        return true;
    }

    //分辨率校验
    private int resolutionValid(StateContext context) {

        int resolutionIndex = context.getActivityContext().getResources().
                getInteger(R.integer.video_resolution_default);

        if (resolutionIndex >= context.getEngine().numberOfResolutions()) {
            Log.d("webrtc", "resolutionIndex:" + String.valueOf(resolutionIndex) +
                    " out of RESOLUTIONS");
            return -1;
        }

        int resolution_W = context.getEngine().getResolutionWidthByIndex(resolutionIndex);
        int resolution_H = context.getEngine().getResolutionHeightByIndex(resolutionIndex);
        boolean isPictureSizeValid = false;
        boolean isPreviewSizeValid = false;

        //摄像头操作b-------------
        cameras = new Camera.CameraInfo[2];
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d("webrtc", "has facing front camera");
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d("webrtc", "has facing back camera");
            }
            cameras[info.facing] = info;
        }

        if (cameras[Camera.CameraInfo.CAMERA_FACING_FRONT] != null) {

            isPictureSizeValid = false;
            isPreviewSizeValid = false;

            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            parameters = camera.getParameters();
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

            for (Camera.Size size : pictureSizes) {
                if ((resolution_W == size.width) && (resolution_H == size.height)) {
                    isPictureSizeValid = true;
                    break;
                }
            }

            for (Camera.Size size : previewSizes) {
                if ((resolution_W == size.width) && (resolution_H == size.height)) {
                    isPreviewSizeValid = true;
                    break;
                }
            }

            if (camera != null) {
                camera.release();
                camera = null;
            }

            if ((isPictureSizeValid == false) || (isPreviewSizeValid == false)) {
                Log.d("webrtc", "front face camera do not support resolution_W:" +
                        resolution_W + " resolution_H:" + resolution_H);
                return -1;
            }
        }

        if (cameras[Camera.CameraInfo.CAMERA_FACING_BACK] != null) {

            isPictureSizeValid = false;
            isPreviewSizeValid = false;

            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            parameters = camera.getParameters();
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

            for (Camera.Size size : pictureSizes) {
                if ((resolution_W == size.width) && (resolution_H == size.height)) {
                    isPictureSizeValid = true;
                    break;
                }
            }

            for (Camera.Size size : previewSizes) {
                if ((resolution_W == size.width) && (resolution_H == size.height)) {
                    isPreviewSizeValid = true;
                    break;
                }
            }

            if (camera != null) {
                camera.release();
                camera = null;
            }

            if ((isPictureSizeValid == false) || (isPreviewSizeValid == false)) {
                Log.d("exception", "back face camera do not support resolution_W:" +
                        resolution_W + " resolution_H:" + resolution_H);
                return -1;
            }
        }
        //摄像头操作e-------------
        return 0;
    }
    //私有方法e---------
}
