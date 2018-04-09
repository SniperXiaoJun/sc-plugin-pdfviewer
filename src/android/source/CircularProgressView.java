/*
 * Copyright (c) CFCA 2016.
 */

package com.sc.pdfviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * circular progress view, used for waiting in loading
 *
 * Created by wufan on 16/1/11.
 */
public class CircularProgressView extends View {
    private static final float INDETERMINANT_MIN_SWEEP = 15f;

    private boolean isIndeterminate, autoStartAnimation;
    private float currentProgress, maxProgress, indeterminateSweep, indeterminateRotateOffset;
    private int thickness, color, animDuration, animSwoopDuration, animSyncDuration, animSteps;

    private float startAngle;
    private float actualProgress;
    private ValueAnimator startAngleRotate;
    private ValueAnimator progressAnimator;
    private AnimatorSet indeterminateAnimator;
    private float initialStartAngle;

    private List<Listener> listeners;
    private Paint paint;
    private int size = 0;
    private RectF bounds;

    public CircularProgressView(Context context) {
        super(context);
        init(null, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    protected void init(AttributeSet attrs, int defStyle) {
        listeners = new ArrayList<Listener>();

        initAttributes(attrs, defStyle);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        updatePaint();

        bounds = new RectF();
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        Context context = getContext();
        int[] array = MyResource.getSourceByName(context,"styleable","CircularProgressView");

        final TypedArray typedArray =
              getContext().obtainStyledAttributes(attrs, array, defStyle, 0);

        Resources resources = getResources();

        try {
            currentProgress = typedArray.getFloat(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_progress"),
                  resources.getInteger(MyResource.getIdByName(context,"integer","cpv_default_progress")));
            maxProgress = typedArray.getFloat(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_maxProgress"),
                  resources.getInteger(MyResource.getIdByName(context,"integer","cpv_default_max_progress")));
            thickness = typedArray.getDimensionPixelSize(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_thickness"),
                  resources.getDimensionPixelSize(MyResource.getIdByName(context,"dimen","cpv_default_thickness")));
            isIndeterminate = typedArray.getBoolean(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_indeterminate"),
                  resources.getBoolean(MyResource.getIdByName(context,"bool","cpv_default_is_indeterminate")));
            autoStartAnimation = typedArray.getBoolean(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_animAutostart"),
                  resources.getBoolean(MyResource.getIdByName(context,"bool","cpv_default_anim_autostart")));
            initialStartAngle = typedArray.getFloat(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_startAngle"),
                  resources.getInteger(MyResource.getIdByName(context,"integer","cpv_default_start_angle")));

            startAngle = initialStartAngle;

            int accentColor = resources.getIdentifier("colorAccent", "attr", getContext().getPackageName());
            if (typedArray.hasValue(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_color"))) { // If color explicitly provided
                color = typedArray.getColor(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_color"),
                      resources.getColor(MyResource.getIdByName(context,"color","cpv_default_color")));
            } else if (accentColor != 0) { // If using support library v7 accentColor
                TypedValue t = new TypedValue();
                getContext().getTheme().resolveAttribute(accentColor, t, true);
                color = t.data;
            } else if (Build.VERSION.SDK_INT
                  >= Build.VERSION_CODES.LOLLIPOP) { // If using native accentColor (SDK >= 21)
                TypedArray t = getContext().obtainStyledAttributes(new int[] { android.R.attr.colorAccent });
                color = t.getColor(0, resources.getColor(MyResource.getIdByName(context,"color","cpv_default_color")));
                t.recycle();
            } else {
                color = resources.getColor(MyResource.getIdByName(context,"color","cpv_default_color"));
            }

            animDuration = typedArray.getInteger(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_animDuration"),
                  resources.getInteger(MyResource.getIdByName(context,"integer","cpv_default_anim_duration")));
            animSwoopDuration = typedArray.getInteger(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_animSwoopDuration"),
                  resources.getInteger(MyResource.getIdByName(context,"integer","cpv_default_anim_swoop_duration")));
            animSyncDuration = typedArray.getInteger(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_animSyncDuration"),
                  resources.getInteger(MyResource.getIdByName(context,"integer","cpv_default_anim_sync_duration")));
            animSteps = typedArray.getInteger(MyResource.getIdByName(context,"styleable","CircularProgressView_cpv_animSteps"),
                  resources.getInteger(MyResource.getIdByName(context,"integer","cpv_default_anim_steps")));
        } finally {
            typedArray.recycle();
        }
    }

    public boolean isIndeterminate() {
        return isIndeterminate;
    }

    public void setIndeterminate(boolean isIndeterminate) {
        boolean old = this.isIndeterminate;
        boolean reset = this.isIndeterminate == isIndeterminate;
        this.isIndeterminate = isIndeterminate;
        if (reset) {
            resetAnimation();
        }
        if (old != isIndeterminate) {
            for (Listener listener : listeners) {
                listener.onModeChanged(isIndeterminate);
            }
        }
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        updatePaint();
        updateBounds();
        invalidate();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        updatePaint();
        invalidate();
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }

    public float getProgress() {
        return currentProgress;
    }

    public void setProgress(final float currentProgress) {
        this.currentProgress = currentProgress;

        if (!isIndeterminate) {
            if (progressAnimator != null && progressAnimator.isRunning()) {
                progressAnimator.cancel();
            }
            progressAnimator = ValueAnimator.ofFloat(actualProgress, currentProgress);
            progressAnimator.setDuration(animSyncDuration);
            progressAnimator.setInterpolator(new LinearInterpolator());
            progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    actualProgress = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            progressAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    for (Listener listener : listeners) {
                        listener.onProgressUpdateEnd(currentProgress);
                    }
                }
            });
            progressAnimator.start();
        }
        invalidate();
        for (Listener listener : listeners) {
            listener.onProgressUpdate(currentProgress);
        }
    }

    public void addListener(Listener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void startAnimation() {
        resetAnimation();
    }

    public void resetAnimation() {
        if (startAngleRotate != null && startAngleRotate.isRunning()) startAngleRotate.cancel();
        if (progressAnimator != null && progressAnimator.isRunning()) progressAnimator.cancel();
        if (indeterminateAnimator != null && indeterminateAnimator.isRunning()) {
            indeterminateAnimator.cancel();
        }

        if (!isIndeterminate) {
            startAngle = initialStartAngle;
            startAngleRotate = ValueAnimator.ofFloat(startAngle, startAngle + 360);
            startAngleRotate.setDuration(animSwoopDuration);
            startAngleRotate.setInterpolator(new DecelerateInterpolator(2));
            startAngleRotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    startAngle = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            startAngleRotate.start();

            actualProgress = 0f;
            progressAnimator = ValueAnimator.ofFloat(actualProgress, currentProgress);
            progressAnimator.setDuration(animSyncDuration);
            progressAnimator.setInterpolator(new LinearInterpolator());
            progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    actualProgress = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            progressAnimator.start();
        } else {
            indeterminateSweep = INDETERMINANT_MIN_SWEEP;
            indeterminateAnimator = new AnimatorSet();
            AnimatorSet prevSet = null, nextSet;
            for (int k = 0; k < animSteps; k++) {
                nextSet = createIndeterminateAnimator(k);
                AnimatorSet.Builder builder = indeterminateAnimator.play(nextSet);
                if (prevSet != null) builder.after(prevSet);
                prevSet = nextSet;
            }

            indeterminateAnimator.addListener(new AnimatorListenerAdapter() {
                boolean wasCanceled = false;

                @Override
                public void onAnimationCancel(Animator animation) {
                    wasCanceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!wasCanceled) resetAnimation();
                }
            });
            indeterminateAnimator.start();
            for (Listener listener : listeners) {
                listener.onAnimationReset();
            }
        }
    }

    private AnimatorSet createIndeterminateAnimator(float step) {
        final float maxSweep = 360f * (animSteps - 1) / animSteps + INDETERMINANT_MIN_SWEEP;
        final float start = -90f + step * (maxSweep - INDETERMINANT_MIN_SWEEP);

        // Extending the front of the arc
        ValueAnimator frontEndExtend = ValueAnimator.ofFloat(INDETERMINANT_MIN_SWEEP, maxSweep);
        frontEndExtend.setDuration(animDuration / animSteps / 2);
        frontEndExtend.setInterpolator(new DecelerateInterpolator(1));
        frontEndExtend.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indeterminateSweep = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });

        // Overall rotation
        ValueAnimator rotateAnimator1 = ValueAnimator.ofFloat(step * 720f / animSteps, (step + .5f) * 720f / animSteps);
        rotateAnimator1.setDuration(animDuration / animSteps / 2);
        rotateAnimator1.setInterpolator(new LinearInterpolator());
        rotateAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indeterminateRotateOffset = (Float) animation.getAnimatedValue();
            }
        });

        // Retracting the back end of the arc
        ValueAnimator backEndRetract = ValueAnimator.ofFloat(start, start + maxSweep - INDETERMINANT_MIN_SWEEP);
        backEndRetract.setDuration(animDuration / animSteps / 2);
        backEndRetract.setInterpolator(new DecelerateInterpolator(1));
        backEndRetract.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (Float) animation.getAnimatedValue();
                indeterminateSweep = maxSweep - startAngle + start;
                invalidate();
            }
        });

        // More overall rotation
        ValueAnimator rotateAnimator2 =
              ValueAnimator.ofFloat((step + .5f) * 720f / animSteps, (step + 1) * 720f / animSteps);
        rotateAnimator2.setDuration(animDuration / animSteps / 2);
        rotateAnimator2.setInterpolator(new LinearInterpolator());
        rotateAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indeterminateRotateOffset = (Float) animation.getAnimatedValue();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(frontEndExtend).with(rotateAnimator1);
        set.play(backEndRetract).with(rotateAnimator2).after(rotateAnimator1);
        return set;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int xPad = getPaddingLeft() + getPaddingRight();
        int yPad = getPaddingTop() + getPaddingBottom();
        int width = getMeasuredWidth() - xPad;
        int height = getMeasuredHeight() - yPad;
        size = (width < height) ? width : height;
        setMeasuredDimension(size + xPad, size + yPad);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        size = (w < h) ? w : h;
        updateBounds();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float sweepAngle = (isInEditMode()) ? currentProgress / maxProgress * 360 : actualProgress / maxProgress * 360;
        if (!isIndeterminate) {
            canvas.drawArc(bounds, startAngle, sweepAngle, false, paint);
        } else {
            canvas.drawArc(bounds, startAngle + indeterminateRotateOffset, indeterminateSweep, false, paint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (autoStartAnimation) startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (startAngleRotate != null) {
            startAngleRotate.cancel();
            startAngleRotate = null;
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
        if (indeterminateAnimator != null) {
            indeterminateAnimator.cancel();
            indeterminateAnimator = null;
        }
    }

    private void updatePaint() {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void updateBounds() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        bounds.set(paddingLeft + thickness, paddingTop + thickness, size - paddingLeft - thickness,
              size - paddingTop - thickness);
    }

    public interface Listener {
        /**
         * Called when setProgress is called (Determinate only)
         *
         * @param currentProgress The progress that was set.
         */
        void onProgressUpdate(float currentProgress);

        /**
         * Called when this view finishes animating to the updated progress. (Determinate only)
         *
         * @param currentProgress The progress that was set and this view has reached in its animation.
         */
        void onProgressUpdateEnd(float currentProgress);

        /**
         * Called when resetAnimation() is called
         */
        void onAnimationReset();

        /**
         * Called when you switch between indeterminate and determiante modes
         *
         * @param isIndeterminate true if mode was set to indeterminate, false otherwise.
         */
        void onModeChanged(boolean isIndeterminate);
    }

    /**
     * Use this if you don't want to implement all listeners
     */
    public static class Adapter implements Listener {
        @Override
        public void onProgressUpdate(float currentProgress) {

        }

        @Override
        public void onProgressUpdateEnd(float currentProgress) {

        }

        @Override
        public void onAnimationReset() {

        }

        @Override
        public void onModeChanged(boolean isIndeterminate) {

        }
    }
}
