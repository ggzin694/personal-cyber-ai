package com.ggzin694.personalcyberai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** Lightweight Vixer Eye indicator; local touch only, no sensors or monitoring. */
public class VixerEyeView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float targetX, targetY, eyeX, eyeY;

    public VixerEyeView(Context context) { super(context); setFocusable(false); }

    @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        eyeX = targetX = w / 2f; eyeY = targetY = h / 2f;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            targetX = event.getX(); targetY = event.getY(); invalidate(); return true;
        }
        return true;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        eyeX += (targetX - eyeX) * .12f; eyeY += (targetY - eyeY) * .12f;
        float dx = eyeX - cx, dy = eyeY - cy, distance = (float) Math.sqrt(dx * dx + dy * dy);
        float max = Math.min(getWidth(), getHeight()) * .22f;
        if (distance > max) { dx = dx / distance * max; dy = dy / distance * max; }
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(cx, cy, Math.min(getWidth(), getHeight()) * .48f,
                Color.argb(75, 67, 219, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, Math.min(getWidth(), getHeight()) * .48f, paint);
        paint.setShader(null); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(67, 219, 255)); canvas.drawCircle(cx, cy, Math.min(getWidth(), getHeight()) * .30f, paint);
        paint.setColor(Color.argb(120, 171, 108, 255)); paint.setStrokeWidth(1);
        canvas.drawCircle(cx, cy, Math.min(getWidth(), getHeight()) * .40f, paint);
        paint.setStyle(Paint.Style.FILL); paint.setColor(Color.rgb(239, 250, 255));
        canvas.drawCircle(cx + dx * .25f, cy + dy * .25f, Math.min(getWidth(), getHeight()) * .17f, paint);
        paint.setColor(Color.rgb(13, 29, 61));
        canvas.drawCircle(cx + dx * .40f, cy + dy * .40f, Math.min(getWidth(), getHeight()) * .09f, paint);
        postInvalidateDelayed(32);
    }
}

