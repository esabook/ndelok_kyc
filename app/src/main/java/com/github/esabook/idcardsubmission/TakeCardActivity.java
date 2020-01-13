package com.github.esabook.idcardsubmission;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TakeCardActivity extends AppCompatActivity {
    public static final String TAG = TakeCardActivity.class.getSimpleName();

    CameraPreviewSurface mPreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_card);
        mPreview = findViewById(R.id.camera);
        mPreview.startPreview();
    }

}
