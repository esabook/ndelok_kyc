package com.github.esabook.idcardsubmission.analyzer;

import android.hardware.Camera;

import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

public abstract class AnalyzerBase<TResult> implements AnalyzerTaskListener<TResult> {


    AnalyzerTaskListener<TResult> taskListener;

    public void setTaskListener(AnalyzerTaskListener<TResult> analyzerTaskListener) {
        this.taskListener = analyzerTaskListener;
    }

    public AnalyzerTaskListener<TResult> getTaskListener() {
        if (this.taskListener == null) return this;
        return this.taskListener;
    }

    public void analyze(byte[] data, Camera camera, int degree) {
    }

    @Override
    public void successed(TResult var1) {

    }

    @Override
    public void failed(Exception e) {

    }

    @Override
    public void completed() {

    }

    /**
     * Helper function to associate image rotation values with Firebase Vision metadata constants.
     */
    Integer degreesToFirebaseRotation(int degrees) throws Exception {

        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new Exception("Rotation must be 0, 90, 180, or 270.");
        }
    }

}