package com.github.esabook.idcardsubmission.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.github.esabook.idcardsubmission.R;
import com.github.esabook.idcardsubmission.analyzer.AnalyzerTaskListener;
import com.github.esabook.idcardsubmission.analyzer.TextAnalyzer;
import com.github.esabook.idcardsubmission.view.CameraPreviewSurface;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

public class TakeCardActivity extends AppCompatActivity {
    public static final String TAG = TakeCardActivity.class.getSimpleName();

    CameraPreviewSurface mPreview;

    public int WIDTH_CROP_PERCENT = 8;
    public int HEIGHT_CROP_PERCENT = 74;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_card);
        mPreview = findViewById(R.id.camera);
        mPreview.startPreview();


        MutableLiveData<String> result = new MutableLiveData<>();
        final TextAnalyzer textAnalyzer = new TextAnalyzer(
                result,
                HEIGHT_CROP_PERCENT,
                WIDTH_CROP_PERCENT);

        textAnalyzer.setTaskListener(new AnalyzerTaskListener<FirebaseVisionText>() {
            @Override
            public void successed(FirebaseVisionText var1) {
                Log.i(TAG, "DETEXTED TEXT: " + var1.getText());
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


        mPreview.getAnalyzer().add(textAnalyzer);


    }

}
