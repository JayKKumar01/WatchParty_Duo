package com.github.jaykkumar01.watchparty_duo.custom_designs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

public class PeerFeedCustomTextureView extends FrameLayout {
    private final Path clipPath = new Path();
    private float cornerRadius;
    private float strokeWidth;

    public PeerFeedCustomTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        cornerRadius = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics()
        ));
        strokeWidth = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()
        ));
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clipPath.reset();
        float left = strokeWidth;
        float top = strokeWidth+0;
        float right = w - strokeWidth;
        float bottom = h - strokeWidth;
        clipPath.addRoundRect(new RectF(left, top, right, bottom),
                cornerRadius, cornerRadius, Path.Direction.CW);
        clipPath.close();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }
}
