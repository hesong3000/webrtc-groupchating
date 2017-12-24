package android.webrtc.avgroupchatproxy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webrtc.activity.R;
import android.webrtc.utils.FastBlur;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by kai on 2016/11/23.
 */
public class ChannelHolder {
    private final String TAG = "ChannelHolder";
    protected void turnIntoAudioChatState(Context context, RelativeLayout layout, Bitmap bitmap){
        Log.d(TAG,"turnIntoAudioChatState");
        layout.removeAllViews();
        //获得parentLayout尺寸
        int layoutWidth = layout.getWidth();
        int layoutHeight = layout.getHeight();
        ImageView addItem = new ImageView(context);
        addItem.setImageBitmap(bitmap);
        addItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        addItem.setLayoutParams(lp);
        layout.addView(addItem);
    }

    protected void turnIntoInvitingState(Context context, RelativeLayout layout, Bitmap bitmap){
        Log.d("GroupVideoActivity","turnIntoInvitingState");
        layout.removeAllViews();
        //获得parentLayout尺寸
        int layoutWidth = layout.getWidth();
        int layoutHeight = layout.getHeight();

        if((layoutWidth==0)||(layoutHeight==0)){
            Log.d("GroupVideoActivity","layoutWidth/layoutHeight must be > 0");
            return;
        }

        //创建ImageView并添加子View
        ImageView addItem = new ImageView(context);
        addItem.setImageBitmap(bitmap);
        addItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        addItem.setLayoutParams(lp);
        layout.addView(addItem);

        //添加gif动画
        GifImageView gifView = new GifImageView(context);
        gifView.setImageResource(R.drawable.calling_gif);
        lp = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        gifView.setLayoutParams(lp);
        layout.addView(gifView);


        //背景图片模糊处理
        applyBlur(context,addItem,gifView,layoutWidth,layoutHeight);
    }

    protected void turnIntoInvitingState(Context context, RelativeLayout layout, Bitmap bitmap,int width,int height){
        Log.d(TAG,"turnIntoInvitingState");
        layout.removeAllViews();
        //获得parentLayout尺寸
        int layoutWidth = width;
        int layoutHeight = height;
        Log.d("SendChannelHolder","layoutWidth:"+layoutWidth+" layoutHeight:"+layoutHeight);

        if((layoutWidth==0)||(layoutHeight==0)){
            Log.d(TAG,"layoutWidth/layoutHeight must be > 0");
            return;
        }
        //创建ImageView并添加子View
        ImageView addItem = new ImageView(context);
        addItem.setImageBitmap(bitmap);
        addItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        addItem.setLayoutParams(lp);
        layout.addView(addItem);

        //添加gif动画
        GifImageView gifView = new GifImageView(context);
        gifView.setImageResource(R.drawable.calling_gif);
        lp = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.setMargins(5, 5, 5, 5);
        gifView.setLayoutParams(lp);
        layout.addView(gifView);

        //背景图片模糊处理
        applyBlur(context,addItem,gifView,layoutWidth,layoutHeight);
    }

    private void applyBlur(final Context context, final ImageView image, final View view
            ,final int layoutWidth, final int layoutHeight) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                image.buildDrawingCache();

                Bitmap bmp = image.getDrawingCache();
                blur(context, bmp, view, layoutWidth, layoutHeight);
                return true;
            }
        });
    }

    private void blur(Context context, Bitmap bkg, View view, int layoutWidth, int layoutHeight) {
        float scaleFactor = 1;
        float radius = 5;
        Bitmap overlay = Bitmap.createBitmap((int) (layoutWidth / scaleFactor),
                (int) (layoutHeight / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        if(bkg!=null) {
            canvas.drawBitmap(bkg, 0, 0, paint);

            overlay = FastBlur.doBlur(overlay, (int) radius, true);
            view.setBackground(new BitmapDrawable(context.getResources(), overlay));
        }
        else{
            Log.d("SendChannelHolder","bkg is null!!");
        }
    }
}
