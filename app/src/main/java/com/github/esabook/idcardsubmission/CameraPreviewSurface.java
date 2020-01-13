package com.github.esabook.idcardsubmission;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import static com.github.esabook.idcardsubmission.CameraUtils.getCameraInstance;

public class CameraPreviewSurface extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = CameraPreviewSurface.class.getSimpleName();


    private int CAMERA_ORIENTATION = 90;
    private SurfaceHolder mHolder;
    private Camera mCamera;


    public CameraPreviewSurface(Context context) {
        this(context, null);

    }

    public CameraPreviewSurface(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreviewSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public void startPreview() {
        if (CameraUtils.checkCameraHardware(this.getContext())) {
            mCamera = getCameraInstance();

            if (mCamera != null) {
                initCameraConfig();

                // The Surface has been created, now tell the camera where to draw the preview.
                try {
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.setDisplayOrientation(CAMERA_ORIENTATION);
                    mCamera.startPreview();

                } catch (IOException e) {
                    Log.d(TAG, "Error setting camera preview: " + e.getMessage());
                }
            }
        }
    }

    void initCameraConfig() {
        Camera.Parameters cp = mCamera.getParameters();
        cp.setRotation(CAMERA_ORIENTATION);
        cp.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCamera.setParameters(cp);
        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {

                Log.d(TAG, "Face total: " + String.valueOf(faces.length));

                for (Camera.Face f : faces) {
                    Log.d(TAG, "Face score: " + String.valueOf(f.score));
                }
            }
        });
    }

    //region Surface Holder


    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.startFaceDetection();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    //endregion
}