package com.hxm.watchview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hxm on 2018/8/22
 * 描述：
 */
public class WatchView extends View {
    private Context context;
    private Paint paint;
    private Paint textPaint;//数字
    private float halfWidth;
    private float halfHeight;
    private int radius;//表盘半径
    private int shortLine;//短刻度
    private int longLine;//长刻度
    private int numToLine;//数字到长刻度线的距离
    private float secondDegree;//秒针旋转角度
    private float minuteDegree;//分钟旋转角度
    private float hourDegree;//时针旋转角度
    private boolean isNight;//24小时制
    private Rect textRect;
    private int sleepTime = 1000;
    private Timer timer = new Timer();

    public WatchView(Context context) {
        this(context, null);
    }

    public WatchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // TODO: 2018/8/23 wrap_content
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfWidth = getWidth() / 2;
        halfHeight = getHeight() / 2;
    }

    private void init() {
        shortLine = dp2px(context, 15);
        longLine = dp2px(context, 30);
        numToLine = dp2px(context, 14);
        paint = new Paint();
        paint.setColor(Color.parseColor("#000000"));
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(dp2px(context, 20));
        textRect = new Rect();
        radius = dp2px(context, 140);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        canvas.translate(halfWidth, halfHeight);
        canvas.drawCircle(0, 0, dp2px(context, 4), paint);
        for (int i = 1; i <= 12; i++) {
            if (i == 1 || i == 4 || i == 7 || i == 10) {
                paint.setStrokeWidth(8);
                canvas.drawLine(radius - longLine, 0, radius, 0, paint);
            } else {
                paint.setStrokeWidth(6);
                canvas.drawLine(radius - shortLine, 0, radius, 0, paint);
            }
            canvas.rotate(30);
        }
        for (int i = 1; i <= 4; i++) {
            drawNum(canvas, i * 90, i * 3 + "", textPaint);
        }
        drawSecondLine(canvas);
        drawMinuteLine(canvas);
        drawHourLine(canvas);
    }

    private void drawSecondLine(Canvas canvas) {
        canvas.save();
        paint.setStrokeWidth(4);
        canvas.rotate(secondDegree);
        canvas.drawLine(0, 40, 0, numToLine + dp2px(context, 10) + longLine - radius, paint);
        canvas.restore();
    }

    private void drawMinuteLine(Canvas canvas) {
        canvas.save();
        paint.setStrokeWidth(8);
        canvas.rotate(minuteDegree);
        canvas.drawLine(0, 50, 0, numToLine + dp2px(context, 10) + longLine - radius, paint);
        canvas.restore();
    }

    private void drawHourLine(Canvas canvas) {
        canvas.save();
        paint.setStrokeWidth(10);
        canvas.rotate(hourDegree);
        canvas.drawLine(0, 40, 0, numToLine + dp2px(context, 30) + longLine - radius, paint);
        canvas.restore();
    }

    //画刻度值
    private void drawNum(Canvas canvas, int degree, String text, Paint paint) {
        paint.getTextBounds(text, 0, text.length(), textRect);
        canvas.rotate(degree);
        canvas.translate(0, numToLine - radius + longLine);
        canvas.rotate(-degree);
        canvas.drawText(text, -textRect.width() / 2,
                textRect.height() / 2, paint);
        canvas.rotate(degree);
        canvas.translate(0, radius - numToLine - longLine);
        canvas.rotate(-degree);
    }

    public void setTime(long millionSeconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = dateFormat.format(new Date(millionSeconds));
        String[] t = time.split(":");
        int h = Integer.parseInt(t[0]);
        int m = Integer.parseInt(t[1]);
        int s = Integer.parseInt(t[2]);
        setTime(h, m, s);
    }

    public void setTime(int hour, int min, int second) {
        if (hour >= 24 || hour < 0 || min >= 60 || min < 0 || second >= 60 || second < 0) {
            return;
        }
        if (hour >= 12) {//这里我们采用24小时制
            isNight = true;//添加一个变量，用于记录是否为下午。
            hourDegree = (hour + min * 1.0f / 60f + second * 1.0f / 3600f - 12) * 30f;
        } else {
            isNight = false;
            hourDegree = (hour + min * 1.0f / 60f + second * 1.0f / 3600f) * 30f;
        }
        minuteDegree = (min + second * 1.0f / 60f) * 6f;
        secondDegree = second * 6f;
        invalidate();
        timer.schedule(task, 0, sleepTime);
    }

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (secondDegree == 360) {
                secondDegree = 0;
            }
            if (minuteDegree == 360) {
                minuteDegree = 0;
            }
            if (hourDegree == 360) {
                hourDegree = 0;
                isNight = !isNight;
            }
            secondDegree = secondDegree + 0.3f * sleepTime / 50;
            minuteDegree = minuteDegree + 0.005f * sleepTime / 50;
            hourDegree = hourDegree + 1.0f * sleepTime / 50 / 2400.0f;
            postInvalidate();
        }
    };

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
