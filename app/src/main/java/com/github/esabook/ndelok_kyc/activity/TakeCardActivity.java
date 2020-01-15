package com.github.esabook.ndelok_kyc.activity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.github.esabook.ndelok_kyc.R;
import com.github.esabook.ndelok_kyc.RegionSpec;
import com.github.esabook.ndelok_kyc.analyzer.AnalyzerTaskListener;
import com.github.esabook.ndelok_kyc.analyzer.TextAnalyzer;
import com.github.esabook.ndelok_kyc.view.CameraPreviewSurface;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

public class TakeCardActivity extends AppCompatActivity {
    public static final String TAG = TakeCardActivity.class.getSimpleName();

    private CameraPreviewSurface mPreview;
    private SurfaceView mOverlay;
    private SurfaceHolder mOverlayHolder;

    private RegionSpec mRegionSpec = new RegionSpec(8, 70, 0, 0);

    private MutableLiveData<Boolean> isCardDetected = new MutableLiveData<>();

    Observer<Boolean> detectorObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean aBoolean) {
            drawOverlay(mOverlayHolder);
        }
    };

    AnalyzerTaskListener<FirebaseVisionText> textAnalyzerListener = new AnalyzerTaskListener<FirebaseVisionText>() {
        @Override
        public void successed(FirebaseVisionText var1) {
            Log.i(TAG, "DETEXTED TEXT: " + var1.getText());
            isCardDetected.setValue(!var1.getText().isEmpty() && var1.getText().length() > 15);
        }

        @Override
        public void failed(Exception e) {
            Log.i(TAG, "FAILED THROW: " + e.getMessage());
            isCardDetected.setValue(false);
        }

        @Override
        public void completed() {
            Log.i(TAG, "COMPLETED");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_card);
        mOverlay = findViewById(R.id.overlay);
        mPreview = findViewById(R.id.camera);
        mPreview.startPreview();

        isCardDetected.setValue(false);
        isCardDetected.observe(this, detectorObserver);

        MutableLiveData<String> result = new MutableLiveData<>();
        TextAnalyzer textAnalyzer = new TextAnalyzer(result, mRegionSpec);
        textAnalyzer.setTaskListener(textAnalyzerListener);


        mPreview.getAnalyzer().add(textAnalyzer);

        initIndicatorOverlay();
    }

    void initIndicatorOverlay() {
        mOverlay.setZOrderOnTop(true);
        mOverlay.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mOverlay.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mOverlayHolder = holder;
                drawOverlay(mOverlayHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mOverlayHolder = null;
            }
        });
    }


    private void drawOverlay(SurfaceHolder holder) {
        if (mOverlayHolder == null) return;

        int strokeColor = isCardDetected.getValue() ? Color.GREEN : Color.RED;

        Canvas canvas = holder.lockCanvas();

        // clear previous canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // bg of outer region
        Paint bgPaint = new Paint();
        bgPaint.setAlpha(140);
        canvas.drawPaint(bgPaint);

        // bg of inner region
        Paint rectPaint = new Paint();
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setAntiAlias(true);
        rectPaint.setColor(Color.WHITE);

        // stroke outline
        Paint outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(strokeColor);
        outlinePaint.setAntiAlias(true);
        outlinePaint.setPathEffect(new DashPathEffect(new float[]{30, 5}, 0));
        outlinePaint.setStrokeWidth(2f);

        int surfaceWidth = holder.getSurfaceFrame().width();
        int surfaceHeight = holder.getSurfaceFrame().height();
        float cornerRadius = 20f;

        // Set rect centered in frame
        float rectTop = surfaceHeight * mRegionSpec.HEIGHT_CROP_PERCENT / 2 / 100f;
        float rectLeft = surfaceWidth * mRegionSpec.WIDTH_CROP_PERCENT / 2 / 100f;
        float rectRight = surfaceWidth * (1 - mRegionSpec.WIDTH_CROP_PERCENT / 2 / 100f);
        float rectBottom = surfaceHeight * (1 - mRegionSpec.HEIGHT_CROP_PERCENT / 2 / 100f);

        // add vertical offset
        float VOffset = surfaceHeight / 2 * mRegionSpec.VERTICAL_OFFSET_PERCENT / 100f;
        rectTop += VOffset;
        rectBottom += VOffset;

        RectF rect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, rectPaint);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, outlinePaint);

        holder.unlockCanvasAndPost(canvas);
    }

}
