package com.channey.timerbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.channey.utils.SharedPreferencesUtils;

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
    private static final int DOING = 0;
    private static final int DONE = 1;
    private boolean mAutoCounting = true;   //进入页面自动未完成的倒计时
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean mIsCounting = false;
    private CountingListener mCountListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOING:
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
                        mCountListener.onCountingFinish();
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
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.TimerButton);
        mAutoCounting = array.getBoolean(R.styleable.TimerButton_autoCounting,true);
        mCountingDownStart = array.getInteger(R.styleable.TimerButton_countingStart,COUNT_DOWN_DEFAULT);
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
                mHandler.sendEmptyMessage(DOING);
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
        void onCountingFinish();
    }

    public void setOnCountingListener(CountingListener listener) {
        this.mCountListener = listener;
    }

    public void setButtonClickable(boolean clickable) {
        setEnabled(clickable);
        setClickable(clickable);
    }

    public void saveTick(long tick){
        SharedPreferencesUtils.INSTANCE.saveLong(mContext,String.valueOf(getId()),tick);
//        Log.d(TAG,"saveTick key : "+String.valueOf(getId())+", tick："+tick);
    }

    public long pickTick(){
        long aLong = SharedPreferencesUtils.INSTANCE.getLong(mContext, String.valueOf(getId()));
//        Log.d(TAG,"pickTick key : "+String.valueOf(getId())+",tick："+aLong);
        return aLong;
    }

    public void clearTick(){
        SharedPreferencesUtils.INSTANCE.remove(mContext,String.valueOf(getId()));
    }
}
