package com.github.esabook.ndelok_kyc.analyzer;

import android.graphics.Bitmap;
import android.hardware.Camera;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.github.esabook.ndelok_kyc.RegionSpec;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class TextAnalyzer extends AnalyzerBase<FirebaseVisionText> {

    public final String TAG = TextAnalyzer.class.getSimpleName();

    MutableLiveData<String> mResult;
    RegionSpec mMarkerSpec;

    private FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    // Flag to skip analyzing new available frames until previous analysis has finished.
    private boolean isBusy = false;


    public TextAnalyzer(MutableLiveData<String> result, RegionSpec markerSpec) {
        this.mResult = result;
        this.mMarkerSpec = markerSpec;
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
            int croppedWidth = (int) (bitmap.getWidth() * (1 - mMarkerSpec.WIDTH_CROP_PERCENT / 100f));
            int croppedHeight = (int) (bitmap.getHeight() * (1 - mMarkerSpec.HEIGHT_CROP_PERCENT / 100f));
            int x = (bitmap.getWidth() - croppedWidth) / 2;
            int y = (bitmap.getHeight() - croppedHeight) / 2;

            // add vertical offset
            float VOffset = bitmap.getHeight() / 2 * mMarkerSpec.VERTICAL_OFFSET_PERCENT / 100f;
            y += VOffset;

            Bitmap cropBmp = Bitmap.createBitmap(bitmap, x, y, croppedWidth, croppedHeight);

//            Log.d(TAG, String.format("Bitmap len: %s\nCropped W: %s\nCropped H: %s\nW-CW : 2: %s\nH-CH : 2: %s",
//                    bitmap.getByteCount(), croppedWidth, croppedHeight, x, y));

            recognizeTextOnDevice(FirebaseVisionImage.fromBitmap(cropBmp))
                    .addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
                        @Override
                        public void onComplete(@NonNull Task<FirebaseVisionText> task) {
                            isBusy = false;
                            getTaskListener().completed();
                        }
                    });
        }
    }

    private Task<FirebaseVisionText> recognizeTextOnDevice(FirebaseVisionImage image) {
        // Pass image to an ML Kit Vision API
        return detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        mResult.setValue(firebaseVisionText.getText());
                        getTaskListener().successed(firebaseVisionText);

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

