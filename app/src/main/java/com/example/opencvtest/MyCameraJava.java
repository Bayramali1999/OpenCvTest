package com.example.opencvtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.io.IOException;

public class MyCameraJava extends JavaCameraView implements Camera.PictureCallback {

    static {
//        System.loadLibrary("native-lib");
    }

    String mPictureFileName;
    ImageView imageView;

    public MyCameraJava(Context context, AttributeSet attrs) {
        super(context, attrs);


    }

    public void takePicture(final String filename, ImageView imageView) {
        this.mPictureFileName = filename;
        mCamera.setPreviewCallback(this);
        mCamera.takePicture(null, null, this);
        this.imageView = imageView;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        // The camera preview was automatically stopped. Start it again.
        Log.i("TAG_TAKE", "Saving a bitmap to file" + data.toString());
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        imageView.setImageBitmap(bitmap);
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            Log.i("TAG_TAKE", "Saving a bitmap to file" + fos.getChannel());

            fos.write(data);
            fos.close();

        } catch (IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
