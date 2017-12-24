package android.webrtc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webrtc.avgroupchatproxy.AVGC;
import android.webrtc.avgroupchatproxy.UserInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.code.microlog4android.config.PropertyConfigurator;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;



/**
 * Created by kai on 2016/7/24.
 */
public class InitialActivity extends AppCompatActivity {
    private final String TAG = "InitialActivity";
    private Button linkVideoCallView;
    private Button avGroupChatSetting;
    private EditText remoteIP_edit;
    private EditText remotePortA_edit;
    private EditText remotePortV_edit;
    private EditText localPortA_edit;
    private EditText localPortV_edit;
    private EditText AudioSSRC_edit;
    private EditText VideoSSRC_edit;
    private Button startActivityForResult;
    public final static int requestCode = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode== InitialActivity.requestCode)&&(resultCode==RESULT_OK)){
            List<String> result = (List<String>) data.getSerializableExtra(AVGC.startActivity_Result_OK);
            if(result!=null){
                Iterator<String> iterator = result.iterator();
                while (iterator.hasNext()){
                    String content = iterator.next();
                    Log.d(TAG,"onActivityResult "+content);
                }
            }
        }
        else if((requestCode== InitialActivity.requestCode)&&(resultCode==RESULT_CANCELED)){
            Log.d(TAG,"onActivityResult 用户取消");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_layout);

        //add by lmm
        PropertyConfigurator.getConfigurator(this).configure();

        linkVideoCallView = (Button)findViewById(R.id.linkVideoCallView);
        remoteIP_edit = (EditText)findViewById(R.id.remoteIP);
        remotePortA_edit = (EditText)findViewById(R.id.remotePort_A);
        remotePortV_edit = (EditText)findViewById(R.id.remotePort_V);
        localPortA_edit = (EditText)findViewById(R.id.localPort_A);
        localPortV_edit = (EditText)findViewById(R.id.localPort_V);
        AudioSSRC_edit = (EditText)findViewById(R.id.Audio_SSRC);
        VideoSSRC_edit = (EditText)findViewById(R.id.Video_SSRC);
        avGroupChatSetting = (Button)findViewById(R.id.avGroupChatSetting);
        startActivityForResult = (Button)findViewById(R.id.startActivityForResult);
        remoteIP_edit.setText("192.168.10.90");
        remotePortA_edit.setText("2000");
        remotePortV_edit.setText("30000");
        localPortA_edit.setText("11111");
        localPortV_edit.setText("2000");
        AudioSSRC_edit.setText("F0226304");
        VideoSSRC_edit.setText("123456");

        linkVideoCallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remoteIP = remoteIP_edit.getText().toString();
                String remotePort_A = remotePortA_edit.getText().toString();
                String remotePort_V = remotePortV_edit.getText().toString();
                String localPort_A = localPortA_edit.getText().toString();
                String localport_V = localPortV_edit.getText().toString();
                String AudioSSRC = AudioSSRC_edit.getText().toString();
                String VideoSSRC = VideoSSRC_edit.getText().toString();

                int remotePort_A_i = Integer.parseInt(remotePort_A,10);
                int remotePort_V_i = Integer.parseInt(remotePort_V,10);
                int localPort_A_i = Integer.parseInt(localPort_A,10);
                int localPort_V_i = Integer.parseInt(localport_V,10);
                int AudioSSRC_i = ssrc_str2int(AudioSSRC);
                int VideoSSRC_i = ssrc_str2int(VideoSSRC);

                Intent intent = new Intent(InitialActivity.this,MyWebRTCDemo.class);
                //Intent intent = new Intent(InitialActivity.this,VideoCallActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("remoteIP",remoteIP);
                bundle.putInt("remotePort_A",remotePort_A_i);
                bundle.putInt("remotePort_V",remotePort_V_i);
                bundle.putInt("localPort_A",localPort_A_i);
                bundle.putInt("localport_V",localPort_V_i);
                bundle.putInt("AudioSSRC",AudioSSRC_i);
                bundle.putInt("VideoSSRC",VideoSSRC_i);
                Toast.makeText(InitialActivity.this,"AudioSSRC:"+AudioSSRC_i+" VideoSSRC:"+VideoSSRC_i,
                        Toast.LENGTH_LONG).show();
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        avGroupChatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("UserInfo");
                UserInfo userInfo = new UserInfo();
                userInfo.setUserID("intentTest");
                userInfo.setUserState("chatting");
                userInfo.setUserName("wkwkwkw");
                intent.putExtra("myUserInfo",(Serializable)userInfo);
                intent.putExtra("hello","intent");
                GroupChatSettingActivity.myStartActivity(InitialActivity.this, userInfo, intent);
            }
        });

        startActivityForResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialActivity.this,ResultActivity.class);
                startActivityForResult(intent,requestCode);
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d("mysurface","InitialActivity onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d("mysurface","InitalActivity onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d("mysurface","InitalActivity onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d("mysurface","InitalActivity onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("mysurface","InitalActivity onDestroy");
        super.onDestroy();
    }

    private int ssrc_str2int(String ssrc){
        char[] chars = ssrc.toUpperCase().toCharArray();
        int ret = 0;
        char step = ('A' - '9') - 1;
        for(int j = 0; j < chars.length; j++){
            ret <<= 4;
            ret += chars[j] > '9' ? chars[j]-'0' - step : chars[j] - '0';
        }

        return ret;
    }

}
