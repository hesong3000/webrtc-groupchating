package android.webrtc.avgroupchatproxy;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webrtc.core.CameraDesc;
import android.webrtc.core.VideoCodecInst;
import android.webrtc.core.VideoEngine;
import android.widget.RelativeLayout;

import org.webrtc.videoengine.VideoCaptureAndroid;

/**
 * Created by kai on 2016/10/31.
 */
public class SendVideoChannel {

    private final String TAG = "SendVideoChannel";
    private static final int VCM_VP8_PAYLOAD_TYPE = 100;
    private static final int SEND_CODEC_FPS = 20;
    private static final int INIT_BITRATE_KBPS = 384;
    private static final int MAX_BITRATE_KBPS = 1024;
    private static final int WIDTH_IDX = 0;
    private static final int HEIGHT_IDX = 1;
    private static final int[][] RESOLUTIONS = {
            {176,144}, {320,240}, {352,288}, {640,480}, {1280,720}
    };

    private int videoChannel;
    private boolean vieSending;
    private SurfaceView svLocal;
    private VideoEngine vie;
    private Context context;
    private int videoCodecIndex;
    private int resolutionIndex;
    private Camera.CameraInfo cameras[];
    private boolean useFrontCamera;
    private int currentCameraHandle = 0;
    private OrientationEventListener orientationListener;
    private String remoteIp;
    private int remotePort;
    private int SSRC;

    private int deviceOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;

    public SendVideoChannel(Context context){
        this.context=context;
        videoChannel = -1;
        vieSending = false;
        cameras = new Camera.CameraInfo[2];
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            cameras[info.facing] = info;
        }

        setDefaultCamera();

        orientationListener =
                new OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
                    public void onOrientationChanged (int orientation) {
                        deviceOrientation = orientation;
                        compensateRotation();
                    }
                };

        orientationListener.enable();
    }

    public boolean isVideoSending(){
        return vieSending;
    }

    public int getChannel(){
        return videoChannel;
    }

    public int createChannel(){
        if(vie==null){
            Log.d(TAG,"VideoEngine is not init, createChannel failed");
        }

        videoChannel = vie.createChannel();
        AVGC.publicCheck(videoChannel >= 0, "Failed voe CreateVoiceChannel");
        return videoChannel;
    }

    private int getCameraIndex() {
        return useFrontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT :
                Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    private void compensateRotation() {
        if (svLocal == null) {
            // Not rendering (or sending).
            return;
        }
        if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }

        int cameraRotation = 0;
        if (cameras[getCameraIndex()].facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraRotation = 270;
        }
        else{
            cameraRotation = 90;
        }

        // Egress streams should have real world up as up.
        if(currentCameraHandle!=0) {
            AVGC.publicCheck(vie.setRotateCapturedFrames(currentCameraHandle, cameraRotation) == 0,
                    "Failed setRotateCapturedFrames: camera " + currentCameraHandle +
                            "rotation " + cameraRotation);
        }
    }

    public void setVideoEngine(VideoEngine vie){
        this.vie = vie;
    }

    public void setSurfaceView(Context context){
        Log.d(TAG,"setSurfaceView");
        svLocal = new SurfaceView(context);
        Log.d(TAG,"setSurfaceView svLocal:"+svLocal.toString());
        svLocal.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG,"channel:"+String.valueOf(videoChannel)+" surfaceCreated");
                if((vie!=null)&&(svLocal!=null)){
                    Log.d(TAG,"svLocal parent:"+svLocal.getParent().toString());
                    if(currentCameraHandle>0){
                        VideoCaptureAndroid.setLocalPreview(svLocal.getHolder());
                        AVGC.publicCheck(vie.startCapture(currentCameraHandle) == 0, "Failed StartCapture");
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG,"channel:"+String.valueOf(videoChannel)+" surfaceDestroyed");
                if(vie!=null) {
                    Log.d(TAG,"svLocal parent:"+svLocal.getParent().toString());
                    if(currentCameraHandle>0){
                        VideoCaptureAndroid.setLocalPreview(null);
                        AVGC.publicCheck(vie.stopCapture(currentCameraHandle) == 0, "Failed StartCapture");
                    }
                }
            }
        });
    }

    public void addView(RelativeLayout layout){
        if((svLocal!=null)&&(svLocal.getParent()==null)) {
            layout.addView(svLocal);
            Log.d(TAG,"addView layout:"+layout.toString());
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleCamera();
                }
            });
        }
    }

    public void removeView(){
        if((svLocal!=null)&&(svLocal.getParent()!=null)) {
            ((RelativeLayout) svLocal.getParent()).removeView(svLocal);
            Log.d(TAG,"removeView");
        }
    }

    private void setDefaultCamera() {
        useFrontCamera = hasFrontCamera();
    }

    private boolean hasFrontCamera() {
        return cameras[Camera.CameraInfo.CAMERA_FACING_FRONT] != null;
    }

    public boolean hasBackCamera() {
        return cameras[Camera.CameraInfo.CAMERA_FACING_BACK] != null;
    }

    public void startVideoSend(){
        if(vieSending==true){
            Log.d(TAG,"ViE is already sending!");
            return;
        }

        if(vie==null){
            Log.d(TAG,"vie is not init, startVideoSend failed");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, startVideoSend failed");
            return;
        }

        startCamera();
        AVGC.publicCheck(vie.startSend(videoChannel) == 0, "Failed StartSend");

        vieSending = true;
    }

    public void stopVideoSend(){
        if(vieSending==false){
            Log.d(TAG,"ViE is not in sending!");
            return;
        }

        if(vie==null){
            Log.d(TAG,"vie is not init, stopVideoSend failed");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, stopVideoSend failed");
            return;
        }

        AVGC.publicCheck(vie.stopSend(videoChannel) == 0, "StopSend");
        stopCamera();
        removeView();
        vieSending = false;
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

    public void setDestionaton(String remoteIP, int remotePort){
        this.remoteIp = remoteIP;
        this.remotePort = remotePort;

        if(vie==null){
            Log.d(TAG,"vie is not init, setDestionaton failed");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, setDestionaton failed");
            return;
        }

        AVGC.publicCheck(vie.setSendDestination(videoChannel, remotePort, remoteIp) == 0,
                "Failed setSendDestination");
    }

    public void setSSRC(int SSRC) {
        this.SSRC = SSRC;

        if(vie==null){
            Log.d(TAG,"vie is not init, setSSRC failed");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, setSSRC failed");
            return;
        }

        Log.d("SendVideoChannel","setLocalSSRC(videoChannel,SSRC):"+SSRC);
        AVGC.publicCheck(vie.setLocalSSRC(videoChannel,SSRC) == 0,"Failed setVideoSSRC");
    }

    public void startCamera() {
        if(vie==null){
            Log.d(TAG,"vie is not init, startCamera failed!");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, startCamera failed!");
            return;
        }

        if(svLocal==null){
            Log.d(TAG,"svLocal is not init, startCamera failed!");
            return;
        }

        CameraDesc cameraInfo = vie.getCaptureDevice(getCameraId(getCameraIndex()));
        currentCameraHandle = vie.allocateCaptureDevice(cameraInfo);
        cameraInfo.dispose();
        AVGC.publicCheck(vie.connectCaptureDevice(currentCameraHandle, videoChannel) == 0,
                "Failed to connect capture device");

        VideoCaptureAndroid.setLocalPreview(svLocal.getHolder());
        AVGC.publicCheck(vie.startCapture(currentCameraHandle) == 0, "Failed StartCapture");
        Log.d(TAG,"startCapture:"+vie.startCapture(currentCameraHandle));
        compensateRotation();
    }

    public void stopCamera() {
        if(currentCameraHandle<0){
            Log.d(TAG,"Camera not work, stopCamera failed!");
            return;
        }

        AVGC.publicCheck(vie.stopCapture(currentCameraHandle) == 0, "Failed StopCapture");
        AVGC.publicCheck(vie.releaseCaptureDevice(currentCameraHandle) == 0,
                "Failed ReleaseCaptureDevice");
    }

    public void toggleCamera() {

        if(vie==null){
            Log.d(TAG,"vie is not init, toggleCamera failed");
            return;
        }

        if(videoChannel<0){
            Log.d(TAG,"videoChannel is invalid, toggleCamera failed");
            return;
        }

        if(vieSending==false){
            Log.d(TAG,"videoChannel is not sending, toggleCamera failed");
            return;
        }

        if(useFrontCamera==true){
            if(hasBackCamera()==false){
                Log.d(TAG,"the device has not backCamera, toggleCamera failed");
                return;
            }
        }

        //stopCapture
        AVGC.publicCheck(vie.stopCapture(currentCameraHandle) == 0, "Failed StopCapture");
        AVGC.publicCheck(vie.releaseCaptureDevice(currentCameraHandle) == 0,
                "Failed ReleaseCaptureDevice");

        useFrontCamera = !useFrontCamera;
        //startCapture
        CameraDesc cameraInfo = vie.getCaptureDevice(getCameraId(getCameraIndex()));
        currentCameraHandle = vie.allocateCaptureDevice(cameraInfo);
        cameraInfo.dispose();

        AVGC.publicCheck(vie.connectCaptureDevice(currentCameraHandle, videoChannel) == 0,
                "Failed to connect capture device");
        AVGC.publicCheck(vie.startCapture(currentCameraHandle) == 0, "Failed StartCapture");
        compensateRotation();
    }

    private int getCameraId(int index) {
        for (int i = Camera.getNumberOfCameras() - 1; i >= 0; --i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (index == info.facing) {
                return i;
            }
        }
        throw new RuntimeException("Index does not match a camera");
    }

    public void dispose(){
        if(videoChannel>=0){
            deleteChannel();
        }

        if(orientationListener!=null) {
            orientationListener.disable();
            orientationListener = null;
        }

        if(cameras!=null) {
            cameras = null;
        }

        if(svLocal!=null) {
            svLocal = null;
        }

        if(vie!=null){
            vie = null;
        }

        if(context!=null){
            context = null;
        }
    }

    public void setVideoCodec(int codecNumber) {
        videoCodecIndex = codecNumber;
        updateVideoCodec();
    }

    private void updateVideoCodec() {
        VideoCodecInst codec = getVideoCodec(videoCodecIndex, resolutionIndex);
        AVGC.publicCheck(vie.setSendCodec(videoChannel, codec) == 0, "Failed setReceiveCodec");
        codec.dispose();
    }

    private VideoCodecInst getVideoCodec(int codecNumber, int resolution) {
        VideoCodecInst retVal = vie.getCodec(codecNumber);

        retVal.setStartBitRate(INIT_BITRATE_KBPS);
        retVal.setMaxBitRate(MAX_BITRATE_KBPS);
        retVal.setWidth(RESOLUTIONS[resolution][WIDTH_IDX]);
        retVal.setHeight(RESOLUTIONS[resolution][HEIGHT_IDX]);
        retVal.setMaxFrameRate(SEND_CODEC_FPS);
        return retVal;
    }

    public void setResolutionIndex(int resolution) {
        resolutionIndex = resolution;
        updateVideoCodec();
    }
}
