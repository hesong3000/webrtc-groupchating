package android.webrtc.proxy;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.webrtc.activity.R;

import java.io.File;
import java.util.List;

/**
 * Created by kai on 2016/8/1.
 */
public class StatePlayout implements State {

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
        Log.d("webrtc","StatePlayout invoke startVideoCall");

        context.getEngine().stopAudioPlayout();

        //参数完整性校验b--------
        if ((!params.containsKey("remoteIP")) || (!params.containsKey("localRecvPort_V")) ||
                (!params.containsKey("remoteSendPort_V")) || (!params.containsKey("ssrc_V"))) {
            Log.d("webrtc", "startVideoCall lack of params");
            return stateError;
        }
        //参数完整性校验e--------

        String remoteIP = "";
        remoteIP = params.getString("remoteIP");
        if (ipValid(remoteIP) == false) {
            Log.d("webrtc", "startVideoCall remoteIP:" + remoteIP + " is invalid");
            return stateError;
        }

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
        context.getEngine().setTrace(context.getActivityContext().getResources().getBoolean(
                R.bool.trace_enabled_default));

        //设置远端地址
        context.getEngine().setRemoteIp(remoteIP);

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
        //设置分辨率(3)
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
        context.setCurrent(context.STATE_VIDEO);
        context.getCurrent().invokeStateInit(context);

        return stateOK;
    }

    @Override
    public String stopVideoCall(StateContext context) {
        Log.d("webrtc","playout state do not support stopVideoCall");
        return stateError;
    }

    @Override
    public String startAudioCall(final StateContext context, Bundle params) {
        Log.d("webrtc","StatePlayout invoke startAudioCall");

        context.getEngine().stopAudioPlayout();

        //参数完整性校验b--------
        if ((!params.containsKey("remoteIP")) || (!params.containsKey("localRecvPort_A")) ||
                (!params.containsKey("remoteSendPort_A")) || (!params.containsKey("ssrc_A"))) {
            Log.d("webrtc", "startAudioCall lack of params");
            return stateError;
        }
        //参数完整性校验e--------

        String remoteIP = "";
        remoteIP = params.getString("remoteIP");
        if (ipValid(remoteIP) == false) {
            Log.d("webrtc", "startAudioCall remoteIP:" + remoteIP + " is invalid");
            return stateError;
        }

        //音频参数合理性校验b---------
        int localRecvPort_A = 0;
        localRecvPort_A = params.getInt("localRecvPort_A");
        if (localRecvPort_A <= 0) {
            Log.d("webrtc", "startAudioCall localRecvPort_A:" +
                    String.valueOf(localRecvPort_A) + " is invalid");
            return stateError;
        }

        int remoteSendPort_A = 0;
        remoteSendPort_A = params.getInt("remoteSendPort_A");
        if (remoteSendPort_A <= 0) {
            Log.d("webrtc", "startAudioCall remoteSendPort_A:" +
                    String.valueOf(remoteSendPort_A) + " is invalid");
            return stateError;
        }

        int ssrc_A = 0;
        ssrc_A = params.getInt("ssrc_A");
        //音频参数合理性校验e---------

        if(context.getEngine().isVoeRunning()==true){
            Log.d("webrtc", "webrtc voice already running!!!");
            return stateError;
        }

        //设置TRACE
        context.getEngine().setTrace(context.getActivityContext().getResources().getBoolean(
                R.bool.trace_enabled_default));

        //扩音可用(true)
        context.getEngine().setSpeaker(context.getActivityContext().getResources().getBoolean(
                R.bool.speaker_enabled_default));

        //设置远端地址
        context.getEngine().setRemoteIp(remoteIP);

        //音频设置b----------
        //设置音频可用(true)
        context.getEngine().setAudio(context.getActivityContext().getResources().getBoolean(
                R.bool.audio_enabled_default));
        //设置本地音频接收端口
        context.getEngine().setAudioRxPort(localRecvPort_A);
        //设置远端音频发送端口
        context.getEngine().setAudioTxPort(remoteSendPort_A);
        //设置音频SSRC
        context.getEngine().setAudioSSRC(ssrc_A);
        //设置ISAC音频编解码器,暂时不添加此接口
        context.getEngine().setAudioCodec(context.getEngine().getIsacIndex());
        //设置噪声抑制(true)
        context.getEngine().setNs(context.getActivityContext().getResources().getBoolean(
                R.bool.audio_ns_enable_default));
        //设置回声消除(false)
        context.getEngine().setEc(context.getActivityContext().getResources().getBoolean(
                R.bool.audio_aec_enable_default));
        //设置自动增益控制(true)
        context.getEngine().setAgc(context.getActivityContext().getResources().getBoolean(
                R.bool.audio_agc_enable_default));
        //目前暂时关闭RTCP功能
        context.getEngine().SetVoiceRTCPEnable(context.getActivityContext().getResources().getBoolean(
                R.bool.audio_rtcp_enable_default));
        //音频设置e----------
        context.getEngine().startVoE();

        context.setCurrent(context.STATE_AUDIO);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String stopAudioCall(StateContext context) {
        Log.d("webrtc","playout state do not support stopAudioCall");
        return stateError;
    }

    @Override
    public String startAudioRecord(StateContext context, Bundle params) {
        Log.d("webrtc","playout state do not support startAudioRecord");
        return stateError;
    }

    @Override
    public String stopAudioRecord(StateContext context) {
        Log.d("webrtc","playout state do not support stopAudioRecord");
        return stateError;
    }

    @Override
    public String startAudioPlayout(StateContext context, Bundle params) {
        Log.d("webrtc","StatePlayout invoke startAudioPlayout");
        context.getEngine().stopAudioPlayout();

        //参数完整性校验b--------
        //palyFile为文件全路径
        if ((!params.containsKey("playFile"))) {
            Log.d("webrtc", "startAudioPlayout lack of params");
            return stateError;
        }
        //参数完整性校验e--------

        //参数合理性校验b--------
        String playFile = "";
        playFile = params.getString("playFile");

        File file = new File(playFile);
        if(file.isFile()==false){
            Log.d("webrtc", "startAudioPlayout playFile:" + playFile + " is invalid");
            return stateError;
        }

        if(file.exists()==false){
            Log.d("webrtc", "startAudioPlayout playFile:" + playFile + " is not exists");
            return stateError;
        }

        if(file.canRead()==false){
            Log.d("webrtc", "startAudioPlayout playFile:" + playFile + " can not read");
            return stateError;
        }

        if(playFile.length()<=4){
            Log.d("webrtc", "startAudioPlayout playFile:" + playFile + " is invalid");
            return stateError;
        }

        if(playFile.substring(playFile.length()-3).toLowerCase().equals(
                R.string.audio_recordFile_format)==false){
            Log.d("webrtc", "startAudioPlayout playFile:" + playFile + " not support format");
            return stateError;
        }
        //参数合理性校验e--------

        context.getEngine().startAudioPlayout(playFile);

        return stateOK;
    }

    @Override
    public String stopAudioPlayout(StateContext context) {
        Log.d("webrtc","StatePlayout invoke stopAudioPlayout");
        context.getEngine().stopAudioPlayout();
        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String setMicMute(StateContext context) {
        Log.d("webrtc","playout state do not support setMicMute");
        return stateError;
    }

    @Override
    public String resumeMicMute(StateContext context) {
        Log.d("webrtc","playout state do not support resumeMicMute");
        return stateError;
    }

    @Override
    public String setRenderMute(StateContext context) {
        Log.d("webrtc","playout state do not support setRenderMute");
        return stateError;
    }

    @Override
    public String resumeRenderMute(StateContext context) {
        Log.d("webrtc","playout state do not support resumeRenderMute");
        return stateError;
    }

    @Override
    public String switchCameraFacing(StateContext context) {
        Log.d("webrtc","playout state do not support switchCameraFacing");
        return stateError;
    }

    @Override
    public String switchRenderView(StateContext context) {
        Log.d("webrtc","playout state do not support switchRenderView");
        return stateError;
    }

    @Override
    public String switchToAudioCall(StateContext context) {
        Log.d("webrtc","playout state do not support switchToAudioCall");
        return stateError;
    }

    @Override
    public String forceTransToIdleState(StateContext context) {
        Log.d("webrtc","StatePlayout invoke forceTransToIdleState");
        context.getEngine().stopAudioPlayout();
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
        Log.d("webrtc","playout state do not support startLocalCapture");
        return stateError;
    }

    @Override
    public String stopLocalCapture(StateContext context) {
        Log.d("webrtc","playout state do not support stopLocalCapture");
        return stateError;
    }

    @Override
    public String setLoudSpeakerOn(StateContext context) {
        Log.d("webrtc","playout state do not support setLoudSpeakerOn");
        return stateError;
    }

    @Override
    public String setLoudSpeakerOff(StateContext context) {
        Log.d("webrtc","playout state do not support setLoudSpeakerOff");
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
