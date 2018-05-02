package com.x.wallet.ui.fingerprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.x.wallet.R;

import java.util.ArrayList;

public class PwdView extends View {

    private ArrayList<String> result;
    private int count;
    private int size;
    private Paint mBorderPaint;
    private Paint mDotPaint;

    private boolean isFirstTime = true;

    private InputCallBack inputCallBack;
    //private InputMethodView inputMethodView;

    public PwdView(Context context) {
        super(context);
        init(null);
    }

    public PwdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PwdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        final float dp = getResources().getDisplayMetrics().density;
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        result = new ArrayList<>();
        int color = getResources().getColor(R.color.gray_aa);

        count = 6;

        size = (int) (dp * 30);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStrokeWidth(3);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(color);

        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setStrokeWidth(3);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = measureWidth(widthMeasureSpec);
        int h = measureHeight(heightMeasureSpec);
        int wsize = MeasureSpec.getSize(widthMeasureSpec);
        int hsize = MeasureSpec.getSize(heightMeasureSpec);

        if (w == -1) {
            if (h != -1) {
                w = h * count;
                size = h;
            } else {
                w = size * count;
                h = size;
            }
        } else {
            if (h == -1) {
                h = w / count;
                size = h;
            }
        }
        setMeasuredDimension(Math.min(w, wsize), Math.min(h, hsize));
    }

    private int measureWidth(int widthMeasureSpec) {
        int wmode = MeasureSpec.getMode(widthMeasureSpec);
        int wsize = MeasureSpec.getSize(widthMeasureSpec);
        if (wmode == MeasureSpec.AT_MOST) {
            return -1;
        }
        return wsize;
    }

    private int measureHeight(int heightMeasureSpec) {
        int hmode = MeasureSpec.getMode(heightMeasureSpec);
        int hsize = MeasureSpec.getSize(heightMeasureSpec);
        if (hmode == MeasureSpec.AT_MOST) {
            return -1;
        }
        return hsize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth() - 2;
        final int height = getHeight() - 2;

        int dotRadius = size / 6;
        for (int i = 0; i < 6; i++) {
            final float x = (float) (size * (i + 0.5));
            final float y = size / 2;
            canvas.drawCircle(x, y, dotRadius, mBorderPaint);
        }

        for (int i = 0; i < result.size(); i++) {
            final float x = (float) (size * (i + 0.5));
            final float y = size / 2;
            canvas.drawCircle(x, y, dotRadius, mDotPaint);
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new MyInputConnection(this, false);
    }

    public void setInputCallBack(InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
    }

    private void clearResult() {
        result.clear();
        isFirstTime = !isFirstTime;
        invalidate();
    }


    private class MyInputConnection extends BaseInputConnection {
        public MyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    public void setInputMethodView(InputMethodView inputMethodView) {
        //this.inputMethodView = inputMethodView;
        inputMethodView.setInputReceiver(new InputMethodView.InputReceiver() {
            @Override
            public void receive(String num) {
                if (num.equals("-1")) {
                    if (!result.isEmpty()) {
                        result.remove(result.size() - 1);
                        invalidate();
                    }
                } else {
                    if (result.size() < count) {
                        result.add(num);
                        invalidate();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ensureFinishInput();
                            }
                        },100);
                    }
                }


            }
        });
    }

    private void ensureFinishInput() {
        if (result.size() == count && inputCallBack != null) {
            StringBuffer sb = new StringBuffer();
            for (String i : result) {
                sb.append(i);
            }
            inputCallBack.onInputFinish(sb.toString());

            clearResult();
        }
    }

    public interface InputCallBack {
        void onInputFinish(String result);
    }
}
