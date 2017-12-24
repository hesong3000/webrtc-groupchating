package android.webrtc.proxy;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.webrtc.activity.R;

/**
 * Created by kai on 2016/8/1.
 */
public class StateVideo implements State {

    //本地、远端视图是否切换（false）
    private boolean bSwitchView = false;

    @Override
    public String startVideoCall(StateContext context, Bundle params) {
        Log.d("webrtc","video state do not support startVideoCall");
        return stateError;
    }

    @Override
    public String stopVideoCall(final StateContext context) {
        Log.d("webrtc","StateVideo invoke stopVideoCall");
        if(context.getEngine().isVieRunning()){
            context.getEngine().stopVie();
            //clearViews(context, bSwitchView);
        }
        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String startAudioCall(final StateContext context, Bundle params) {
        Log.d("webrtc","StateVideo invoke startAudioCall");

        //参数完整性校验b--------
        if ((!params.containsKey("localRecvPort_A")) ||(!params.containsKey("remoteSendPort_A")) ||
                (!params.containsKey("ssrc_A"))) {

            Log.d("webrtc", "startAudioCall lack of params");
            return stateError;
        }
        //参数完整性校验e--------

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
            //Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.BeRefused),Toast.LENGTH_SHORT).show();

            return stateError;
        }

        //设置TRACE
        //context.getEngine().setTrace(context.getActivityContext().getResources().getBoolean(
        //        R.bool.trace_enabled_default));

        //音频设置b----------
        //设置音频可用(true)
        context.getEngine().setAudio(context.getActivityContext().getResources().getBoolean(
                R.bool.audio_enabled_default));
        //扩音可用(true)
        context.getEngine().setSpeaker(context.getActivityContext().getResources().getBoolean(
                R.bool.speaker_enabled_default));
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
        context.setCurrent(context.STATE_VIDEO_AUDIO);
        return stateOK;
    }

    @Override
    public String stopAudioCall(StateContext context) {
        Log.d("webrtc","video state do not support stopAudioCall");
        return stateError;
    }

    @Override
    public String startAudioRecord(StateContext context, Bundle params) {
        Log.d("webrtc","video state do not support startAudioRecord");
        return stateError;
    }

    @Override
    public String stopAudioRecord(StateContext context) {
        Log.d("webrtc","video state do not support stopAudioRecord");
        return stateError;
    }

    @Override
    public String startAudioPlayout(StateContext context, Bundle params) {
        Log.d("webrtc","video state do not support startAudioPlayout");
        return stateError;
    }

    @Override
    public String stopAudioPlayout(StateContext context) {
        Log.d("webrtc","video state do not support stopAudioPlayout");
        return stateError;
    }

    @Override
    public String setMicMute(StateContext context) {
        Log.d("webrtc","video state do not support setMicMute");
        return stateError;
    }

    @Override
    public String resumeMicMute(StateContext context) {
        Log.d("webrtc","video state do not support resumeMicMute");
        return stateError;
    }

    @Override
    public String setRenderMute(StateContext context) {
        //增加实现(暂时不提供此功能)
        return stateOK;
    }

    @Override
    public String resumeRenderMute(StateContext context) {
        //增加实现（暂时不提供此功能）
        return stateOK;
    }

    @Override
    public String switchCameraFacing(final StateContext context) {
        Log.d("webrtc","invoke StateVideo switchCameraFacing");

        if(context.getEngine().hasMultipleCameras()==false)
            return stateError;

        //切换摄像头，先关闭，设置启用方向，后打开
        new Thread(new Runnable() {
            @Override
            public void run() {
                context.getEngine().toggleCamera();
            }
        }).start();
        return stateOK;
    }

    @Override
    public String switchRenderView(StateContext context) {
        Log.d("webrtc","invoke StateVideo switchRenderView");
        //移除当前视图
        context.getEngine().togglePauseView();
        //clearViews(context, bSwitchView);

        bSwitchView = !bSwitchView;

        context.getEngine().toggleResumeView();
        //加载当前视图
        setViews(context, bSwitchView);
        return stateOK;
    }

    @Override
    public String switchToAudioCall(StateContext context) {
        Log.d("webrtc","video state do not support switchToAudioCall");
        return stateError;
    }

    @Override
    public String forceTransToIdleState(StateContext context) {
        Log.d("webrtc","invoke StateVideo forceTransToIdleState");
        if(context.getEngine().isRunning()){
            if(context.getEngine().isVieRunning()) {
                clearViews(context, bSwitchView);
            }
            context.getEngine().stop();
        }
        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        return stateOK;
    }

    @Override
    public String invokeStateInit(StateContext context) {
        Log.d("webrtc","StateVideo invokeStateInit");
        bSwitchView = false;
        setViews(context, bSwitchView);
        return stateOK;
    }

    @Override
    public String startLocalCapture(StateContext context) {
        Log.d("webrtc","video state do not support startLocalCapture");
        return stateError;
    }

    @Override
    public String stopLocalCapture(StateContext context) {
        Log.d("webrtc","video state do not support stopLocalCapture");
        return stateError;
    }

    @Override
    public String setLoudSpeakerOn(StateContext context) {
        Log.d("webrtc","video state do not support setLoudSpeakerOn");
        return stateError;
    }

    @Override
    public String setLoudSpeakerOff(StateContext context) {
        Log.d("webrtc","video state do not support setLoudSpeakerOff");
        return stateError;
    }

    private void setViews(StateContext context, boolean isViewSwitch) {

        //必须先primarySurace，后secondarySurface
        SurfaceView localSurfaceView = context.getEngine().getLocalSurfaceView();
        SurfaceView remoteSurfaceView = context.getEngine().getRemoteSurfaceView();
        if (isViewSwitch == false) {
            if (localSurfaceView != null) {
                if(context.getSecondarySurface().getChildCount()==0) {
                    context.getSecondarySurface().addView(localSurfaceView);
                }
            }
            if (remoteSurfaceView != null) {
                if(context.getPrimarySurace().getChildCount()==0) {
                    context.getPrimarySurace().addView(remoteSurfaceView);
                }
            }
        } else {
            if (remoteSurfaceView != null) {
                if(context.getSecondarySurface().getChildCount()==0) {
                    context.getSecondarySurface().addView(remoteSurfaceView);
                }
            }
            if (localSurfaceView != null) {
                if(context.getPrimarySurace().getChildCount()==0) {
                    context.getPrimarySurace().addView(localSurfaceView);
                }
            }
        }
    }

    private void clearViews(StateContext context, boolean isViewSwitch) {

        //必须先secondarySurface，后primarySurace
        SurfaceView localSurfaceView = context.getEngine().getLocalSurfaceView();
        SurfaceView remoteSurfaceView = context.getEngine().getRemoteSurfaceView();

        if (isViewSwitch == false) {
            if (localSurfaceView != null) {
                if(context.getSecondarySurface().getChildCount()==1) {
                    context.getSecondarySurface().removeView(localSurfaceView);
                }
            }
            if (remoteSurfaceView != null) {
                if(context.getPrimarySurace().getChildCount()==1) {
                    context.getPrimarySurace().removeView(remoteSurfaceView);
                }
            }
        } else {
            if (remoteSurfaceView != null) {
                if(context.getSecondarySurface().getChildCount()==1) {
                    context.getSecondarySurface().removeView(remoteSurfaceView);
                }
            }
            if (localSurfaceView != null) {
                if(context.getPrimarySurace().getChildCount()==1) {
                    context.getPrimarySurace().removeView(localSurfaceView);
                }
            }
        }
    }
}
