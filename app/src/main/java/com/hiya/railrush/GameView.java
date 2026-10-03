package com.hiya.railrush;

import android.content.Context;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public final class GameView extends View {
    private final GameEngine engine;
    private long lastFrameMs;
    private float downX;
    private float downY;

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        engine = new GameEngine(context);
        lastFrameMs = System.currentTimeMillis();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = System.currentTimeMillis();
        float dt = Math.min(0.045f, Math.max(0.001f, (now - lastFrameMs) / 1000f));
        lastFrameMs = now;

        engine.update(dt);
        engine.draw(canvas, getWidth(), getHeight());

        postInvalidateDelayed(16);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (engine.handleKey(keyCode)) {
            invalidate();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 40) {
                engine.handleSwipe(dx > 0 ? 1 : -1);
            } else if (Math.abs(dy) > 40) {
                engine.handleSwipe(dy < 0 ? 2 : 3);
            } else {
                engine.handleTap(event.getX(), event.getY(), getWidth(), getHeight());
            }
            invalidate();
            return true;
        }
        return true;
    }
}
