package com.channey.timerbutton

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.Log
import java.util.*

/**
 * Created by channey on 2017/12/16.
 */
class TimerButton : AppCompatTextView {
    private var mContext: Context
    private var mTick: Long = 0
    private var mCountingDownStart: Long = 0  //初始倒计时时间
    private var mAutoCounting = true //进入页面自动未完成的倒计时
    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null
    open var isCounting = false
    private var mCountListener: CountingListener? = null
    private var countingTextColor = 0
    private var defaultTextColor = 0
    private var countingBackground = 0
    private var defaultBackground: Drawable? = null
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                COUNTING -> {
                    if (mAutoCounting) saveTick(mTick)
                    setButtonClickable(false)
                    isCounting = true
                    if (mCountListener != null) {
                        mCountListener!!.onCounting(mTick)
                    }
                }
                DONE -> {
                    Log.d(TAG, "counting down finished.")
                    clearTick()
                    setButtonClickable(true)
                    isCounting = false
                    if (mCountListener != null) {
                        mCountListener!!.onFinished()
                    }
                    stopTimer()
                    mTick = mCountingDownStart
                }
            }
            super.handleMessage(msg)
        }
    }

    constructor(context: Context) : super(context) {
        mContext = context
        initParams()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        initParams()
        initAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        initParams()
        initAttrs(attrs)
    }

    private fun initParams() {}
    private fun initAttrs(attrs: AttributeSet) {
        defaultTextColor = currentTextColor
        countingTextColor = defaultTextColor
        defaultBackground = background
        val array = mContext.obtainStyledAttributes(attrs, R.styleable.TimerButton)
        mAutoCounting = array.getBoolean(R.styleable.TimerButton_TimerButton_autoCounting, true)
        mCountingDownStart = array.getInteger(R.styleable.TimerButton_TimerButton_from, COUNT_DOWN_DEFAULT).toLong()
        countingTextColor = array.getColor(R.styleable.TimerButton_TimerButton_countingTextColor, defaultTextColor)
        countingBackground = array.getResourceId(R.styleable.TimerButton_TimerButton_countingBackground, -1)
        mTick = mCountingDownStart
        Log.d(TAG, "init attrs auto counting : $mAutoCounting, is counting:$isCounting")
        if (mAutoCounting) {
            val lastSavedTick = pickTick()
            var timeLeft = getTimeLeft()
            if (timeLeft < lastSavedTick) {
                mTick = lastSavedTick - timeLeft
                Log.d(TAG, "timeLeft: $timeLeft, lastSavedTick:$lastSavedTick , current tick:$mTick")
                start()
            }
        } else {
            setButtonClickable(true)
        }
    }

    private fun taskSchedule() {
        mTimer = Timer()
        mTimerTask = object : TimerTask() {
            override fun run() {
                mTick -= 1000
                mHandler.sendEmptyMessage(COUNTING)
                if (mTick / 1000 <= 0L) {
                    mTimer!!.cancel()
                    mHandler.sendEmptyMessage(DONE)
                }
            }
        }
        mTimer!!.schedule(mTimerTask, 1, 1000)
    }

    /**
     * 倒计时开始
     *
     */
    fun start() {
        Log.d(TAG, "timer start.")
        stopTimer()
        taskSchedule()
    }

    private fun getTimeLeft():Long{
        var t = System.currentTimeMillis()
        var detachTime = SharedPreferencesUtils.getLong(mContext,KEY_TIME_LEFT)
        if (detachTime <= 0) detachTime = t
        return t - detachTime
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTimer()
        Log.d(TAG,"on detached from window : $id")
        SharedPreferencesUtils.saveLong(mContext,KEY_TIME_LEFT, System.currentTimeMillis())
    }

    /**
     * 结束倒计时
     */
    private fun stopTimer() {
        Log.d(TAG, "timer stop.")
        if (mTimerTask != null) {
            mTimerTask!!.cancel()
            mTimerTask = null
        }
        if (mTimer != null) mTimer!!.cancel()
        mTimer = null
    }

    /**
     * 倒计时状态监听
     */
    interface CountingListener {
        /**
         * 倒计时ing
         */
        fun onCounting(time: Long)

        /**
         * 倒计时结束
         */
        fun onFinished()
    }

    fun setOnCountingListener(listener: CountingListener?) {
        mCountListener = listener
    }

    fun setButtonClickable(clickable: Boolean) {
        isClickable = clickable
        if (clickable) {
            setTextColor(defaultTextColor)
            background = defaultBackground
        } else {
            setTextColor(countingTextColor)
            if (countingBackground != -1) {
                setBackgroundResource(countingBackground)
            }
        }
    }

    fun saveTick(tick: Long) {
        SharedPreferencesUtils.saveLong(mContext, id.toString(), tick)
//        Log.d(TAG,"save tick key : $id ,tick：$tick");
    }

    private fun pickTick(): Long {
        var long = SharedPreferencesUtils.getLong(mContext, id.toString())
        Log.d(TAG,"pick tick key : $id ,tick：$long");
        return long
    }

    fun clearTick() {
        Log.d(TAG,"clear tick key : $id");
        SharedPreferencesUtils.remove(mContext, id.toString())
        SharedPreferencesUtils.remove(mContext, KEY_TIME_LEFT)
    }

    companion object {
        private const val TAG = "TimerButton"
        private const val KEY_TIME_LEFT = "time_left"
        private const val COUNT_DOWN_DEFAULT = 60 * 1000
        private const val COUNTING = 0
        private const val DONE = 1
    }
}