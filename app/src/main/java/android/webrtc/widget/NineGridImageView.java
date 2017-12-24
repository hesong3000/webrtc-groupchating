package android.webrtc.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webrtc.activity.R;
import android.webrtc.avgroupchatproxy.AVGC;
import android.widget.RelativeLayout;

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
public class NineGridImageView<T> extends ViewGroup{
    //private String TAG = NineGridImageView.class.getName();
    private String TAG = "NineGridImageView";
    private int mRowCount; //行数
    private int mColumnCount;  //列数

    private int mMaxSize = AVGC.MaxLayoutNum;  //最大图片数
    private int mGap = 0; //宫格间距
    private Context context;

    private int parentWidth;//父组件宽
    private int parentHeight;//父组件高
    private float displayRatio; //显示的长宽比

    public int getChildViewWidth() {
        return childViewWidth;
    }

    public int getChildViewHeight() {
        return childViewHeight;
    }

    private int childViewWidth;
    private int childViewHeight;

    public void setDisplayRatio(float ratio){
        displayRatio = ratio;
    }

    private List<RelativeLayout> mViewList = new ArrayList<>();

    private NineGridImageViewAdapter<T> mAdapter;

    public NineGridImageView(Context context) {
        this(context,null);
        displayRatio = 1;
    }

    public NineGridImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        displayRatio = 1;
    }

    public NineGridImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        displayRatio = 1;
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
        //layoutChildrenView();
        Log.d("GroupVideoActivity","onLayout");
        //计算绘制区域
        int viewLeft = l;
        int viewTop = t;
        int viewWidth = r-l;
        int viewHeight = b-t;
        int paintTop = 0;
        int paintLeft = 0;
        int paintWidth = 0;
        int paintHeight = 0;
        float curRatio = (float)viewHeight/(float)viewWidth;
        if(curRatio>=displayRatio){
            //上下留黑
            paintWidth = viewWidth;
            paintHeight = (int)(paintWidth*displayRatio);
            paintLeft = viewLeft;
            paintTop = viewTop+(viewHeight-paintHeight)/2;
        }
        else{
            //左右留黑
            paintHeight = viewHeight;
            paintWidth = (int)(paintHeight/displayRatio);
            paintLeft = viewLeft+(viewWidth-paintWidth)/2;
            paintTop = viewTop;
        }
        layoutChildrenView(paintLeft,paintTop,paintWidth,paintHeight);
    }

    //子画面之间无间隔
    private void layoutChildrenView(int l, int t, int width, int height){
        int childrenCount = getChildCount();
        if(childrenCount==0)
            return;
        if(childrenCount>9)
            return;

        int gridType = 0;   //根据childrenCount数量判定九宫格种类：单宫格、二宫格、三宫格、4宫格、9宫格
        if(childrenCount==1){
            gridType = 1;
        }
        else if(childrenCount==2){
            gridType = 2;
        }
        else if(childrenCount==3){
            gridType = 3;
        }
        else if(childrenCount==4){
            gridType = 4;
        }
        else{
            gridType = 9;
        }

        for(int i = 0; i < childrenCount; i++){
            RelativeLayout childrenView = (RelativeLayout)getChildAt(i);
            if(childrenView==null)
                continue;

            int childViewLeft = 0;
            int childViewTop = 0;
            childViewWidth = 0;
            childViewHeight = 0;
            if(gridType==1){
                //单宫格时，单宫格面积占总布局的1/4
                childViewWidth = width/2;
                childViewHeight = height/2;
                childViewLeft = l+childViewWidth/2;
                childViewTop = t+childViewHeight/2;
                childrenView.layout(childViewLeft,childViewTop,childViewLeft+childViewWidth,childViewTop+childViewHeight);
                Log.d("LocalCaptrueActivity","childrenView layout width:"+childViewWidth
                        +" height:"+childViewHeight);
            }
            else if(gridType==2){
                //二宫格时，单宫格面积占总布局的1/4，纵向居中
                childViewWidth = width/2;
                childViewHeight = height/2;
                childViewTop = t+childViewHeight/2;
                childViewLeft = l+i*childViewWidth;
                childrenView.layout(childViewLeft,childViewTop,childViewLeft+childViewWidth,childViewTop+childViewHeight);
            }
            else if(gridType==3){
                //三宫格时，单宫格面积占总布局的1/4
                //前两个宫格填充总布局的上半部分，第三个宫格在总布局的下半部分居中
                childViewWidth = width/2;
                childViewHeight = height/2;
                if(i<=1){
                    childViewTop = t;
                    childViewLeft = l+i*childViewWidth;
                }
                else if(i==2){
                    childViewTop = t+childViewHeight;
                    childViewLeft = l+childViewWidth/2;
                }
                childrenView.layout(childViewLeft,childViewTop,childViewLeft+childViewWidth,childViewTop+childViewHeight);
            }
            else if(gridType==4){
                //四宫格时，单宫格面积占总布局的1/4，并填充整个布局
                childViewWidth = width/2;
                childViewHeight = height/2;
                int columnCount = 2;
                int rowIndex = i/columnCount;
                int columnIndex = i%columnCount;
                childViewTop = t+rowIndex*childViewHeight;
                childViewLeft = l+columnIndex*childViewWidth;
                childrenView.layout(childViewLeft,childViewTop,childViewLeft+childViewWidth,childViewTop+childViewHeight);
            }
            else if(gridType==9){
                //九宫格时，单宫格面积占总布局的1/9，并填充整个布局
                childViewWidth = width/3;
                childViewHeight = height/3;
                int columnCount = 3;
                int rowIndex = i/columnCount;
                int columnIndex = i%columnCount;
                childViewTop = t+rowIndex*childViewHeight;
                childViewLeft = l+columnIndex*childViewWidth;
                childrenView.layout(childViewLeft,childViewTop,childViewLeft+childViewWidth,childViewTop+childViewHeight);
            }
            layoutChildrenView(childrenView);
        }
    }

    //为View中的子View再布局
    private void layoutChildrenView(RelativeLayout layout){
        Log.d("GroupVideoActivity","layoutChildrenView LinearLayout");
        if (layout.getChildCount() > 0 ) {
            for(int viewIndex=0;viewIndex<layout.getChildCount();viewIndex++) {
                View view = layout.getChildAt(viewIndex);
                view.layout(0, 0, layout.getWidth(), layout.getHeight());
            }
        }
        else{
            Log.d("GroupVideoActivity","没有子View layout wodth:"+layout.getWidth()+" height:"+layout.getHeight());
        }
    }

    /**
     * 获得 View
     * 保证了 View的重用
     *
     * @param position 位置
     */
    public RelativeLayout getView(final int position){
        if(position < mViewList.size()){
            return mViewList.get(position);
        }
        return null;
    }

    /**
     * 设置宫格参数
     *
     * @param imagesSize 图片数量
     * @return 宫格参数 gridParam[0] 宫格行数 gridParam[1] 宫格列数
     */
    protected static int[] calculateGridParam(int imagesSize){
        int[] gridParam = new int[2];
        if(imagesSize < 3){
            gridParam[0] = 1;
            gridParam[1] = imagesSize;
        }else if(imagesSize <= 4){
            gridParam[0] = 2;
            gridParam[1] = 2;
        }else{
            gridParam[0] = imagesSize/3 + (imagesSize % 3 == 0?0:1);
            gridParam[1] = 3;
        }
        return gridParam;
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(NineGridImageViewAdapter adapter){
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

    public int addLayout(){
        if(mViewList.size()>=mMaxSize){
            return -1;
        }
        RelativeLayout layout = mAdapter.generateView(getContext());
        mViewList.add(layout);
        addView(layout,generateDefaultLayoutParams());
        return mViewList.size()-1;
    }

    public void removeLayout(int position){
        if((position>=0)&&(position<mViewList.size())) {
            RelativeLayout layout = mViewList.remove(position);
            removeView(layout);
        }
    }

    public void removeLayout(RelativeLayout layout){
        if(layout!=null){
            ListIterator<RelativeLayout> listIterator = mViewList.listIterator();
            boolean hasLayout = false;
            while (listIterator.hasNext()){
                RelativeLayout childViewLayout = listIterator.next();
                if(childViewLayout.equals(layout)){
                    listIterator.remove();
                    removeView(layout);
                    hasLayout = true;
                    break;
                }
            }

            if(hasLayout==true){
                Log.d(TAG,"removeLayout success!!");
            }
            else{
                Log.d(TAG,"removeLayout failed, can not find childLayout!!");
            }
        }
        else{
            Log.d(TAG,"removeLayout failed, layout is null!!");
        }
    }

    public int getChildViewCount(){
        return mViewList.size();
    }

    public void notifyDataSetChanged(){
        int num = mViewList.size();
        int[] gridParam = calculateGridParam(num);
        mRowCount = gridParam[0];
        mColumnCount = gridParam[1];

        requestLayout();
    }
}
