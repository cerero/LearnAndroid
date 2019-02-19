package com.badlogic.androidgames.ch04androidbasics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;


public class SurfaceViewTest extends AppCompatActivity {
    class RenderView extends View {
        Paint paint;
        Typeface font;
        Rect bounds = new Rect();

        public RenderView(Context ctx) {
            super(ctx);
            paint = new Paint();
            font = Typeface.createFromAsset(ctx.getAssets(), "Amatic-Bold.ttf");
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawARGB(255, 0, 0, 0);
            paint.setColor(Color.YELLOW);
            paint.setTypeface(font);
            paint.setTextSize(28 + 50);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("This is a test!", canvas.getWidth() / 2, 100, paint);

            String text = "This is another test o_O"; paint.setColor(Color.WHITE);
            paint.setTextSize(18 + 50);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, canvas.getWidth() - bounds.width(), 140, paint);

            this.invalidate();
        }
    }

    FastRenderView renderView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(new RenderView(this));

        renderView = new FastRenderView(this);
        setContentView(renderView);
    }

    protected void onResume() {
        super.onResume();
        if (renderView != null)
            renderView.resume();
    }

    protected void onPause() {
        super.onPause();
        if (renderView != null)
            renderView.pause();
    }

    class FastRenderView extends SurfaceView implements Runnable {
        Random randon = new Random();
        Thread renderThread = null;
        SurfaceHolder holder;
        volatile boolean running = false;

        public FastRenderView(Context context) {
            super(context);
            holder = getHolder();
            this.setZOrderOnTop(true);
            this.getHolder().setFormat(PixelFormat.TRANSPARENT);
        }

        public void resume() {
            running = true;
            renderThread = new Thread(this);
            renderThread.start();
        }

        public void run() {
            while(running) {
                if(!holder.getSurface().isValid())
                    continue;

                Canvas canvas = holder.lockCanvas();
//                canvas.drawRGB(255, 0, 0);
                canvas.drawARGB(0, 255, 0, 0);
                holder.unlockCanvasAndPost(canvas);

            }
        }

        public void pause() {
            running = false;
            while(true) {
                try {
                    renderThread.join();
                    return;
                } catch (InterruptedException e) {
                    // retry
                }
            }
        }
    }
}

