package com.mnevent.hz.View;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.Utils.Log;

/**
 * Created by zyand on 2019/5/21.
 */

public class SimerTextview extends android.support.v7.widget.AppCompatTextView {

    private Paint paint;

    private int aint;

    private LinearGradient linearGradient;

    public SimerTextview(Context context) {
        super(context);
        init(context,null);
    }

    public SimerTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }



    public SimerTextview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayerType(LAYER_TYPE_SOFTWARE,null);
        paint = getPaint();
        int lite = (int) paint.measureText(getText().toString());
        stateAnimer(lite);
        createLinerGradient(lite);
    }

    private void createLinerGradient(int lite) {
        linearGradient = new LinearGradient(-lite,0,0,0,new int[]{getCurrentTextColor(),0xffff0000,0xff00ff00,0xff0000ff,0xff008800,getCurrentTextColor()},new float[]{0,0.2f,0.4f,0.6f,0.8f,1},Shader.TileMode.REPEAT);
    }

    private void stateAnimer(int lite) {
        ValueAnimator animator = ValueAnimator.ofInt(0,lite*2);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                aint = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {


        paint.setTypeface(MyApp.fromAsset);

        Matrix matrix = new Matrix();

        matrix.postTranslate(aint,0);
        linearGradient.setLocalMatrix(matrix);
        paint.setShader(linearGradient);
        super.onDraw(canvas);
    }
}
