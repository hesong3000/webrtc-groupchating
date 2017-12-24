package android.webrtc.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webrtc.core.MediaEngine;
import android.webrtc.core.NativeWebRtcContextRegistry;
import android.webrtc.utils.DisplayUtil;
import android.webrtc.utils.FastBlur;
import android.webrtc.utils.ImageResUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

import pl.droidsonroids.gif.GifImageView;

public class GifOverLayout extends AppCompatActivity {
    private final String TAG = "GifOverLayout";
    private RelativeLayout relativeLayoutContainer;
    private Button addImage;
    private Button addGifOverImage;
    private Button addSurface;
    private Button removeImage;
    private Button removeGifOverImage;
    private Button removeSurface;
    private NativeWebRtcContextRegistry contextRegistry = null;
    private MediaEngine mediaEngine = null;
    private String chattingStatus = "chatting";
    private int relativeLayoutWidth = 0;
    private int relativeLayoutHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_over_layout);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        //防止闪屏
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        initMediaEngine();
        findViews();
        initViews();
        setOnclicked();
    }

    @Override
    protected void onDestroy() {
        mediaEngine.dispose();
        contextRegistry.unRegister();
        contextRegistry = null;
        mediaEngine = null;
        super.onDestroy();
    }

    private void initMediaEngine() {
        contextRegistry = new NativeWebRtcContextRegistry();
        contextRegistry.register(this);
        mediaEngine = new MediaEngine(this);
    }

    private void findViews() {
        relativeLayoutContainer = (RelativeLayout) findViewById(R.id.relativeLayoutContainer);
        addImage = (Button) findViewById(R.id.addImage);
        addGifOverImage = (Button) findViewById(R.id.addGifOverImage);
        addSurface = (Button) findViewById(R.id.addSurface);
        removeImage = (Button) findViewById(R.id.removeImage);
        removeGifOverImage = (Button) findViewById(R.id.removeGifOverImage);
        removeSurface = (Button) findViewById(R.id.removeSurface);
    }

    private void initViews() {
        Point point = DisplayUtil.getScreenMetrics(this);
        int screenWidth = point.x;
        int screenHeight = point.y;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 2, screenHeight / 2);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayoutContainer.setLayoutParams(layoutParams);
        relativeLayoutWidth = screenWidth / 2;
        relativeLayoutHeight = screenHeight / 2;
    }

    private void setOnclicked() {
        addImage.setOnClickListener(new GifOverLayoutClickListener());
        addGifOverImage.setOnClickListener(new GifOverLayoutClickListener());
        addSurface.setOnClickListener(new GifOverLayoutClickListener());
        removeImage.setOnClickListener(new GifOverLayoutClickListener());
        removeGifOverImage.setOnClickListener(new GifOverLayoutClickListener());
        removeSurface.setOnClickListener(new GifOverLayoutClickListener());
    }

    private void addImageProc() {
        Bitmap bitmap = ImageResUtil.getBitmapFromResources(this,R.drawable.test_avatar);
        if (chattingStatus.contains(";with-video;")) {
            mediaEngine.stopCamera();
        }
        relativeLayoutContainer.removeAllViews();

        ImageView addItem = new ImageView(this);
        addItem.setImageBitmap(bitmap);
        addItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(relativeLayoutWidth, relativeLayoutWidth);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        addItem.setLayoutParams(lp);
        relativeLayoutContainer.addView(addItem);
        chattingStatus = ";chatting;" + ";with-voice;";
    }

    private void addGifOverImageProc() throws IOException {
        if (chattingStatus.contains(";with-video;")) {
            mediaEngine.stopCamera();
        }
        relativeLayoutContainer.removeAllViews();

        Bitmap bitmap = ImageResUtil.getBitmapFromResources(this,R.drawable.test_avatar);
        ImageView addItem = new ImageView(this);
        addItem.setImageBitmap(bitmap);
        addItem.setScaleType(ImageView.ScaleType.CENTER_CROP);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(relativeLayoutWidth, relativeLayoutWidth);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        addItem.setLayoutParams(lp);
        relativeLayoutContainer.addView(addItem);
        //添加gif动画
        GifImageView gifView = new GifImageView(this);
        gifView.setImageResource(R.drawable.calling_gif);
        lp = new RelativeLayout.LayoutParams(relativeLayoutWidth, relativeLayoutWidth);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        gifView.setLayoutParams(lp);
        relativeLayoutContainer.addView(gifView);
        Log.d(TAG,"gifView width:"+gifView.getWidth()+" height:"+gifView.getHeight());
        //背景图片模糊处理
        applyBlur(addItem,gifView);
    }

    private void applyBlur(final ImageView image, final View view) {
        image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                image.getViewTreeObserver().removeOnPreDrawListener(this);
                image.buildDrawingCache();

                Bitmap bmp = image.getDrawingCache();
                blur(bmp, view);
                return true;
            }
        });
    }

    private void blur(Bitmap bkg, View view) {
        float scaleFactor = 1;
        float radius = 5;
        Log.d(TAG,"view.getMeasuredWidth:"+view.getMeasuredWidth()+" view.getMeasuredHeight:"+view.getMeasuredHeight());
        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 5,(relativeLayoutHeight-relativeLayoutWidth)/2, paint);

        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
    }

    private void addSurfaceProc() {
        if (!chattingStatus.contains(";with-video;")) {
            relativeLayoutContainer.removeAllViews();
            SurfaceView localSurfaceView = mediaEngine.getLocalSurfaceView();
            relativeLayoutContainer.addView(localSurfaceView);
            mediaEngine.startCamera();
            chattingStatus += ";with-video;";
        } else {
            Log.d(TAG, "chattingStatus is already with video!!");
        }
    }

    private void removeImageProc() {
        Log.d(TAG, "removeImageProc");
        relativeLayoutContainer.removeAllViews();
    }

    private void removeGifOverImageProc() {

    }

    private void removeSurfaceProc() {
        if (chattingStatus.contains(";with-video;")) {
            mediaEngine.stopCamera();
            relativeLayoutContainer.removeAllViews();
            chattingStatus = chattingStatus.replace(";with-video;", "");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class GifOverLayoutClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.addImage:
                    addImageProc();
                    break;
                case R.id.addGifOverImage:
                    try {
                        addGifOverImageProc();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.addSurface:
                    addSurfaceProc();
                    break;
                case R.id.removeImage:
                    removeImageProc();
                    break;
                case R.id.removeGifOverImage:
                    removeGifOverImageProc();
                    break;
                case R.id.removeSurface:
                    removeSurfaceProc();
                    break;
                default:
                    break;
            }
        }
    }
}
