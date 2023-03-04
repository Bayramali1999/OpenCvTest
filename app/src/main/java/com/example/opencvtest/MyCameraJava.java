package com.example.opencvtest;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;

public class MyCameraJava extends JavaCameraView implements Camera.PictureCallback {

    String mPictureFileName;

    public MyCameraJava(Context context, AttributeSet attrs) {
        super(context, attrs);


    }

    public void takePicture(final String filename) {
        this.mPictureFileName = filename;
        mCamera.setPreviewCallback(this);
        mCamera.takePicture(null, null, this);

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);

            fos.write(data);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
