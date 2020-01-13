package com.github.esabook.idcardsubmission;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TakeSelfieWithCardActivity extends AppCompatActivity {

    CameraPreviewSurface mPreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_selfie_with_card);

        mPreview = findViewById(R.id.camera);
        mPreview.startPreview();
    }

}
