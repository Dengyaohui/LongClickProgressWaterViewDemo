package com.example.longclickprogresswaterview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class WaterProgressView extends View {


    private Context mContext;
    private Paint mBgPaint; // 背景的画笔
    private Paint mProgressBgPaint; // 完整进度背景的画笔
    private Paint mProgressPaint; // 进度的画笔
    private RectF mRectangleRectF;  //背景范围矩形
    private RectF mProgressRectangleRectF;  //进度（水漫）范围矩形
    private int mViewWidth;//当前View的宽度
    private int mViewHeight;//当前View的高度
    private int mBgColor;  //背景颜色
    private float mProgress;  //进度
    private int mTargetProgress = 100;  //最大进度
    private int mProgressColor;  //进度颜色

    private Bitmap mBitmap;  //完整进度的bitmap对象

    private Handler mHandler;
    private Runnable mRunnable;  //长按动作的计时器
    private Runnable mCancelRunnable;  //取消动作的计时器
    private static final int FINISH_TIME = 1000;

    private WaterProgressView.OnLongClickStateListener mOnLongClickStateListener;


    public void setOnLongClickStateListener(WaterProgressView.OnLongClickStateListener onLongClickStateListener) {
        this.mOnLongClickStateListener = onLongClickStateListener;
    }


    public WaterProgressView(Context context) {
        this(context,null);
    }

    public WaterProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WaterProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getAttrValue(attrs);
        init();
    }

    private void getAttrValue(AttributeSet attrs) {
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.WaterProgressView);
        mBgColor = ta.getColor(R.styleable.WaterProgressView_background_color, Color.parseColor("#EE191C"));
        mProgressColor = ta.getColor(R.styleable.WaterProgressView_progress_color, Color.parseColor("#FF5263"));
        ta.recycle();
    }

    private void init() {
        //初始化画带圆角矩形的画笔
        mBgPaint = new Paint();
        mBgPaint.setColor(mBgColor);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setDither(true);

        //初始化画进度的paint
        mProgressPaint = new Paint();
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setDither(true);
        mProgressPaint.setStyle(Paint.Style.FILL);

        //初始化画完整进度的paint
        mProgressBgPaint = new Paint();
        mProgressBgPaint.setColor(mProgressColor);
        mProgressBgPaint.setAntiAlias(true);
        mProgressBgPaint.setDither(true);
        mProgressBgPaint.setStyle(Paint.Style.FILL);

        //显示背景的矩形范围
        mRectangleRectF = new RectF();
        //显示进度的矩形范围
        mProgressRectangleRectF = new RectF();

        mHandler = new Handler();
        //环形进度条自动增加逻辑
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mProgress += 1;
                setProgress(mProgress);
                //更新进度的接口回调
                if (mOnLongClickStateListener != null){
                    mOnLongClickStateListener.onProgress(mProgress);
                }
                if (mProgress < mTargetProgress){
                    mHandler.postDelayed(this, 1);
                }else {
                    //当环形进度条达到100，取消循环，进度置零，调用接口的完成回调
                    mProgress = 0;
                    if (mOnLongClickStateListener != null){
                        mOnLongClickStateListener.onFinish();
                    }
                }
            }
        };

        //取消动作的逻辑
        mCancelRunnable = new Runnable() {
            @Override
            public void run() {
                setProgress(mProgress);
                //当进度为0时，取消循环
                if (mProgress <= 0){
                    return;
                }else if (mProgress < 10){
                    //当进度降低到较低状态时，减缓降低的速度，每次减2
                    mProgress -= 2;
                }else {
                    //进度较高时，进度条减少的速度加快，每次减7
                    mProgress -= 7;
                }

                if (mProgress > 0){
                    mHandler.postDelayed(this, FINISH_TIME / 100);
                }else {
                    //当环形进度条达到0，再次手动置零，调用接口的取消回调，并返回进度回调参数
                    mProgress = 0;
                    if (mOnLongClickStateListener != null){
                        mOnLongClickStateListener.onCancel();
                    }
                }
                //更新进度的接口回调
                if (mOnLongClickStateListener != null){
                    mOnLongClickStateListener.onProgress(mProgress);
                }
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画背景圆角矩形
        canvas.drawRoundRect(mRectangleRectF, mViewWidth / 2, mViewWidth / 2, mBgPaint);
        //进度圆角矩形，只画出完整进度的（mProgress / mTargetProgress）部分，主需要控制改画出的部分的四个顶点坐标，即可单独画出顶点内的部分
        canvas.drawRect(0, mViewHeight * (1 - mProgress / mTargetProgress), mViewWidth, mViewHeight, mProgressPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        //背景的矩形
        mRectangleRectF.set(0, 0, mViewWidth, mViewHeight);
        //进度的矩形，因为防止计算数值时精度丢失，导致进度条面积小于实际背景面积，当水漫进度增加时，无法完全覆盖背景，所以范围增加1px
        mProgressRectangleRectF.set(-1, - 1, mViewWidth + 1, mViewHeight + 1);
        dealBitmap();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
//        Log.e("TAG", "onTouchEvent: " + mProgress );
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //当手指按下时，执行环形进度条增加Runnable，进度条开始增加
                mHandler.post(mRunnable);
                mHandler.removeCallbacks(mCancelRunnable);
                Log.e("TAG", "onTouchEvent: ACTION_DOWN");
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //当手指松开时，执行环形进度条减少Runnable，进度条开始减少
                mHandler.removeCallbacks(mRunnable);
                mHandler.post(mCancelRunnable);
                Log.e("TAG", "onTouchEvent: ACTION_UP");
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return false;
    }

    //处理Bitmap
    private void dealBitmap(){
        mBitmap = Bitmap.createBitmap(mViewWidth,
                mViewHeight , Bitmap.Config.ARGB_8888);
        //把完整进度画进bitmap
        Canvas canvas = new Canvas(mBitmap);
        canvas.drawRoundRect(mProgressRectangleRectF, mViewWidth / 2, mViewWidth / 2, mProgressBgPaint);
        mProgressPaint.setShader(new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
    }

    /**
     * 设置背景颜色
     * @param
     */
    public void setBgColor(int bgColor) {
        this.mBgColor = bgColor;
        mBgPaint.setColor(mBgColor);
        invalidate();
    }

    /**
     * 设置进度颜色
     * @param progressColor 进度颜色
     */
    public void setProgressColor(int progressColor) {
        this.mProgressColor = progressColor;
        //设置进度未满时的画笔颜色
        mProgressPaint.setColor(mProgressColor);
        //设置进度满时完整的画笔颜色
        mProgressBgPaint.setColor(mProgressColor);
        invalidate();
    }

    /**
     * 设置进度
     * @param mProgress（1-100f)
     */
    public void setProgress(float mProgress) {
        if(mProgress < 0){
            mProgress = 0;
        } else if(mProgress > 100){
            mProgress = 100;
        }
        this.mProgress = mProgress;
        invalidate();
    }

    /**
     * 长按完成和取消的接口
     */
    public interface OnLongClickStateListener {
        void onFinish();
        void onProgress(float progress);
        void onCancel();
    }
}
