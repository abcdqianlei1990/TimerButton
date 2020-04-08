package com.channey.timerbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by channey on 2017/12/16.
 */

public class TimerButton extends android.support.v7.widget.AppCompatTextView{
    private static final String TAG = "TimerButton";
    private static final int COUNT_DOWN_DEFAULT = 60 * 1000;
    private Context mContext;
    private long mTick;
    private long mCountingDownStart;    //初始倒计时时间
    private static final int COUNTING = 0;
    private static final int DONE = 1;
    private boolean mAutoCounting = true;   //进入页面自动未完成的倒计时
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean mIsCounting = false;
    private CountingListener mCountListener;
    private int countingTextColor;
    private int defaultTextColor;
    private int countingBackground;
    private Drawable defaultBackground;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COUNTING:
                    if (mAutoCounting) saveTick(mTick);
                    setButtonClickable(false);
                    mIsCounting = true;
                    if (mCountListener != null) {
                        mCountListener.onCounting(mTick);
                    }
                    break;
                case DONE:
                    clearTick();
                    setButtonClickable(true);
                    mIsCounting = false;
                    if (mCountListener != null) {
                        mCountListener.onFinished();
                    }
                    stop();
                    mTick = mCountingDownStart;
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public TimerButton(Context context) {
        super(context);
        mContext = context;
        initParams();
    }

    public TimerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initParams();
        initAttrs(attrs);
    }

    public TimerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initParams();
        initAttrs(attrs);
    }

    private void initParams() {

    }

    private void initAttrs(AttributeSet attrs) {
        defaultTextColor = getCurrentTextColor();
        countingTextColor = defaultTextColor;
        defaultBackground = getBackground();
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.TimerButton);
        mAutoCounting = array.getBoolean(R.styleable.TimerButton_TimerButton_autoCounting,true);
        mCountingDownStart = array.getInteger(R.styleable.TimerButton_TimerButton_from,COUNT_DOWN_DEFAULT);
        countingTextColor = array.getColor(R.styleable.TimerButton_TimerButton_countingTextColor,defaultTextColor);
        countingBackground = array.getResourceId(R.styleable.TimerButton_TimerButton_countingBackground,-1);
        mTick = mCountingDownStart;
        if (mAutoCounting){
            long tick = pickTick();
            if (tick > 0){
                mTick = tick;
                start();
            }
        }
    }

    private void taskSchedule() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mTick -= 1000;
                mHandler.sendEmptyMessage(COUNTING);
                if (mTick / 1000 == 0) {
                    mTimer.cancel();
                    mHandler.sendEmptyMessage(DONE);
                }
            }
        };
        mTimer.schedule(mTimerTask, 1, 1000);
    }


    /**
     * 倒计时开始
     *
     * */
    public void start() {
        stop();
        taskSchedule();
    }

    /**
     * 结束倒计时
     */
    private void stop() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null)
            mTimer.cancel();
        mTimer = null;
    }

    /**
     *
     * @return
     */
    public boolean isCounting() {
        return mIsCounting;
    }

    /**
     * 倒计时状态监听
     */
    public interface CountingListener {
        /**
         * 倒计时ing
         */
        void onCounting(long time);

        /**
         * 倒计时结束
         */
        void onFinished();
    }

    public void setOnCountingListener(CountingListener listener) {
        this.mCountListener = listener;
    }

    public void setButtonClickable(boolean clickable) {
        setClickable(clickable);
        if (clickable){
            setTextColor(defaultTextColor);
            setBackground(defaultBackground);
        }else{
            setTextColor(countingTextColor);
            if (countingBackground != -1){
                setBackgroundResource(countingBackground);
            }
        }
    }

    public void saveTick(long tick){
        SharedPreferencesUtils.saveLong(mContext,String.valueOf(getId()),tick);
//        Log.d(TAG,"saveTick key : "+String.valueOf(getId())+", tick："+tick);
    }

    public long pickTick(){
        long aLong = SharedPreferencesUtils.getLong(mContext, String.valueOf(getId()));
//        Log.d(TAG,"pickTick key : "+String.valueOf(getId())+",tick："+aLong);
        return aLong;
    }

    public void clearTick(){
        SharedPreferencesUtils.remove(mContext,String.valueOf(getId()));
    }
}
