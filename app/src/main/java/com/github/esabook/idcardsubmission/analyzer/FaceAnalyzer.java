package com.github.esabook.idcardsubmission.analyzer;

import android.graphics.Bitmap;
import android.hardware.Camera;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

import java.util.List;

public class FaceAnalyzer extends AnalyzerBase<List<FirebaseVisionFace>> {

    public final String TAG = FaceAnalyzer.class.getSimpleName();

    int widthCropPercent;
    int heightCropPercent;

    private FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector();
    // Flag to skip analyzing new available frames until previous analysis has finished.
    private boolean isBusy = false;


    public FaceAnalyzer(int widthCropPercent,
                        int heightCropPercent) {
        this.widthCropPercent = widthCropPercent;
        this.heightCropPercent = heightCropPercent;

    }


    @Override
    public void analyze(byte[] data, Camera camera, int degree) {
        try {
            analyze(data, camera.getParameters().getPreviewSize(), degreesToFirebaseRotation(degree));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void analyze(byte[] mediaImage, Camera.Size previewSize, @FirebaseVisionImageMetadata.Rotation int degrees) {
        if (mediaImage != null && !isBusy) {
            isBusy = true;
            FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                    .setWidth(previewSize.width)   // 480x360 is typically sufficient for
                    .setHeight(previewSize.height)  // image recognition
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setRotation(degrees)
                    .build();
            Bitmap bitmap = FirebaseVisionImage.fromByteArray(mediaImage, metadata).getBitmap();
            int croppedWidth = (int) (bitmap.getWidth() * (1 - widthCropPercent / 100f));
            int croppedHeight = (int) (bitmap.getHeight() * (1 - heightCropPercent / 100f));
            int x = (bitmap.getWidth() - croppedWidth) / 2;
            int y = (bitmap.getHeight() - croppedHeight) / 2;
            Bitmap cropBmp = Bitmap.createBitmap(bitmap, x, y, croppedWidth, croppedHeight);

//            Log.d(TAG, String.format("Bitmap len: %s\nCropped W: %s\nCropped H: %s\nW-CW : 2: %s\nH-CH : 2: %s",
//                    bitmap.getByteCount(), croppedWidth, croppedHeight, x, y));

            recognizeTextOnDevice(FirebaseVisionImage.fromBitmap(cropBmp))
                    .addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionFace>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<FirebaseVisionFace>> task) {
                            isBusy = false;
                            getTaskListener().completed();
                        }
                    });
        }
    }

    private Task<List<FirebaseVisionFace>> recognizeTextOnDevice(FirebaseVisionImage image) {
        // Pass image to an ML Kit Vision API
        return detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        getTaskListener().successed(firebaseVisionFaces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getTaskListener().failed(e);
                    }
                });
    }

}

