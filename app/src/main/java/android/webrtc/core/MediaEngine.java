/*
 *  Copyright (c) 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package android.webrtc.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

import org.webrtc.videoengine.ViERenderer;
import org.webrtc.videoengine.VideoCaptureAndroid;

import java.io.File;

public class MediaEngine implements VideoDecodeEncodeObserver {
  // TODO(henrike): Most of these should be moved to xml (since static).

  //add by lmm
  private static final Logger logger = LoggerFactory.getLogger();

  private static final int VCM_VP8_PAYLOAD_TYPE = 100;
  private static final int SEND_CODEC_FPS = 30;
  // TODO(henrike): increase INIT_BITRATE_KBPS to 2000 and ensure that
  // 720p30fps can be acheived (on hardware that can handle it). Note that
  // setting 2000 currently leads to failure, so that has to be resolved first.
  private static final int INIT_BITRATE_KBPS = 768;
  private static final int MAX_BITRATE_KBPS = 1576;
  private static final String LOG_DIR = "webrtc";
  private static final int WIDTH_IDX = 0;
  private static final int HEIGHT_IDX = 1;
  private static final int[][] RESOLUTIONS = {
    {176,144}, {320,240}, {352,288}, {640,480}, {1280,720}
  };
  // Arbitrary choice of 4/5 volume (204/256).
  private static final int volumeLevel = 204;

  public static int numberOfResolutions() { return RESOLUTIONS.length; }

  public static String[] resolutionsAsString() {
    String[] retVal = new String[numberOfResolutions()];
    for (int i = 0; i < numberOfResolutions(); ++i) {
      retVal[i] = RESOLUTIONS[i][0] + "x" + RESOLUTIONS[i][1];
    }
    return retVal;
  }

  // Checks for and communicate failures to user (logcat and popup).
  /*
  private void check(boolean value, String message) {
    if (value) {
      return;
    }
    Log.e("WEBRTC-CHECK", message);
    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
    alertDialog.setTitle("WebRTC Error");
    alertDialog.setMessage(message);
    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
        "OK",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            return;
          }
        }
                          );
    alertDialog.show();
  }
  */

  private void check(boolean value, String message){
    if (value) {
      return;
    }

    //add by lmm
    String debug_msg = "[MediaEngine::check] value:" + value + " message:" + message;
    logger.debug(debug_msg);

    Log.e("WEBRTC-CHECK", message);
  }

  // Converts device rotation to camera rotation. Rotation depends on if the
  // camera is back facing and rotate with the device or front facing and
  // rotating in the opposite direction of the device.
  private static int rotationFromRealWorldUp(CameraInfo info,
                                             int deviceRotation) {
    int coarseDeviceOrientation =
        (int)(Math.round((double)deviceRotation / 90) * 90) % 360;
    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
      // The front camera rotates in the opposite direction of the
      // device.
      int inverseDeviceOrientation = 360 - coarseDeviceOrientation;
      return (inverseDeviceOrientation + info.orientation) % 360;
    }
    else {
      return (coarseDeviceOrientation + info.orientation) % 360;
    }
  }

  // Shared Audio/Video members.
  private Context context;
  private String remoteIp;
  private boolean enableTrace;

    // Audio
  private VoiceEngine voe;
  private int audioChannel;
  private boolean audioEnabled;
  private boolean voeRunning;
  private int audioCodecIndex;
  private int audioTxPort;
  private int audioRxPort;
  //zhangdoudou
  private boolean voeRecording;
  private boolean voePlayouting;

  private boolean speakerEnabled;
  private boolean enableAgc;
  private boolean enableNs;
  private boolean enableAecm;

  private boolean audioRtpDump;
  private boolean apmRecord;

  // Video
  private VideoEngine vie;
  private int videoChannel;
  private boolean receiveVideo;
  private boolean sendVideo;
  private boolean vieRunning;
  private int videoCodecIndex;
  private int resolutionIndex;
  private int videoTxPort;
  private int videoRxPort;

  private BroadcastReceiver headsetListener;
  private boolean headsetPluggedIn;

  // Indexed by CameraInfo.CAMERA_FACING_{BACK,FRONT}.
  private CameraInfo cameras[];
  private boolean useFrontCamera;
  private int currentCameraHandle;
  private boolean enableNack;
  // openGl, surfaceView or mediaCodec (integers.xml)
  private int viewSelection;
  private boolean videoRtpDump;

  private SurfaceView svLocal;
  private SurfaceView svRemote;

  MediaCodecVideoDecoder externalCodec = null;

  private boolean isCapture = false;
  private boolean bSwitchView = false;

  private int inFps;
  private int inKbps;
  private int outFps;
  private int outKbps;
  private int inWidth;
  private int inHeight;

  private OrientationEventListener orientationListener;
  private int deviceOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;

  public MediaEngine(Context context) {
    String debugMsg1 = "[MediaEngine::MediaEngine] construct!";
    logger.debug(debugMsg1);

    this.context = context;
    voe = new VoiceEngine();
    check(voe.init() == 0, "Failed voe Init");
    audioChannel = voe.createChannel();
    check(audioChannel >= 0, "Failed voe CreateChannel");
    vie = new VideoEngine();
    check(vie.init() == 0, "Failed voe Init");
    check(vie.setVoiceEngine(voe) == 0, "Failed setVoiceEngine");
    videoChannel = vie.createChannel();
    check(audioChannel >= 0, "Failed voe CreateChannel");
    check(vie.connectAudioChannel(videoChannel, audioChannel) == 0,
        "Failed ConnectAudioChannel");

    cameras = new CameraInfo[2];
    for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
      CameraInfo info = new CameraInfo();
      Camera.getCameraInfo(i, info);
      cameras[info.facing] = info;
    }

    setDefaultCamera();
    check(voe.setSpeakerVolume(volumeLevel) == 0,
        "Failed setSpeakerVolume");
    check(voe.setAecmMode(VoiceEngine.AecmModes.SPEAKERPHONE, false) == 0,
        "VoE set Aecm speakerphone mode failed");
    check(vie.setKeyFrameRequestMethod(videoChannel,
            VideoEngine.VieKeyFrameRequestMethod.
                    KEY_FRAME_REQUEST_PLI_RTCP) == 0,
        "Failed setKeyFrameRequestMethod");

    svLocal = new SurfaceView(context);
    svRemote = ViERenderer.CreateRenderer(context, true);

    /*
    check(vie.registerObserver(videoChannel, this) == 0,
        "Failed registerObserver");*/

    // TODO(hellner): SENSOR_DELAY_NORMAL?
    // Listen to changes in device orientation.

    orientationListener =
        new OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
          public void onOrientationChanged (int orientation) {
            deviceOrientation = orientation;
            compensateRotation();
          }
        };

    orientationListener.enable();

    // Set audio mode to communication
    AudioManager audioManager =
            ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    // Listen to headset being plugged in/out.
    IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

    headsetListener = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(Intent.ACTION_HEADSET_PLUG) == 0) {
          headsetPluggedIn = intent.getIntExtra("state", 0) == 1;
            /*Toast.makeText(context, "耳机热插拔",
                    Toast.LENGTH_SHORT).show();*/
          updateAudioOutput();
        }
      }
    };
    context.registerReceiver(headsetListener, receiverFilter);

    //add by lmm
    String debugMsg = "[MediaEngine::MediaEngine] successed!";
    logger.debug(debugMsg);
  }

  private void updateAudioOutput() {
      boolean useSpeaker = !headsetPluggedIn && speakerEnabled;
      AudioManager audioManager =
              ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
      int mode = audioManager.getMode();

      audioManager.setSpeakerphoneOn(useSpeaker);

      //setSpeakerphoneOn useSpeaker
      //add by lmm
      String debugMsg = "[MediaEngine::updateAudioOutput] setSpeakerphoneOn useSpeaker:" + useSpeaker;
    logger.debug(debugMsg);
  }

  public void dispose() {
    Log.d("mysurface1","mediaEngine dispose");
    check(!voeRunning && !voeRunning, "Engines must be stopped before dispose");

    if(orientationListener!=null) {
      orientationListener.disable();
      orientationListener = null;
    }

    if(headsetListener!=null) {
      context.unregisterReceiver(headsetListener);
      headsetListener = null;
    }

    if(svLocal!=null) {
      svLocal = null;
    }

    if(svRemote!=null) {
      svRemote = null;
    }

    if(cameras!=null) {
      cameras = null;
    }

    /*
    check(vie.deregisterObserver(videoChannel) == 0,
            "Failed deregisterObserver");*/
    if (externalCodec != null) {
      check(vie.deRegisterExternalReceiveCodec(videoChannel,
              VCM_VP8_PAYLOAD_TYPE) == 0,
              "Failed to deregister external decoder");

      externalCodec = null;
    }
    check(vie.deleteChannel(videoChannel) == 0, "DeleteChannel");
    vie.dispose();
    check(voe.deleteChannel(audioChannel) == 0, "VoE delete channel failed");
    voe.dispose();
    Log.d("mysurface1","mediaEngine dispose done");
    vie = null;
    voe = null;
    context = null;
  }

  public void start() {
    if (audioEnabled) {
      startVoE();
    }
    if (receiveVideo || sendVideo) {
      startViE();
    }
  }

  public void stop() {

    if(isVieRunning()==true) {
      check(vie.disconnectAudioChannel(videoChannel) == 0, "disconnectAudioChannel");
      stopVie();
    }

    if(isVoeRunning()==true) {
      Log.d("webrtc","stopVoe begin");
      stopVoe();
    }
  }

  public boolean isRunning() {
    return voeRunning || vieRunning;
  }

  public boolean isVieRunning(){
    return vieRunning;
  }

  public boolean isVoeRunning(){
    return voeRunning;
  }

  //zhangdoudou
  public boolean isVoeRecording()
  {
    return voeRecording;
  }

  public boolean isVoePlayouting(){ return voePlayouting;}

  public void setRemoteIp(String remoteIp) {
    this.remoteIp = remoteIp;
    UpdateSendDestination();
  }

  public String remoteIp() { return remoteIp; }

  public void setTrace(boolean enable) {
    if (enable) {
      vie.setTraceFile("/sdcard/trace.txt", false);
      vie.setTraceFilter(VideoEngine.TraceLevel.TRACE_ALL);
      return;
    }
    vie.setTraceFilter(VideoEngine.TraceLevel.TRACE_MEMORY);
  }

  private String getDebugDirectory() {
    // Should create a folder in /scard/|LOG_DIR|
    return Environment.getExternalStorageDirectory().toString() + "/" +
        LOG_DIR;
  }

  private boolean createDebugDirectory() {
    File webrtc_dir = new File(getDebugDirectory());
    if (!webrtc_dir.exists()) {
      return webrtc_dir.mkdir();
    }
    return webrtc_dir.isDirectory();
  }

  public void startVoE() {
    //Log.d("webrtc","startVoE11");
    check(!voeRunning, "VoE already started");
    check(voe.startListen(audioChannel) == 0, "Failed StartListen");
    check(voe.startPlayout(audioChannel) == 0, "VoE start playout failed");
    check(voe.startSend(audioChannel) == 0, "VoE start send failed");
    voeRunning = true;

    //success
    //add by lmm
    String debugMsg = "[MediaEngine::startVoe] success!";
    logger.debug(debugMsg);
  }

  public void stopVoe() {
    check(voeRunning, "VoE not started");
    check(voe.stopReceive(audioChannel) == 0, "VoE stop receive failed");
    check(voe.stopSend(audioChannel) == 0, "VoE stop send failed");
    check(voe.stopPlayout(audioChannel) == 0, "VoE stop playout failed");
    check(voe.stopListen(audioChannel) == 0, "VoE stop listen failed");
    voeRunning = false;
  }

  public void setAudio(boolean audioEnabled) {
    this.audioEnabled = audioEnabled;
  }

  public boolean audioEnabled() { return audioEnabled; }

  public int audioCodecIndex() { return audioCodecIndex; }

  public void setAudioCodec(int codecNumber) {
    audioCodecIndex = codecNumber;
    CodecInst codec = voe.getCodec(codecNumber);
    check(voe.setSendCodec(audioChannel, codec) == 0, "Failed setSendCodec");
    codec.dispose();
    //add by lmm
    String debugMsg = "[MediaEngine::seetAudioCodec] successs";
    logger.debug(debugMsg);

  }

  public String[] audioCodecsAsString() {
    String[] retVal = new String[voe.numOfCodecs()];
    for (int i = 0; i < voe.numOfCodecs(); ++i) {
      CodecInst codec = voe.getCodec(i);
      retVal[i] = codec.toString();
      codec.dispose();
    }
    return retVal;
  }

  private CodecInst[] defaultAudioCodecs() {
    CodecInst[] retVal = new CodecInst[voe.numOfCodecs()];
     for (int i = 0; i < voe.numOfCodecs(); ++i) {
      retVal[i] = voe.getCodec(i);
    }
    return retVal;
  }

  public int getIsacIndex() {
    CodecInst[] codecs = defaultAudioCodecs();
    for (int i = 0; i < codecs.length; ++i) {
      if (codecs[i].name().contains("ISAC")) {
        return i;
      }
    }
    return 0;
  }

  public void setAudioTxPort(int audioTxPort) {
    this.audioTxPort = audioTxPort;
    UpdateSendDestination();
  }

  public int audioTxPort() { return audioTxPort; }

  public void setAudioRxPort(int audioRxPort) {
    check(voe.setLocalReceiver(audioChannel, audioRxPort) == 0,
        "Failed setLocalReceiver");
    this.audioRxPort = audioRxPort;
  }

  public int audioRxPort() { return audioRxPort; }

  public boolean agcEnabled() { return enableAgc; }

  public void setAgc(boolean enable) {
    enableAgc = enable;
    VoiceEngine.AgcConfig agc_config =
        new VoiceEngine.AgcConfig(3, 9, true);
    check(voe.setAgcConfig(agc_config) == 0, "VoE set AGC Config failed");
    check(voe.setAgcStatus(enableAgc, VoiceEngine.AgcModes.FIXED_DIGITAL) == 0,
        "VoE set AGC Status failed");

    //add by lmm
    String debugMsg = "[Media::setAgc] enable:" + enable;
    logger.debug(debugMsg);
  }

  public boolean nsEnabled() { return enableNs; }

  public void setNs(boolean enable) {
    enableNs = enable;
    check(voe.setNsStatus(enableNs,
            VoiceEngine.NsModes.MODERATE_SUPPRESSION) == 0,
        "VoE set NS Status failed");
    //add by lmm
    String debugMsg = "[Media::setNs] enable:" + enable;
    logger.debug(debugMsg);

  }

  public boolean aecmEnabled() { return enableAecm; }

  public void setEc(boolean enable) {
    enableAecm = enable;
    check(voe.setEcStatus(enable, VoiceEngine.EcModes.AECM) == 0,
        "voe setEcStatus");
    //add by lmm
    String debugMsg = "[Media::setEc] enable:" + enable;
    logger.debug(debugMsg);
  }

  public boolean speakerEnabled() {
    return speakerEnabled;
  }

  public void setSpeaker(boolean enable) {
    speakerEnabled = enable;
    //updateAudioOutput();
  }

  // Debug helpers.
  public boolean apmRecord() { return apmRecord; }

  public boolean audioRtpDump() { return audioRtpDump; }

  public void setDebuging(boolean enable) {
    apmRecord = enable;
    if (!enable) {
      check(voe.stopDebugRecording() == 0, "Failed stopping debug");
      return;
    }
    if (!createDebugDirectory()) {
      check(false, "Unable to create debug directory.");
      return;
    }
    String debugDirectory = getDebugDirectory();
    check(voe.startDebugRecording(debugDirectory +  String.format("/apm_%d.dat",
                System.currentTimeMillis())) == 0,
        "Failed starting debug");
  }

  public void setIncomingVoeRtpDump(boolean enable) {
    audioRtpDump = enable;
    if (!enable) {
      check(voe.stopRtpDump(videoChannel,
              VoiceEngine.RtpDirections.INCOMING) == 0,
          "voe stopping rtp dump");
      return;
    }
    String debugDirectory = getDebugDirectory();
    check(voe.startRtpDump(videoChannel, debugDirectory +
            String.format("/voe_%d.rtp", System.currentTimeMillis()),
            VoiceEngine.RtpDirections.INCOMING) == 0,
        "voe starting rtp dump");
  }

  public void startViE() {
    check(!vieRunning, "ViE already started");

    if (receiveVideo) {
      /*
      if (viewSelection ==
          context.getResources().getInteger(R.integer.openGl)) {
        svRemote = ViERenderer.CreateRenderer(context, true);
      } else if (viewSelection ==
          context.getResources().getInteger(R.integer.surfaceView)) {
        svRemote = ViERenderer.CreateRenderer(context, false);
      } else {
        externalCodec = new MediaCodecVideoDecoder(context);
        svRemote = externalCodec.getView();
      }
      */
      //startCamera不受sendVideo的限制

      if (externalCodec != null) {
        check(vie.registerExternalReceiveCodec(videoChannel,
                VCM_VP8_PAYLOAD_TYPE, externalCodec, true) == 0,
            "Failed to register external decoder");
      } else {
        check(vie.addRenderer(videoChannel, svRemote,
                0, 0, 0, 1, 1) == 0, "Failed AddRenderer");
        check(vie.startRender(videoChannel) == 0, "Failed StartRender");
      }
      check(vie.startReceive(videoChannel) == 0, "Failed StartReceive");

    }

    startCamera();
    if (sendVideo) {
      check(vie.startSend(videoChannel) == 0, "Failed StartSend");
    }

    vieRunning = true;

    //add by lmm
    String debugMsg = "[Media::startViE] success";
    logger.debug(debugMsg);
  }

  public void stopVie() {

    Log.d("mysurface1","stopvie begin");
    if (!vieRunning) {
      return;
    }
    check(vie.stopSend(videoChannel) == 0, "StopSend");
    stopCamera();
    check(vie.stopReceive(videoChannel) == 0, "StopReceive");
    if (externalCodec != null) {
      check(vie.deRegisterExternalReceiveCodec(videoChannel,
              VCM_VP8_PAYLOAD_TYPE) == 0,
              "Failed to deregister external decoder");

      externalCodec.dispose();
      externalCodec = null;
      Log.d("mysurface1","deRegisterExternalReceiveCodec");
    } else {
      Log.d("mysurface1","stopRender+removeRenderer");
      check(vie.stopRender(videoChannel) == 0, "StopRender");
      check(vie.removeRenderer(videoChannel) == 0, "RemoveRenderer");
    }

    if((svLocal.getParent()!=null)){
      Log.d("svLocal.getParent:","mysurface1"+svLocal.getParent().toString());
      ((LinearLayout)svLocal.getParent()).removeView(svLocal);
    }

    if(svRemote.getParent()!=null){
      Log.d("svRemote.getParent:","mysurface1"+svRemote.getParent().toString());
      ((LinearLayout)svRemote.getParent()).removeView(svRemote);
    }

    vieRunning = false;

    orientationListener.disable();
    svLocal = null;
    svRemote = null;
    orientationListener = null;
    cameras = null;

    //add by lmm
    String debugMsg = "[Media::stopVie] success";
    logger.debug(debugMsg);
    Log.d("mysurface1","stopvie end");
  }

  public void setReceiveVideo(boolean receiveVideo) {
    this.receiveVideo = receiveVideo;
  }

  public boolean receiveVideo() { return receiveVideo; }

  public void setSendVideo(boolean sendVideo) { this.sendVideo = sendVideo; }

  public boolean sendVideo() { return sendVideo; }

  public int videoCodecIndex() { return videoCodecIndex; }

  public void setVideoCodec(int codecNumber) {
    videoCodecIndex = codecNumber;
    updateVideoCodec();
  }

  public String[] videoCodecsAsString() {
    String[] retVal = new String[vie.numberOfCodecs()];
    for (int i = 0; i < vie.numberOfCodecs(); ++i) {
      VideoCodecInst codec = vie.getCodec(i);
      retVal[i] = codec.toString();
      codec.dispose();
    }
    return retVal;
  }

  public int resolutionIndex() { return resolutionIndex; }

  public void setResolutionIndex(int resolution) {
    resolutionIndex = resolution;
    updateVideoCodec();
  }

  private void updateVideoCodec() {
    VideoCodecInst codec = getVideoCodec(videoCodecIndex, resolutionIndex);
    check(vie.setSendCodec(videoChannel, codec) == 0, "Failed setReceiveCodec");
    codec.dispose();
  }

  private VideoCodecInst getVideoCodec(int codecNumber, int resolution) {
    VideoCodecInst retVal = vie.getCodec(codecNumber);

    retVal.setStartBitRate(INIT_BITRATE_KBPS);
    retVal.setMaxBitRate(MAX_BITRATE_KBPS);
    retVal.setWidth(RESOLUTIONS[resolution][WIDTH_IDX]);
    retVal.setHeight(RESOLUTIONS[resolution][HEIGHT_IDX]);
    retVal.setMaxFrameRate(SEND_CODEC_FPS);

    inWidth = RESOLUTIONS[resolution][WIDTH_IDX];
    inHeight = RESOLUTIONS[resolution][HEIGHT_IDX];

    return retVal;
  }

  public int getResolutionWidthByIndex(int resolution){
    return RESOLUTIONS[resolution][WIDTH_IDX];
  }

  public int getResolutionHeightByIndex(int resolution){
    return RESOLUTIONS[resolution][HEIGHT_IDX];
  }

  public void setVideoRxPort(int videoRxPort) {
    this.videoRxPort = videoRxPort;
    check(vie.setLocalReceiver(videoChannel, videoRxPort) == 0,
        "Failed setLocalReceiver");
    check(true,"");
    //add by lmm
    String debugMsg = "[Media::setVideoRxPort] videoRxPort:" + videoRxPort;
    logger.debug(debugMsg);
  }

  public int videoRxPort() { return videoRxPort; }

  public void setVideoTxPort(int videoTxPort) {
    this.videoTxPort = videoTxPort;
    UpdateSendDestination();
    //add by lmm
    String debugMsg = "[Media::setVideoTxPort] videoTxPort:" + videoTxPort;
    logger.debug(debugMsg);
  }

  private void UpdateSendDestination() {
    if (remoteIp == null) {
      return;
    }
    if (audioTxPort != 0) {
      check(voe.setSendDestination(audioChannel, audioTxPort,
              remoteIp) == 0, "VoE set send destination failed");
    }
    if (videoTxPort != 0) {
      check(vie.setSendDestination(videoChannel, videoTxPort, remoteIp) == 0,
          "Failed setSendDestination");
    }

    // Setting localSSRC manually (arbitrary value) for loopback test,
    // As otherwise we will get a clash and a new SSRC will be set,
    // Which will reset the receiver and other minor issues.

    /*
    if (remoteIp.equals("127.0.0.1")) {
      check(vie.setLocalSSRC(videoChannel, 0x01234567) == 0,
              "Failed setLocalSSRC");
    }
    */
  }

  public void setAudioSSRC(int ssrc){
    check(voe.setLocalSSRC(audioChannel,ssrc) == 0,"Failed setAudioSSRC");
  }

  public void setVideoSSRC(int ssrc){
    check(vie.setLocalSSRC(videoChannel,ssrc) == 0,"Failed setVideoSSRC");
  }

  public int videoTxPort() {
    return videoTxPort;
  }

  public boolean hasMultipleCameras() {
    return Camera.getNumberOfCameras() > 1;
  }

  public boolean frontCameraIsSet() {
    return useFrontCamera;
  }

  // Set default camera to front if there is a front camera.
  private void setDefaultCamera() {
    useFrontCamera = hasFrontCamera();
  }

  public void toggleCamera() {

    if(useFrontCamera==true){
      if(hasBackCamera()==false){
        return;
      }
    }

    if (vieRunning) {
      isCapture = false;
      //VideoCaptureAndroid.setLocalPreview(null);
      check(vie.stopCapture(currentCameraHandle) == 0, "Failed StopCapture");
      check(vie.releaseCaptureDevice(currentCameraHandle) == 0,
              "Failed ReleaseCaptureDevice");
    }
    useFrontCamera = !useFrontCamera;
    if (vieRunning) {
      CameraDesc cameraInfo = vie.getCaptureDevice(getCameraId(getCameraIndex()));
      currentCameraHandle = vie.allocateCaptureDevice(cameraInfo);
      cameraInfo.dispose();

      check(vie.connectCaptureDevice(currentCameraHandle, videoChannel) == 0,
              "Failed to connect capture device");

      check(vie.startCapture(currentCameraHandle) == 0, "Failed StartCapture");
      compensateRotation();
      isCapture = true;
    }
  }

  public void togglePauseView(){
    if (vieRunning) {
      isCapture = false;
      //VideoCaptureAndroid.setLocalPreview(null);
      check(vie.stopCapture(currentCameraHandle) == 0, "Failed StopCapture");
      check(vie.stopRender(videoChannel) == 0, "StopRender");
      check(vie.removeRenderer(videoChannel) == 0, "Failed RemoveRenderer");

      if((svLocal.getParent()!=null)){
        Log.d("svLocal.getParent:","mysurface1"+svLocal.getParent().toString());
        ((LinearLayout)svLocal.getParent()).removeView(svLocal);
      }

      if(svRemote.getParent()!=null){
        Log.d("svRemote.getParent:","mysurface1"+svRemote.getParent().toString());
        ((LinearLayout)svRemote.getParent()).removeView(svRemote);
      }
    }
  }

  public void toggleResumeView(){
    if (vieRunning) {
      check(vie.startCapture(currentCameraHandle) == 0, "Failed StopCapture");
      compensateRotation();
      check(vie.addRenderer(videoChannel, svRemote,
              0, 0, 0, 1, 1) == 0, "Failed AddRenderer");
      check(vie.startRender(videoChannel) == 0, "Failed StartRender");
      isCapture = true;
    }
  }

  public void startCamera() {

    CameraDesc cameraInfo = vie.getCaptureDevice(getCameraId(getCameraIndex()));
    Log.d("webrtc","cameraInfo:"+cameraInfo.toString());
    currentCameraHandle = vie.allocateCaptureDevice(cameraInfo);
    cameraInfo.dispose();

    Log.d("currentCameraHandle","currentCameraHandle:"+currentCameraHandle);
    check(vie.connectCaptureDevice(currentCameraHandle, videoChannel) == 0,
        "Failed to connect capture device");
    // Camera and preview surface.

    if(svLocal!=null) {
      VideoCaptureAndroid.setLocalPreview(svLocal.getHolder());
    }
    else{
      Log.d("webrtc","svLocal is non pointer");
    }

    check(vie.startCapture(currentCameraHandle) == 0, "Failed StartCapture");
    Log.d("SendVideoChannel","startCapture:"+vie.startCapture(currentCameraHandle));
    compensateRotation();

    isCapture = true;
    //add by lmm
    String debugMsg = "[Media::startCamera] success";
    logger.debug(debugMsg);

  }

  public void stopCamera() {
    isCapture = false;
    check(vie.stopCapture(currentCameraHandle) == 0, "Failed StopCapture");
    //check(vie.disconnectCaptureDevice(videoChannel) ==0, "Failed disconnectCaptureDevice");
    check(vie.releaseCaptureDevice(currentCameraHandle) == 0,
        "Failed ReleaseCaptureDevice");
    //add by lmm
    String debugMsg = "[Media::stopCamera] success";
    logger.debug(debugMsg);
  }

  public void SetVoiceRTCPEnable(boolean enable){
    check(voe.SetVoiceRTCPEnable(audioChannel,enable) == 0,"Failed SetVoiceRTCPEnable");

    //add by lmm
    String debugMsg = "[Media::SetVoiceRTCPEnable] enable:"+enable;
    logger.debug(debugMsg);
  }

  public void SetVideoRTCPMode(int rtcpMode){
    check(vie.SetVideoRTCPMode(videoChannel,rtcpMode) == 0,"Failed SetVideoRTCPEnable");
  }

  private boolean hasFrontCamera() {
    return cameras[CameraInfo.CAMERA_FACING_FRONT] != null;
  }

  public boolean hasBackCamera() {
    return cameras[CameraInfo.CAMERA_FACING_BACK] != null;
  }

  public SurfaceView getRemoteSurfaceView() {
    return svRemote;
  }

  public SurfaceView getLocalSurfaceView() {
    return svLocal;
  }

  public void setViewSelection(int viewSelection) {
    this.viewSelection = viewSelection;
  }

  public int viewSelection() { return viewSelection; }

  public boolean nackEnabled() { return enableNack; }

  public void setNack(boolean enable) {
    enableNack = enable;
    check(vie.setNackStatus(videoChannel, enableNack) == 0,
        "Failed setNackStatus");
    //add by lmm
    String debugMsg = "[Media::setNack] enable:"+enable;
    logger.debug(debugMsg);
  }

  // Collates current state into a multiline string.
  public String sendReceiveState() {
    int packetLoss = 0;
    if (vieRunning) {
      RtcpStatistics stats = vie.getReceivedRtcpStatistics(videoChannel);
      if (stats != null) {
        // Calculate % lost from fraction lost.
        // Definition of fraction lost can be found in RFC3550.
        packetLoss = (stats.fractionLost * 100) >> 8;
      }
    }
    String retVal =
        "fps in/out: " + inFps + "/" + outFps + "\n" +
        "kBps in/out: " + inKbps / 1024 + "/ " + outKbps / 1024 + "\n" +
        "resolution: " + inWidth + "x" + inHeight + "\n" +
        "loss: " + packetLoss + "%";
    return retVal;
  }

  MediaEngineObserver observer;
  public void setObserver(MediaEngineObserver observer) {
    this.observer = observer;
  }

  // Callbacks from the VideoDecodeEncodeObserver interface.
  public void incomingRate(int videoChannel, int framerate, int bitrate) {
    inFps = framerate;
    inKbps = bitrate;
    newStats();
  }

  public void incomingCodecChanged(int videoChannel,
      VideoCodecInst videoCodec) {
    inWidth = videoCodec.width();
    inHeight = videoCodec.height();
    videoCodec.dispose();
    newStats();
  }

  public void requestNewKeyFrame(int videoChannel) {}

  public void outgoingRate(int videoChannel, int framerate, int bitrate) {
    outFps = framerate;
    outKbps = bitrate;
    newStats();
  }

  private void newStats() {
    if (observer != null) {
      observer.newStats(sendReceiveState());
    }
  }

  // Debug helpers.
  public boolean videoRtpDump() { return videoRtpDump; }
  //InComing方向的RTP存储
  public void setIncomingVieRtpDump(boolean enable) {
    videoRtpDump = enable;
    if (!enable) {
      check(vie.stopRtpDump(videoChannel,
              VideoEngine.RtpDirections.INCOMING) == 0,
          "vie StopRTPDump");
      return;
    }
    String debugDirectory = getDebugDirectory();
    check(vie.startRtpDump(videoChannel, debugDirectory +
            String.format("/vie_%d.rtp", System.currentTimeMillis()),
            VideoEngine.RtpDirections.INCOMING) == 0,
        "vie StartRtpDump");
  }

  private int getCameraIndex() {
    return useFrontCamera ? CameraInfo.CAMERA_FACING_FRONT :
        CameraInfo.CAMERA_FACING_BACK;
  }

  private int getCameraId(int index) {
    for (int i = Camera.getNumberOfCameras() - 1; i >= 0; --i) {
      CameraInfo info = new CameraInfo();
      Camera.getCameraInfo(i, info);
      if (index == info.facing) {
        return i;
      }
    }
    throw new RuntimeException("Index does not match a camera");
  }

  private void compensateRotation() {
    if (svLocal == null) {
      // Not rendering (or sending).
      return;
    }
    if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
      return;
    }

    if(isCapture==false){
      return;
    }

    int cameraRotation = rotationFromRealWorldUp(
        cameras[getCameraIndex()], deviceOrientation);

    if (cameras[getCameraIndex()].facing == CameraInfo.CAMERA_FACING_FRONT) {
      cameraRotation = 270;
    }
    else{
      cameraRotation = 90;
    }

    // Egress streams should have real world up as up.
    if(currentCameraHandle!=0) {
      check(vie.setRotateCapturedFrames(currentCameraHandle, cameraRotation) == 0,
              "Failed setRotateCapturedFrames: camera " + currentCameraHandle +
                      "rotation " + cameraRotation);
    }
  }

  public void loudSpeakerEnable(boolean enable){
    check(voe.setLoudspeakerStatus(enable)==0,"Failed setLoudSpeaker");
  }

  public void setMicMute(boolean enable){
    check(voe.setMicrophoneMute(audioChannel,enable)==0,"Failed setMicMute");

    //add by lmm
    String debugMsg = "[Media::setMicMute] enable:"+enable;
    logger.debug(debugMsg);
  }

  //传入存储文件全路径
  public void startAudioRecord(String fileName)
  {
    check(!voeRecording, "Record Test:********************VoE recording started");
    check(voe.startRecordingMicrophone(fileName) == 0, "Record Test:********************Failed StartRecord");
    voeRecording = true;
  }

  public void stopAudioRecord()
  {
    check(voeRecording, "StopRecord Test:********************VoE recording stoped");
    check(voe.stopRecordingMicrophone() == 0, "StopRecord Test:********************Failed StopRecord");
    voeRecording = false;
  }

  public void startAudioPlayout(String fileName)
  {
    check(!voePlayouting, "Playout Test:********************VoE playout started");
    check(voe.startPlayingFileLocally(audioChannel,fileName,false) == 0, "StartPlayout Test:********************Failed StartPlayout");
    check(voe.startPlayout(audioChannel) == 0,"start playout*************");
    voePlayouting = true;
  }

  public void stopAudioPlayout()
  {
    check(voePlayouting, "StopPlayout Test:********************VoE playout stoped");
    check(voe.stopPlayout(audioChannel) == 0, "StopPlayout Test:********************VoE playout stoped");
    check(voe.stopPlayingFileLocally(audioChannel) == 0, "StopRecord Test:********************Failed StopPlayout");

    voePlayouting = false;
  }

  public void connectAudioAndVideo(boolean enableConnect)
  {
    if(enableConnect == true) {
      check(vie.connectAudioChannel(videoChannel, audioChannel) == 0, "Failed connectAudioAndVideo");
    }
  }
}
