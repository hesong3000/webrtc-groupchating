package android.webrtc.avgroupchatproxy;

import android.util.Log;

/**
 * Created by kai on 2016/11/12.
 */
public class AVGC {
    public final static String RemoteIP = "RemoteIP";
    public final static String RemoteRecvPort_A = "RemoteRecvPort_A";
    public final static String RemoteRecvPort_V = "RemoteRecvPort_V";
    public final static int MaxLayoutNum = 9;
    public final static int MinLayoutNum = 0;


    //测试使用b----------
    public final static String SSRC_A = "SSRC_A";
    public final static String SSRC_V = "SSRC_V";
    public final static String APort_chan1 = "APort_chan1";
    public final static String VPort_chan1 = "VPort_chan1";
    public final static String APort_chan2 = "APort_chan2";
    public final static String VPort_chan2 = "VPort_chan2";
    public final static String startActivity_Result_OK = "activity_result_ok";
    public final static String startActivity_Result_CANCEL = "activity_result_cancel";
    //测试使用e----------

    public static void publicCheck(boolean value, String message){
        if (value) {
            return;
        }
        Log.e("AVGC-check", message);
    }
}

