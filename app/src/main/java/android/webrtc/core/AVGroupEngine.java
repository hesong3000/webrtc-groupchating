package android.webrtc.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by kai on 2016/10/26.
 */
public class AVGroupEngine {

    private static final String TAG = "AVGroupEngine";
    private Context context;
    private VoiceEngine voe;
    private VideoEngine vie;
    private boolean enableAgc;
    private boolean enableNs;
    private boolean enableAecm;
    private BroadcastReceiver headsetListener;
    private boolean headsetPluggedIn;
    // Arbitrary choice of 4/5 volume (204/256).
    private static final int volumeLevel = 204;

    public VoiceEngine getVoe(){
        return voe;
    }

    public VideoEngine getVie(){
        return vie;
    }

    private void check(boolean value, String message){
        if (value) {
            return;
        }
        Log.e(TAG, message);
    }

    public AVGroupEngine(Context context){
        this.context = context;
        voe = new VoiceEngine();
        check(voe.init() == 0, "Failed voe Init");
        vie = new VideoEngine();
        check(vie.init() == 0, "Failed voe Init");
        check(vie.setVoiceEngine(voe) == 0, "Failed setVoiceEngine");

        check(voe.setSpeakerVolume(volumeLevel) == 0,
                "Failed setSpeakerVolume");
        check(voe.setAecmMode(VoiceEngine.AecmModes.SPEAKERPHONE, false) == 0,
        "VoE set Aecm speakerphone mode failed");

        //设置噪声抑制(true)
        setNs(true);
        //设置回声消除(true)
        setEc(true);
        //设置自动增益控制(true)
        setAgc(true);

        /*
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
                    updateAudioOutput();
                }
            }
        };
        context.registerReceiver(headsetListener, receiverFilter);*/
    }

    private void updateAudioOutput() {
        boolean useSpeaker = !headsetPluggedIn;
        AudioManager audioManager =
                ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        audioManager.setSpeakerphoneOn(useSpeaker);
    }

    public void dispose() {
        /*
        if(headsetListener!=null) {
            context.unregisterReceiver(headsetListener);
            headsetListener = null;
        }*/

        vie.dispose();
        voe.dispose();
        vie = null;
        voe = null;
        context = null;
    }

    public void setAgc(boolean enable) {
        enableAgc = enable;
        VoiceEngine.AgcConfig agc_config =
                new VoiceEngine.AgcConfig(3, 9, true);
        check(voe.setAgcConfig(agc_config) == 0, "VoE set AGC Config failed");
        check(voe.setAgcStatus(enableAgc, VoiceEngine.AgcModes.FIXED_DIGITAL) == 0,
                "VoE set AGC Status failed");
    }

    public void setNs(boolean enable) {
        enableNs = enable;
        check(voe.setNsStatus(enableNs,
                VoiceEngine.NsModes.MODERATE_SUPPRESSION) == 0,
                "VoE set NS Status failed");
    }

    public void setEc(boolean enable) {
        enableAecm = enable;
        check(voe.setEcStatus(enable, VoiceEngine.EcModes.AECM) == 0,
                "voe setEcStatus");
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

    private CodecInst[] defaultAudioCodecs() {
        CodecInst[] retVal = new CodecInst[voe.numOfCodecs()];
        for (int i = 0; i < voe.numOfCodecs(); ++i) {
            retVal[i] = voe.getCodec(i);
        }
        return retVal;
    }
}
