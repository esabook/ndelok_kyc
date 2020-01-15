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
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.github.esabook.ndelok_kyc.R;
import com.github.esabook.ndelok_kyc.RegionSpec;
import com.github.esabook.ndelok_kyc.analyzer.AnalyzerTaskListener;
import com.github.esabook.ndelok_kyc.analyzer.FaceAnalyzer;
import com.github.esabook.ndelok_kyc.analyzer.TextAnalyzer;
import com.github.esabook.ndelok_kyc.view.CameraPreviewSurface;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.List;

public class TakeSelfieWithCardActivity extends AppCompatActivity {

    public static final String TAG = TakeSelfieWithCardActivity.class.getSimpleName();

    CameraPreviewSurface mPreview;
    private SurfaceView mOverlay;
    private SurfaceHolder mOverlayHolder;
    private Button mSwitchCamera;
//    private ImageView mMarker;

    private MutableLiveData<Boolean> isCardDetected = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFaceDetected = new MutableLiveData<>();
    private MutableLiveData<String> textResult = new MutableLiveData<>();

    RegionSpec mFaceMarkerRegion = new RegionSpec(40, 60, -20, 0);
    RegionSpec mCardMarkerRegion = new RegionSpec(60, 85, 40, 0);

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
            drawOverlay(mOverlayHolder);
        }
    };


    AnalyzerTaskListener<List<FirebaseVisionFace>> faceAnalyzerListener = new AnalyzerTaskListener<List<FirebaseVisionFace>>() {
        @Override
        public void successed(List<FirebaseVisionFace> var1) {
            Log.i(TAG, "DETECTED FACE: " + var1.size());
            isFaceDetected.setValue(!var1.isEmpty());
        }

        @Override
        public void failed(Exception e) {
            Log.i(TAG, "FAILED THROW: " + e.getMessage());
            isFaceDetected.setValue(false);
        }

        @Override
        public void completed() {
            Log.i(TAG, "COMPLETED");
//                mMarker.getDrawable().setColorFilter(
//                        isFaceDetected ? Color.GREEN : Color.RED,
//                        PorterDuff.Mode.SRC_ATOP);
        }
    };

    View.OnClickListener clickSwitchButtonCameraMode = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.switchCameraMode();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_selfie_with_card);
        mOverlay = findViewById(R.id.overlay);
        mPreview = findViewById(R.id.camera);
        mSwitchCamera = findViewById(R.id.switch_camera);

        mSwitchCamera.setOnClickListener(clickSwitchButtonCameraMode);

        mPreview.startPreview();

        isCardDetected.setValue(false);
        isFaceDetected.setValue(false);
        isCardDetected.observe(this, detectorObserver);
        isFaceDetected.observe(this, detectorObserver);

        final TextAnalyzer textAnalyzer = new TextAnalyzer(textResult, mCardMarkerRegion);
        textAnalyzer.setTaskListener(textAnalyzerListener);


        FaceAnalyzer faceAnalyzer = new FaceAnalyzer(mFaceMarkerRegion);
        faceAnalyzer.setTaskListener(faceAnalyzerListener);

        mPreview.getAnalyzer().add(faceAnalyzer);
        mPreview.getAnalyzer().add(textAnalyzer);

        initIndicatorOverlay();
    }

    void initIndicatorOverlay() {
//        mOverlay.setZOrderOnTop(true);
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

        int cardStrokeColor = isCardDetected.getValue() ? Color.GREEN : Color.RED;
        int faceStrokeColor = isFaceDetected.getValue() ? Color.GREEN : Color.RED;

        Canvas canvas = holder.lockCanvas();

        // clear previous canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // bg of outer region
        Paint bgPaint = new Paint();
        bgPaint.setAlpha(140);
        canvas.drawPaint(bgPaint);

        // CARD: bg of inner region
        Paint rectPaint = new Paint();
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setAntiAlias(true);
        rectPaint.setColor(Color.WHITE);

        // CARD: stroke outline
        Paint outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(cardStrokeColor);
        outlinePaint.setAntiAlias(true);
        outlinePaint.setPathEffect(new DashPathEffect(new float[]{30, 5}, 0));
        outlinePaint.setStrokeWidth(2f);

        int surfaceWidth = holder.getSurfaceFrame().width();
        int surfaceHeight = holder.getSurfaceFrame().height();
        float cornerRadius = 20f;

        // CARD: Set rect centered in frame
        float rectTop = surfaceHeight * mCardMarkerRegion.HEIGHT_CROP_PERCENT / 2 / 100f;
        float rectLeft = surfaceWidth * mCardMarkerRegion.WIDTH_CROP_PERCENT / 2 / 100f;
        float rectRight = surfaceWidth * (1 - mCardMarkerRegion.WIDTH_CROP_PERCENT / 2 / 100f);
        float rectBottom = surfaceHeight * (1 - mCardMarkerRegion.HEIGHT_CROP_PERCENT / 2 / 100f);


        // FACE: add vertical offset
        float VOffset = surfaceHeight / 2 * mCardMarkerRegion.VERTICAL_OFFSET_PERCENT / 100f;
        rectTop += VOffset;
        rectBottom += VOffset;

        RectF rect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, rectPaint);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, outlinePaint);


        // FACE: Set rect centered in frame
        rectTop = surfaceHeight * mFaceMarkerRegion.HEIGHT_CROP_PERCENT / 2 / 100f;
        rectLeft = surfaceWidth * mFaceMarkerRegion.WIDTH_CROP_PERCENT / 2 / 100f;
        rectRight = surfaceWidth * (1 - mFaceMarkerRegion.WIDTH_CROP_PERCENT / 2 / 100f);
        rectBottom = surfaceHeight * (1 - mFaceMarkerRegion.HEIGHT_CROP_PERCENT / 2 / 100f);

        // FACE: add vertical offset
        VOffset = surfaceHeight / 2 * mFaceMarkerRegion.VERTICAL_OFFSET_PERCENT / 100f;
        rectTop += VOffset;
        rectBottom += VOffset;

        outlinePaint.setColor(faceStrokeColor);
        RectF rectFace = new RectF(rectLeft, rectTop, rectRight, rectBottom);
        canvas.drawOval(rectFace, rectPaint);
        canvas.drawOval(rectFace, outlinePaint);


        holder.unlockCanvasAndPost(canvas);
    }

}
