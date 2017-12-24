package android.webrtc.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.sub.Interface.Interfaces;
import android.sub.PubStruct;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ichat.Interface.HandlerType;
import com.ichat.Interface.MessageType;
import com.ichat.model.User;
import com.ichat.utility.ApplicationSession;
import com.ichat.utility.PubHolder;

import java.io.InputStream;
import java.util.List;

import imsub.interfaces.IMessage;
import imsub.interfaces.MessageProvider;


/**
 * Created by kai on 2016/8/11.
 */
public class MyLoginActivity extends AppCompatActivity {

    private static final String TAG = "MyLoginActivity";
    private String user_account;
    private String user_password;
    private EditText account_edit;
    private EditText password_edit;
    private EditText called_account;
    private EditText remote_userID;
    private Button login_btn;
    private Button logout_btn;
    private Button outgoingcall_btn;
    private Button audiocall_btn;
    private Button getremoteID_btn;

    private Handler mainHandler = null;
    private Handler videoHandler = null;
    private Handler userHandler = null;
    private Handler voiceHandler = null;
    private Interfaces.ILoginRequest loginRequest = null;
    private Interfaces.IUserRequest userRequest = null;
    private MessageProvider msgProvider = new MessageProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("mysurface","MyLoginActivity onCreate!");
        setContentView(R.layout.mylogin_layout);
        getSupportActionBar().hide();
        //每个Activity都必须加上，将Activity加入Application的管理器中
        ((ApplicationSession)getApplication()).addActivity(this);

        account_edit = (EditText)findViewById(R.id.user_account);
        password_edit = (EditText)findViewById(R.id.user_password);
        account_edit.setText("wktest");
        password_edit.setText("123456");

        called_account = (EditText)findViewById(R.id.called_account);
        getremoteID_btn = (Button)findViewById(R.id.get_remoteID_btn);
        remote_userID = (EditText)findViewById(R.id.remote_userID);

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = activityManager.getRunningTasks(Integer.MAX_VALUE).get(0).topActivity;
        Log.d("data","top activity:"+cn.getClassName());

        mainHandler = new Handler(){
            public void handleMessage(Message msg){
                MessageType type = MessageType.values()[msg.what];
                switch (type){
                    case LOGIN_MSG_OK:{
                        //登录成功后，将User session存入application中
                        PubStruct.UserInfo myMsg = (PubStruct.UserInfo) msg.obj;
                        ApplicationSession session = (ApplicationSession) getApplication();
                        User user = new User();
                        user.setAccount(user_account);
                        user.setName(myMsg.get(PubStruct.UserInfo.Name));
                        user.setUserID(myMsg.get(PubStruct.UserInfo.UserID));
                        user.setAge(myMsg.get(PubStruct.UserInfo.Age));
                        user.setSex(myMsg.get(PubStruct.UserInfo.Sex));
                        user.setAvatar(myMsg.get(PubStruct.UserInfo.Avatar));
                        user.setSign(myMsg.get(PubStruct.UserInfo.Sign));
                        String fileFullname = userRequest.GetLocalFile(user.getUserID(),user.getAvatar());
                        if(fileFullname!=null){    //不为空，表示本地已经存有头像
                            user.setFileFullname(fileFullname);
                        }else     //为空，表示本地没有头像
                            user.setFileFullname(null);
                        session.setUser(user);
                        Toast.makeText(MyLoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case LOGIN_MSG_FAILED: {
                        IMessage myMsg = (IMessage) msg.obj;
                        showMsg("登录失败！\n"+myMsg.getString("ErrorInfo"));
                        break;
                    }

                    case LOGOUT_MSG_OK:{
                        Toast.makeText(MyLoginActivity.this,"注销成功",Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case LOGOUT_MSG_FAILED:{
                        IMessage myMsg = (IMessage) msg.obj;
                        showMsg("注销失败！\n"+myMsg.getString("ErrorInfo"));
                        break;
                    }

                    case INITE_MSG: {
                        String ret = (String) msg.obj;
                        if (ret.contains("OK")) {
                            Log.d(TAG, "PubHolder 初始化成功!");
                            PubHolder.getInstance().addLoginHandler(HandlerType.LOGINACTIVITY_LOGINHANDLER.ordinal(), mainHandler);
                            loginRequest = PubHolder.getInstance().getLoginInstance();
                            PubHolder.getInstance().addUserHandler(HandlerType.LOGINACTIVITY_USERHANDLER.ordinal(), mainHandler);
                            userRequest = PubHolder.getInstance().getUserRequest();
                        } else {
                            showMsg("初始化异常！");
                            finish();
                        }

                        if(loginRequest!=null){
                            IMessage mymsg = msgProvider.createMessage();
                            user_account = account_edit.getText().toString();
                            String password = password_edit.getText().toString();
                            String encryptPassword = loginRequest.EncryptAlgorithm(password);
                            mymsg.setString("UserAccount", user_account);
                            mymsg.setString("PassWord", encryptPassword);
                            loginRequest.PreLogin(mymsg);
                        }
                        break;
                    }
                    default:break;
                }
            }
        };

        try {
            String path = this.getApplicationContext().getCacheDir().getAbsolutePath() + "/IM.conf";
            InputStream is = getResources().getAssets().open("IM.conf");
            PubHolder.getInstance().inite(path,is,mainHandler);
        }catch (Exception e){
            showMsg("初始化异常！" + e.getMessage());
            e.printStackTrace();
            return;
        }

        login_btn = (Button)findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ApplicationSession)getApplication()).setUser(null);
                if(loginRequest!=null){
                    IMessage msg = msgProvider.createMessage();
                    user_account = account_edit.getText().toString();
                    String password = password_edit.getText().toString();
                    String encryptPassword = loginRequest.EncryptAlgorithm(password);
                    msg.setString("UserAccount", user_account);
                    msg.setString("PassWord", encryptPassword);
                    loginRequest.PreLogin(msg);
                }
            }
        });

        logout_btn = (Button)findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMessage msg = msgProvider.createMessage();
                msg.setString("Addition","logout");
                loginRequest.LogOut(msg);
            }
        });

        outgoingcall_btn = (Button)findViewById(R.id.outgoingcall_btn);
        outgoingcall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String remoteUserID = remote_userID.getText().toString();
                if(remoteUserID.length()>0) {
                    onClickVideoCall(remoteUserID);
                }
                else{
                    Toast.makeText(MyLoginActivity.this,"请先获取被叫ID",Toast.LENGTH_SHORT).show();
                }
            }
        });

        audiocall_btn = (Button)findViewById(R.id.audiocall_btn);
        audiocall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remoteUserID = remote_userID.getText().toString();
                if(remoteUserID.length()>0) {
                    onClickAudioCall(remoteUserID);
                }
                else{
                    Toast.makeText(MyLoginActivity.this,"请先获取被叫ID",Toast.LENGTH_SHORT).show();
                }
            }
        });

        getremoteID_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String calledAccount = called_account.getText().toString();
                if(calledAccount.length()>0){
                    onClickGetUserID(calledAccount);
                }
                else{
                    Toast.makeText(MyLoginActivity.this,"请输入被叫账号",Toast.LENGTH_SHORT).show();
                }
            }
        });

        addCallStateListener();
    }

    private void onClickVideoCall(String remoteUserID){
        ApplicationSession session = (ApplicationSession) getApplication();
        String groupType = "Single";
        String sessionID = "0";
        String audioID = "0";
        startVideoCallActivity(session.getUser().getUserID(), remoteUserID, groupType, sessionID, audioID);
    }

    private void onClickAudioCall(String remoteUserID){
        ApplicationSession session = (ApplicationSession) getApplication();
        String groupType = "Single";
        String sessionID = "0";
        startVoiceCallActivity(session.getUser().getUserID(), remoteUserID, groupType, sessionID);
    }

    private void onClickGetUserID(String userAccount){
        IMessage msg = msgProvider.createMessage();
        msg.setString("Account",userAccount);
        msg.setString("Version","0");
        userRequest.GetUserInfoByAccount(msg);
    }

    @Override
    protected void onStop() {
        Log.d("mysurface","MyLoginActivity onStop!");
        super.onStop();
    }

    private String getTopActivity(Activity context)
    {
        ActivityManager manager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE) ;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;
        if(runningTaskInfos != null)
            return (runningTaskInfos.get(0).topActivity).toString() ;
        else
            return null ;
    }

    private void addCallStateListener(){
        videoHandler = new Handler(){
            public void handleMessage(final Message msg) {
                MessageType type = MessageType.values()[msg.what];
                switch (type){
                    case RECVINVITEVIDEO_MSG:
                        onRecvInviteVideo(msg);
                        break;
                    default:break;
                }
            }
        };
        PubHolder.getInstance().addVideoHandler(HandlerType.INITIALACTIVITY_VIDEOHANDLER.ordinal()
                ,videoHandler);
        Log.d("mysurface","MyLoginActivity addVideoHandler INITIALACTIVITY_VIDEOHANDLER");

        userHandler = new Handler(){
            public void handleMessage(final Message msg) {
                MessageType type = MessageType.values()[msg.what];
                switch (type){
                    case GETUSERINFOBYACCOUNT_MSG_OK:
                        PubStruct.UserInfo myMsg = (PubStruct.UserInfo) msg.obj;
                        remote_userID.setText(myMsg.get(PubStruct.UserInfo.UserID));
                        Toast.makeText(MyLoginActivity.this,"获取被叫ID成功",Toast.LENGTH_SHORT).show();
                        break;

                    case GETUSERINFOBYACCOUNT_MSG_FAILED:
                        Toast.makeText(MyLoginActivity.this,"获取被叫ID失败",Toast.LENGTH_SHORT).show();
                        break;
                    default:break;
                }
            }
        };
        PubHolder.getInstance().addUserHandler(HandlerType.INITIALACTIVITY_USERHANDLER.ordinal()
                ,userHandler);
        Log.d("mysurface","MyLoginActivity addVideoHandler INITIALACTIVITY_USERHANDLER");

        voiceHandler = new Handler(){
            public void handleMessage(final Message msg) {
                MessageType type = MessageType.values()[msg.what];
                switch (type){
                    case RECVINVITEVOICE_MSG:
                        onRecvInviteVoice(msg);
                        break;
                    default:break;
                }
            }
        };
        PubHolder.getInstance().addAudioHandler(HandlerType.INITIALACTIVITY_VOICEHANDLER.ordinal()
                ,voiceHandler);
        Log.d("mysurface","MyLoginActivity addAudioHandler INITIALACTIVITY_VOICEHANDLER");
    }

    private void onRecvInviteVideo(Message msg){
        //读取msg数据
        Log.d("mysurface","MyLoginActivity onRecvInviteVideo!");
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = activityManager.getRunningTasks(Integer.MAX_VALUE).get(0).topActivity;
        Log.d("data","onRecvInviteVideo top activity:"+cn.getShortClassName());


        IMessage myMsg = (IMessage)msg.obj;
        if((myMsg.checkKey("FromUser"))&&(myMsg.checkKey("ToUser"))
                &&(myMsg.checkKey("GroupType"))&&(myMsg.checkKey("SessionID"))
                &&(myMsg.checkKey("AudioID"))){
            String fromUserID = myMsg.getString("FromUser");
            String toUserID = myMsg.getString("ToUser");
            String groupType = myMsg.getString("GroupType");
            String sessionID = myMsg.getString("SessionID");
            String audioID = myMsg.getString("AudioID");
            startVideoCallActivity(fromUserID, toUserID, groupType, sessionID, audioID);
        }
    }

    private void onRecvInviteVoice(Message msg){
        Log.d("mysurface","MyLoginActivity onRecvInviteVoice!");

        IMessage myMsg = (IMessage)msg.obj;
        if((myMsg.checkKey("FromUser"))&&(myMsg.checkKey("ToUser"))
                &&(myMsg.checkKey("GroupType"))&&(myMsg.checkKey("SessionID"))){
            String fromUserID = myMsg.getString("FromUser");
            String toUserID = myMsg.getString("ToUser");
            String groupType = myMsg.getString("GroupType");
            String sessionID = myMsg.getString("SessionID");
            startVoiceCallActivity(fromUserID, toUserID, groupType, sessionID);
        }
    }

    private void startVideoCallActivity(String fromUserID, String remoteUserID, String groupType, String sessionID, String audioID){
        Log.d("data","Videocall: "+fromUserID+" "+remoteUserID+" "+sessionID);
        Intent intent = new Intent(MyLoginActivity.this, VideoCallActivity.class);
        intent.putExtra("FromUserID",fromUserID);
        intent.putExtra("ToUserID",remoteUserID);
        intent.putExtra("GroupType",groupType);
        intent.putExtra("SessionID",sessionID);
        intent.putExtra("AudioID",audioID);
        startActivity(intent);
    }

    private void startVoiceCallActivity(String fromUserID, String remoteUserID, String groupType, String sessionID){
        Log.d("data","VoiceCall: "+fromUserID+" "+remoteUserID+" "+sessionID);
        Intent intent = new Intent(MyLoginActivity.this, VoiceCallActivity.class);
        intent.putExtra("FromUserID",fromUserID);
        intent.putExtra("ToUserID",remoteUserID);
        intent.putExtra("GroupType",groupType);
        intent.putExtra("SessionID",sessionID);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mysurface","MyLoginActivity onDestroy!");
        PubHolder.getInstance().removeLoginHandler(HandlerType.LOGINACTIVITY_LOGINHANDLER.ordinal());
        PubHolder.getInstance().removeVideoHandler(HandlerType.INITIALACTIVITY_VIDEOHANDLER.ordinal());
        PubHolder.getInstance().removeUserHandler(HandlerType.INITIALACTIVITY_USERHANDLER.ordinal());
        PubHolder.getInstance().removeAudioHandler(HandlerType.INITIALACTIVITY_VOICEHANDLER.ordinal());
        ((ApplicationSession)getApplication()).removeActivity(this);
    }

    @Override
    protected void onPause() {
        Log.d("mysurface","MyLoginActivity onPause!");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("mysurface","MyLoginActivity onResume!");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("mysurface","MyLoginActivity onRestart!");
        //如果上一次用户没有退出登录，则自动登录
        //autoLogin();
    }

    private void autoLogin(){
        //如果上一次用户没有退出登录，则自动登录
        ApplicationSession session = (ApplicationSession)getApplication();
        User sessionUser = session.getUser();
        if(sessionUser!=null){
            Log.d(TAG,sessionUser.getName() + " login automatically！");
            Intent intent = new Intent(MyLoginActivity.this,InitialActivity.class);
            startActivity(intent);
        }
    }

    public void showMsg(String msg){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MyLoginActivity.this);
        dialog.setTitle("消息");
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }
}
