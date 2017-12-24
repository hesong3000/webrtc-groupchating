package android.webrtc.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webrtc.activity.R;
import android.webrtc.avgroupchatproxy.AVGC;
import android.webrtc.utils.DisplayUtil;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Jaeger on 16/2/24.
 *
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */

/**
 * update by loften on 16/4/21.
 */
public class AVGroupChatMemView<T> extends ViewGroup{
    //private String TAG = NineGridImageView.class.getName();
    private Context context;
    private String TAG = "AVGroupChatMemView";
    private int mMaxSize = AVGC.MaxLayoutNum;  //最大图片数
    private int mGap = 0; //宫格间距
    private int leftPadding = 20;
    private int maxImageCountPerRow = 4;    //一行最多放4个
    private int defaultImageSize = 96;
    private int parentWidth;//父组件宽
    private int parentHeight;//父组件高

    private List<ImageView> mViewList = new ArrayList<>();

    private AVGroupChatMemAdapter<T> mAdapter;

    public AVGroupChatMemView(Context context) {
        this(context,null);
    }

    public AVGroupChatMemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AVGroupChatMemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NineGridImageView);
        this.mGap = (int) typedArray.getDimension(R.styleable.NineGridImageView_imgGap, 8);
        typedArray.recycle();
        this.context = context;
    }

    /**
     * 计算所有ChildView的宽度和高度 然后根据ChildView的计算结果，设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        parentWidth = measureWidth(widthMeasureSpec);
        parentHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(parentWidth,parentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(context==null)
            return;
        //计算绘制区域
        int viewLeft = l;
        int viewTop = 10;
        int viewWidth = r-l;
        int viewHeight = b-t-DisplayUtil.dip2px(context,140);
        int rows = 0;
        int imageSize = 0;
        //4人一行
        int childcount = getChildCount();
        if(childcount==0)
            return;
        if(childcount>=AVGC.MaxLayoutNum)
            return;

        if((childcount>0)&&(childcount<=4)){
            rows = 1;
        }
        else if((childcount>4)&&(childcount<=(AVGC.MaxLayoutNum-1))){
            rows = 2;
        }

        int imageCountPerRow = 0;
        if(childcount>maxImageCountPerRow){
            imageCountPerRow = maxImageCountPerRow;
        }
        else{
            imageCountPerRow = childcount;
        }

        //纵向算image的最大宽度
        int maxSize_H = (viewHeight-(rows-1)*mGap)/rows;
        //横向算image的最大宽度,左右空白20宽度
        int maxSize_W = (viewWidth-leftPadding*2-(imageCountPerRow-1)*mGap)/imageCountPerRow;

        //取较小的作为image的宽高
        if(maxSize_W>=maxSize_H){
            imageSize = maxSize_H;
        }
        else{
            imageSize = maxSize_W;
        }

        if(imageSize>=defaultImageSize){
            imageSize =defaultImageSize;
        }

        if(context!=null) {
            imageSize = DisplayUtil.dip2px(context,imageSize);
        }

        layoutChildrenView(viewLeft+leftPadding,viewTop,viewWidth-2*leftPadding,viewHeight,imageSize,rows,imageCountPerRow);
    }

    private void layoutChildrenView(int l, int t, int width, int height, int imageSize, int rows, int imageCountPerRow){
        if(rows<=0)
            return;
        int childrenCount = getChildCount();
        for(int i=0;i<childrenCount;i++){
            ImageView childrenView = (ImageView)getChildAt(i);
            int childViewLeft = 0;
            int childViewTop = 0;
            int rowIndex = i/maxImageCountPerRow;
            int columnIndex = i%maxImageCountPerRow;
            int tmpLeftPadding = (width-(imageCountPerRow-1)*mGap-imageCountPerRow*imageSize)/2;
            childViewLeft = l+tmpLeftPadding+columnIndex*(imageSize+mGap);
            childViewTop = t+rowIndex*(imageSize+mGap);
            childrenView.layout(childViewLeft,childViewTop,childViewLeft+imageSize,childViewTop+imageSize);
        }
    }

    /**
     * 获得 View
     * 保证了 View的重用
     *
     * @param position 位置
     */
    public ImageView getView(final int position){
        if(position < mViewList.size()){
            return mViewList.get(position);
        }
        return null;
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(AVGroupChatMemAdapter adapter){
        mAdapter = adapter;
    }

    /**
     * 设置宫格间距
     *
     * @param gap 宫格间距 px
     */
    public void setGap(int gap){
        mGap = gap;
    }

    /**
     * 对宫格的宽高进行重新定义
     */
    private int measureWidth(int measureSpec){
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if(specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }else{
            result = 200;
            if(specMode == MeasureSpec.AT_MOST){
                result = Math.min(result,specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec){
        int result = 0;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if(specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }else{
            result = 200;
            if(specMode == MeasureSpec.AT_MOST){
                result = Math.min(result,specSize);
            }
        }
        return result;
    }

    public int addLayout(Bitmap bitmap){
        if(mViewList.size()>=(mMaxSize-1)){
            return -1;
        }

        ImageView layout = mAdapter.generateView(getContext());
        layout.setImageBitmap(bitmap);
        mViewList.add(layout);
        addView(layout,generateDefaultLayoutParams());
        return mViewList.size()-1;
    }

    public void updateImageView(ImageView imageView, Bitmap bitmap){
        ListIterator<ImageView> listIterator = mViewList.listIterator();
        while (listIterator.hasNext()){
            ImageView tmpchildImage = listIterator.next();
            if(tmpchildImage.equals(imageView)){
                tmpchildImage.setImageBitmap(bitmap);
                break;
            }
        }
    }

    public void removeLayout(int position){
        if((position>=0)&&(position<mViewList.size())) {
            ImageView layout = mViewList.remove(position);
            removeView(layout);
        }
    }

    public int getChildViewCount(){
        return mViewList.size();
    }

    public void notifyDataSetChanged(){
        requestLayout();
    }
}
