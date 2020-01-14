package com.github.esabook.idcardsubmission.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.esabook.idcardsubmission.R;
import com.github.esabook.idcardsubmission.analyzer.AnalyzerTaskListener;
import com.github.esabook.idcardsubmission.analyzer.FaceAnalyzer;
import com.github.esabook.idcardsubmission.view.CameraPreviewSurface;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.util.List;

public class TakeSelfieWithCardActivity extends AppCompatActivity {

    public static final String TAG = TakeSelfieWithCardActivity.class.getSimpleName();

    CameraPreviewSurface mPreview;


    public int WIDTH_CROP_PERCENT_FACE = 8;
    public int HEIGHT_CROP_PERCENT_FACE = 74;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_selfie_with_card);

        mPreview = findViewById(R.id.camera);
        mPreview.startPreview();

        FaceAnalyzer faceAnalyzer = new FaceAnalyzer(
                HEIGHT_CROP_PERCENT_FACE,
                WIDTH_CROP_PERCENT_FACE);

        faceAnalyzer.setTaskListener(new AnalyzerTaskListener<List<FirebaseVisionFace>>() {
            @Override
            public void successed(List<FirebaseVisionFace> var1) {

                Log.i(TAG, "DETECTED FACE: " + var1.size());
            }

            @Override
            public void failed(Exception e) {
                Log.i(TAG, "FAILED THROW: " + e.getMessage());

            }

            @Override
            public void completed() {
                Log.i(TAG, "COMPLETED");
            }
        });

        mPreview.getAnalyzer().add(faceAnalyzer);
    }

}
