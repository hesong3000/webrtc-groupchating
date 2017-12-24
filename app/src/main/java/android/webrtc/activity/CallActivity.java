package android.webrtc.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by kai on 2016/8/10.
 */
public class CallActivity extends AppCompatActivity {
    private final static String TAG = "CallActivity";
    protected CallingState callingState = CallingState.CANCED;
    protected CallingDuration callingDuration = CallingDuration.INCALLING;
    protected AudioManager audioManager;
    protected SoundPool soundPool;
    protected Ringtone ringtone;
    protected int outgoing;
    protected String remoteName;
    protected boolean isIncomingCall;
    //呼叫时长
    protected String callDruationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null)
            soundPool.release();
        if (ringtone != null && ringtone.isPlaying())
            ringtone.stop();
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setMicrophoneMute(false);
        //Log.d(TAG,"onDestroy");
    }

    /**
     * 播放拨号响铃
     *
     * @param sound
     * @param number
     */
    protected int playMakeCallSounds() {
        try {
            // 最大音量
            float audioMaxVolumn = audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_RING);
            // 当前音量
            float audioCurrentVolumn = audioManager
                    .getStreamVolume(AudioManager.STREAM_RING);
            float volumnRatio = audioCurrentVolumn / audioMaxVolumn;

            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);

            // 播放
            int id = soundPool.play(outgoing, // 声音资源
                    0.3f, // 左声道
                    0.3f, // 右声道
                    1, // 优先级，0最低
                    -1, // 循环次数，0是不循环，-1是永远循环
                    1); // 回放速度，0.5-2.0之间。1为正常速度
            return id;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 保存通话消息记录
     *
     * @param type
     *            0：音频，1：视频
     * @param isIncoming
     *            true：呼入；false：呼出
     */
    protected void saveCallRecord(int type) {

        String st1 = getResources().getString(R.string.call_duration);
        String st2 = getResources().getString(R.string.Refused);
        String st3 = getResources().getString(
                R.string.The_other_party_has_refused_to);
        String st4 = getResources().getString(R.string.The_other_is_not_online);
        String st5 = getResources().getString(
                R.string.The_other_is_on_the_phone);
        String st6 = getResources().getString(
                R.string.The_other_party_did_not_answer);
        String st7 = getResources().getString(R.string.did_not_answer);
        String st8 = getResources().getString(R.string.Has_been_cancelled);

    }

    // 打开扬声器
    protected void openSpeakerOn() {
        try {
            if (!audioManager.isSpeakerphoneOn())
                audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 关闭扬声器
    protected void closeSpeakerOn() {

        try {
            if (audioManager != null) {
                // int curVolume =
                // audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                if (audioManager.isSpeakerphoneOn())
                    audioManager.setSpeakerphoneOn(false);
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                // curVolume, AudioManager.STREAM_VOICE_CALL);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //SSRC：String to unsigned int
    protected int ssrc2uint(String ssrc){
        char[] chars = ssrc.toUpperCase().toCharArray();
        int ret = 0;
        char step = ('A'-'9')-1;
        for(int j=0;j<chars.length;j++){
            ret <<= 4;
            ret += chars[j] > '9'? chars[j]-'0'-step : chars[j]-'0';
        }
        return ret;
    }

    enum CallingState {
        CANCED, NORMAL, REFUESD, BEREFUESD, UNANSWERED, OFFLINE, NORESPONSE, BUSY
    }

    enum CallingDuration{
        OUTGOING, INCOMING, INCALLING, IDLE, ESTABLISHING
    }

    enum CallingMode {
        VIDEO, AUDIO
    }
}
