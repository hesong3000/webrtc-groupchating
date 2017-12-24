package android.webrtc.proxy;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

/**
 * Created by kai on 2016/8/8.
 */
public class StateVideo_Audio implements State {
    //本地、远端视图是否切换（false）
    private boolean bSwitchView = false;
    private boolean bLoudSpeaker = false;

    @Override
    public String startVideoCall(StateContext context, Bundle params) {
        Log.d("webrtc","videoAndAudio state do not support startVideoCall");
        return stateError;
    }

    @Override
    public String stopVideoCall(final StateContext context) {
        Log.d("mysurface1","invoke StateVideo_Audio stopVideoCall begin");
        if(context.getEngine().isRunning()){
            //clearViews(context, bSwitchView);
            Log.d("mysurface1","invoke context.getEngine().stop() begin");
            context.getEngine().stop();
            Log.d("mysurface1","invoke context.getEngine().stop() end");
        }
        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        Log.d("mysurface1","trans context.STATE_IDLEl");
        return stateOK;
    }

    @Override
    public String startAudioCall(StateContext context, Bundle params) {
        Log.d("webrtc","videoAndAudio state do not support startAudioCall");
        return stateError;
    }

    @Override
    public String stopAudioCall(StateContext context) {
        Log.d("webrtc","videoAndAudio state do not support stopAudioCall");
        return stateError;
    }

    @Override
    public String startAudioRecord(StateContext context, Bundle params) {
        Log.d("webrtc","videoAndAudio state do not support startAudioRecord");
        return stateError;
    }

    @Override
    public String stopAudioRecord(StateContext context) {
        Log.d("webrtc","videoAndAudio state do not support stopAudioRecord");
        return stateError;
    }

    @Override
    public String startAudioPlayout(StateContext context, Bundle params) {
        Log.d("webrtc","videoAndAudio state do not support startAudioPlayout");
        return stateError;
    }

    @Override
    public String stopAudioPlayout(StateContext context) {
        Log.d("webrtc","videoAndAudio state do not support stopAudioPlayout");
        return stateError;
    }

    @Override
    public String setMicMute(StateContext context) {
        //暂时在视音频通话时，不能设置静音
        Log.d("webrtc","videoAndAudio state do not support setMicMute");
        return stateError;
    }

    @Override
    public String resumeMicMute(StateContext context) {
        //暂时在视音频通话时，不能设置静音
        Log.d("webrtc","videoAndAudio state do not support resumeMicMute");
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
        Log.d("webrtc","invoke StateVideo_Audio switchCameraFacing");
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
        Log.d("webrtc","invoke StateVideo_Audio switchRenderView");
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
        Log.d("webrtc","invoke StateVideo_Audio switchToAudioCall");
        if(context.getEngine().isVieRunning()){
            context.getEngine().stopVie();
            //clearViews(context,bSwitchView);
        }
        context.setCurrent(context.STATE_AUDIO);
        return stateOK;
    }

    @Override
    public String forceTransToIdleState(StateContext context) {
        Log.d("mysurface1","invoke StateVideo_Audio forceTransToIdleState begin");
        if(context.getEngine().isRunning()){
            if(context.getEngine().isVieRunning()) {
                //clearViews(context, bSwitchView);
            }
            context.getEngine().stop();
        }
        context.setCurrent(context.STATE_IDLE);
        context.getCurrent().invokeStateInit(context);
        Log.d("mysurface1","invoke StateVideo_Audio forceTransToIdleState end");
        return stateOK;
    }

    @Override
    public String invokeStateInit(StateContext context) {
        Log.d("webrtc","invoke StateVideo_Audio invokeStateInit");
        bSwitchView = false;
        bLoudSpeaker = false;
        setViews(context, bSwitchView);
        return stateOK;
    }

    @Override
    public String startLocalCapture(StateContext context) {
        Log.d("webrtc","videoAndAudio state do not support startLocalCapture");
        return stateError;
    }

    @Override
    public String stopLocalCapture(StateContext context) {
        Log.d("webrtc","videoAndAudio state do not support stopLocalCapture");
        return stateError;
    }

    @Override
    public String setLoudSpeakerOn(StateContext context) {
        Log.d("webrtc","invoke StateVideo_Audio setLoudSpeakerOn");

        if(bLoudSpeaker==false) {
            context.getEngine().loudSpeakerEnable(true);
            bLoudSpeaker = true;
        }
        return stateOK;
    }

    @Override
    public String setLoudSpeakerOff(StateContext context) {
        Log.d("webrtc","invoke StateVideo_Audio setLoudSpeakerOff");

        if(bLoudSpeaker==true) {
            context.getEngine().loudSpeakerEnable(false);
            bLoudSpeaker = false;
        }
        return stateOK;
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
