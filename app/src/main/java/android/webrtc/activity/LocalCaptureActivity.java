package android.webrtc.activity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webrtc.core.MediaEngine;
import android.webrtc.core.NativeWebRtcContextRegistry;
import android.webrtc.widget.NineGridImageView;
import android.webrtc.widget.NineGridImageViewAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LocalCaptureActivity extends AppCompatActivity {
    private final String TAG = "LocalCaptrueActivity";
    private NineGridImageView gridView;
    private NineGridImageViewAdapter<String> adapter;
    private NativeWebRtcContextRegistry contextRegistry = null;
    private MediaEngine mediaEngine = null;
    private LinearLayout linearLayout;
    private Button startCaptureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_capture);
        //防止闪屏
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
        startCaptureBtn = (Button)findViewById(R.id.startCaptureBtn);

        contextRegistry = new NativeWebRtcContextRegistry();
        contextRegistry.register(this);
        mediaEngine = new MediaEngine(this);
        Log.d(TAG,"mediaEngine:"+mediaEngine.toString());
        gridView = (NineGridImageView)findViewById(R.id.localCapture_gridview);
        adapter = createNineGridImageViewAdapter();
        gridView.setAdapter(adapter);

        int position = gridView.addLayout();
        RelativeLayout layout = gridView.getView(position);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        Log.d(TAG,"LayoutParams width:"+params.width+" height:"+params.height);

        SurfaceView localSurfaceView = mediaEngine.getLocalSurfaceView();

        layout.addView(localSurfaceView);
        mediaEngine.startCamera();

        /*
        startCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = gridView.addLayout(getResources().getDrawable(R.drawable.default_useravatar));
                LinearLayout layout = gridView.getView(position);
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                Log.d(TAG,"LayoutParams width:"+params.width+" height:"+params.height);

                SurfaceView localSurfaceView = mediaEngine.getLocalSurfaceView();

                layout.addView(localSurfaceView);
                mediaEngine.startCamera();
            }
        });
        */
    }

    @Override
    protected void onDestroy() {
        mediaEngine.stopCamera();
        mediaEngine.dispose();
        contextRegistry.unRegister();
        contextRegistry = null;
        mediaEngine = null;
        super.onDestroy();
    }

    private NineGridImageViewAdapter<String> createNineGridImageViewAdapter(){
        return new NineGridImageViewAdapter<String>() {
            @Override
            protected RelativeLayout generateView(Context context) {
                return super.generateView(context);
            }
        };
    }
}
