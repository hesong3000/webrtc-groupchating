package android.webrtc.activity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webrtc.avgroupchatproxy.AVGC;
import android.webrtc.widget.AVGroupChatMemAdapter;
import android.webrtc.widget.AVGroupChatMemView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InviteUIDemo extends AppCompatActivity {
    private final String TAG = "InviteUIDemo";
    private LinearLayout avgroupchat_incoming_ui;
    private ImageView avgroupchat_useravatar;
    private TextView avgroupchat_invitorname;
    private AVGroupChatMemView avgroupchat_members_gridview;
    private AVGroupChatMemAdapter<String> mems_adapter;
    private ImageView avgroupchat_refuse_call;
    private ImageView avgroupchat_answer_call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_uidemo);

        getSupportActionBar().hide();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        findViews();
        initViews();
        initMemsGrid();
        setOnListener();
    }

    private void findViews(){
        avgroupchat_incoming_ui = (LinearLayout)findViewById(R.id.avgroupchat_incoming_ui);
        avgroupchat_useravatar = (ImageView)findViewById(R.id.avgroupchat_useravatar);
        avgroupchat_invitorname = (TextView) findViewById(R.id.avgroupchat_invitorname);
        avgroupchat_members_gridview = (AVGroupChatMemView)findViewById(R.id.avgroupchat_members_gridview);
        avgroupchat_refuse_call = (ImageView)findViewById(R.id.avgroupchat_refuse_call);
        avgroupchat_answer_call = (ImageView)findViewById(R.id.avgroupchat_answer_call);
    }

    private void initViews(){
        avgroupchat_useravatar.setImageResource(R.drawable.test_avatar);
        avgroupchat_invitorname.setText("王凯");
    }

    private void initMemsGrid(){
        avgroupchat_members_gridview.setGap(20);
        mems_adapter = createMemsImageViewAdapter();
        avgroupchat_members_gridview.setAdapter(mems_adapter);
    }

    private AVGroupChatMemAdapter<String> createMemsImageViewAdapter(){
        return new AVGroupChatMemAdapter<String>() {
            @Override
            protected ImageView generateView(Context context) {
                return super.generateView(context);
            }
        };
    }

    private void setOnListener(){
        avgroupchat_answer_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources res = getResources();
                Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.test_avatar);
                addMemView(bmp);
            }
        });

        avgroupchat_refuse_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delMemView();
            }
        });
    }

    private void delMemView(){
        if(avgroupchat_members_gridview.getChildViewCount()<= AVGC.MinLayoutNum){
            Log.d(TAG,"avgroupchat_members_gridview do not have members!!");
            return;
        }

        avgroupchat_members_gridview.removeLayout(0);
    }

    private int addMemView(Bitmap bitmap){
        if(avgroupchat_members_gridview.getChildViewCount()>= (AVGC.MaxLayoutNum-1)){
            Log.d(TAG,"avgroupchat_members_gridview has enough members!!");
            return -1;
        }

        int position = avgroupchat_members_gridview.addLayout(bitmap);
        if(position>=0){
            ImageView image = avgroupchat_members_gridview.getView(position);
            if(image!=null){
                Log.d(TAG,"avgroupchat_members_gridview addLayout success, layout:"+image.toString());
                //gridView更新
                avgroupchat_members_gridview.notifyDataSetChanged();
                return position;
            }
        }
        return -1;
    }
}
