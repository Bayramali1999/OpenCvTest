package com.example.opencvtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int CAM_REQ_CODE = 123;
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;

    Mat mRgba, intermediaMat, mGray, hierarchy;

    int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    List<MatOfPoint> contours;

    View view;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = findViewById(R.id.l_corner_view);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAM_REQ_CODE && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadCamera();
        } else {
            Toast.makeText(this, "You must to give permission for using this app", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCamera() {
        cameraBridgeViewBase = findViewById(R.id.cameraView);
        cameraBridgeViewBase.setVisibility(View.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.setCameraIndex(this.activeCamera);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }

            }
        };
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "There is some error", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width, height, CvType.CV_8UC4);
        mGray = new Mat(width, height, CvType.CV_8UC1);
        intermediaMat = new Mat(width, height, CvType.CV_8UC4);
        hierarchy = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        intermediaMat.release();
        mGray.release();
        hierarchy.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = new Mat();
        contours = new ArrayList<>();

        Imgproc.Canny(mRgba, intermediaMat, 100, 100);
        //Pointdagi 0 obyektni burchlari siljishi
        Imgproc.findContours(intermediaMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        hierarchy.release();

        for (int i = 0; i < contours.size(); i++) {

            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());

            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());


            //u need all of here
            Rect rect = Imgproc.boundingRect(points);
            double height = rect.height;
            double width = rect.width;
//            height > 300 && width > 300
            //height and width rendering rectangles size
            int x = rect.x;
            int y = rect.y;
//            view.setX(rect.x);
//            view.setY(rect.y);

            if (width>200 && height>200) {

                Log.d("TAG_CAM_FRAME", "rect.height" + rect.height);
                Log.d("TAG_CAM_FRAME", "rect.width" + rect.width);
                Log.d("TAG_CAM_FRAME", "rect.x" + rect.x);
                Log.d("TAG_CAM_FRAME", "rect.y" + rect.y);
                Log.d("TAG_CAM_FRAME", "rect.tl()" + rect.tl());
                Log.d("TAG_CAM_FRAME", "rect.area()" + rect.area());


                Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0, 0), 5);
//                Imgproc.putText(mRgba, "a", rect.tl(), Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2, new Scalar(0, 255, 0, 0), 4);
            }
        }
        return mRgba;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAM_REQ_CODE);
        } else {
            loadCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }
}