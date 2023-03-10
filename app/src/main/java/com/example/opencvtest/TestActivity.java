package com.example.opencvtest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TestActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    MyCameraJava cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    //image holder
    Mat bwIMG, hsvIMG, lrrIMG, urrIMG, dsIMG, usIMG, cIMG, hovIMG;
    MatOfPoint2f approxCurve;
    int threshold;

    Button button;

    int PERMISSION_ALL = 1;

    String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,};
    int displayWidth;
    int displayHeight;
    int titleHeight;
    int titleWidth;
    int marginLengthOnDP;


    int cameraScreenHeight = 0;
    int cameraScreenWidth = 0;
//    ConstraintLayout layoutContainer;

    //    @SuppressLint("MissingInflatedId")
    View rTopView, lTopView, rBottomView, lBottomView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initView();
        setRectanglesPositionOnView();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MediaActionSound mediaActionSound = new MediaActionSound();
                mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);

                Date date = new Date();
                long currentTime = Calendar.getInstance().getTimeInMillis();
                String fileName = Environment.getExternalStorageDirectory().getPath() + "/sample_file_pic_" + currentTime + ".jpeg";

                cameraBridgeViewBase.takePicture(fileName, imageView);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.con, new BlankFragment()).commit();
//                imageView.get
            }
        });
    }

    private void setRectanglesPositionOnView() {
        double proportion = 268.0 / 213.0;

        titleHeight = (int) (displayWidth * proportion);
        titleWidth = displayWidth;

        marginLengthOnDP = (displayHeight - titleHeight) / 2;

        Resources resources = getResources();
        int marginLengthOnPX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginLengthOnDP, resources.getDisplayMetrics());


        ConstraintLayout.LayoutParams mLeftTop = (ConstraintLayout.LayoutParams) lTopView.getLayoutParams();
        mLeftTop.setMargins(0, marginLengthOnPX, 0, 0);
        lTopView.setLayoutParams(mLeftTop);
        ConstraintLayout.LayoutParams mRightTop = (ConstraintLayout.LayoutParams) rTopView.getLayoutParams();
        mRightTop.setMargins(0, marginLengthOnPX, 0, 0);
        rTopView.setLayoutParams(mRightTop);

        ConstraintLayout.LayoutParams mRightBottom = (ConstraintLayout.LayoutParams) rBottomView.getLayoutParams();
        mRightBottom.setMargins(0, 0, 0, marginLengthOnPX);
        rBottomView.setLayoutParams(mRightBottom);

        ConstraintLayout.LayoutParams mLeftBottom = (ConstraintLayout.LayoutParams) lBottomView.getLayoutParams();
        mLeftBottom.setMargins(0, 0, 0, marginLengthOnPX);
        lBottomView.setLayoutParams(mLeftBottom);
    }

    private void initView() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        displayWidth = (int) (displayMetrics.widthPixels / displayMetrics.density);
        displayHeight = (int) (displayMetrics.heightPixels / displayMetrics.density);

        imageView = findViewById(R.id.iv);
        rTopView = findViewById(R.id.r_top);
        lTopView = findViewById(R.id.l_top);
        lBottomView = findViewById(R.id.l_bottom);
        rBottomView = findViewById(R.id.r_bottom);
        button = findViewById(R.id.took);
        cameraBridgeViewBase = findViewById(R.id.cameraViewer);
        cameraBridgeViewBase.setMaxFrameSize(640, 480);


        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
//TODO TRESHOLD CHANGE SIZE
        threshold = 10;
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        bwIMG = new Mat();
                        dsIMG = new Mat();
                        hsvIMG = new Mat();
                        lrrIMG = new Mat();
                        urrIMG = new Mat();
                        usIMG = new Mat();
                        cIMG = new Mat();
                        hovIMG = new Mat();
                        approxCurve = new MatOfPoint2f();
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

    }

    private boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ALL && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        cameraScreenHeight = width;
        cameraScreenWidth = height;

        Log.d("TAG_CAMERA_EW", "onCameraViewStarted: " + width + " height: " + height);
        Log.d("TAG_CAMERA_EW", "onCameraViewStarted: " + titleWidth + " height: " + titleHeight);
    }

    @Override
    public void onCameraViewStopped() {

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat gray = inputFrame.gray();
        Mat dst = inputFrame.rgba();

        Imgproc.pyrDown(gray, dsIMG, new Size(gray.cols() / 2.0, gray.rows() / 2.0));
        Imgproc.pyrUp(dsIMG, usIMG, gray.size());

        Imgproc.Canny(usIMG, bwIMG, 0, threshold);

        Imgproc.dilate(bwIMG, bwIMG, new Mat(), new Point(-1, 1), 1);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        cIMG = bwIMG.clone();

        Imgproc.findContours(cIMG, contours, hovIMG, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        rTopView.setBackgroundResource(R.drawable.rectangel_red);
        lTopView.setBackgroundResource(R.drawable.rectangel_red);
        rBottomView.setBackgroundResource(R.drawable.rectangel_red);
        lBottomView.setBackgroundResource(R.drawable.rectangel_red);
        for (MatOfPoint cnt : contours) {

            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());

            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);

            int numberVertices = (int) approxCurve.total();

            double contourArea = Imgproc.contourArea(cnt);
// TODO rendering rectangle size
//            if (Math.abs(contourArea) < 2 && Math.abs(contourArea) > 50) {
//                continue;
//            }

            //Rectangle detected
            if (numberVertices >= 4 && numberVertices <= 6) {

                List<Double> cos = new ArrayList<>();

                for (int j = 2; j < numberVertices + 1; j++) {
                    cos.add(angle(approxCurve.toArray()[j % numberVertices], approxCurve.toArray()[j - 2], approxCurve.toArray()[j - 1]));
                }

                Collections.sort(cos);

                double mincos = cos.get(0);
                double maxcos = cos.get(cos.size() - 1);

                if (numberVertices == 4 && mincos >= -0.1 && maxcos <= 0.3) {
                    //
                    setLabel(dst, "X", cnt);
                }

            }


        }

        return dst;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    private static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    private void setLabel(Mat im, String label, MatOfPoint contour) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 3;//0.4;
        int thickness = 3;//1;
        int[] baseline = new int[1];
        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(contour);

        Log.d("TAG_RECT", "setLabel: x = " + r.width + " Y = " + r.height);
        Point pt = new Point(r.x + ((r.width - text.width) / 2), r.y + ((r.height + text.height) / 2));
        //todo you can check view with x and y
        Log.d("TAG_DRAW_X", "setLabel: " + r.x + " " + r.y);

        int marginByTitleAndCareRendering = Math.abs((cameraScreenHeight - titleHeight) / 2);

        if ((r.x >= marginByTitleAndCareRendering && r.x < marginByTitleAndCareRendering + 40) && (r.y >= 0 && r.y <= 80)) {
            Imgproc.putText(im, label, pt, fontface, scale, new Scalar(255, 0, 0), thickness);
            rTopView.setBackgroundResource(R.drawable.rectangel_gree);
        }
        if ((r.x >= marginByTitleAndCareRendering && r.x < marginByTitleAndCareRendering + 40) && (r.y >= titleWidth && r.y <= titleWidth + 80)) {
            Imgproc.putText(im, label, pt, fontface, scale, new Scalar(255, 0, 0), thickness);
            lTopView.setBackgroundResource(R.drawable.rectangel_gree);
        }
        int heightScreen = cameraScreenHeight - marginByTitleAndCareRendering;
        if ((r.x >= heightScreen - 40 && r.x < heightScreen) && (r.y >= titleWidth && r.y <= titleWidth + 80)) {
            Imgproc.putText(im, label, pt, fontface, scale, new Scalar(255, 0, 0), thickness);
            lBottomView.setBackgroundResource(R.drawable.rectangel_gree);
        }

        if ((r.x >= heightScreen - 40 && r.x < heightScreen) && (r.y >= 0 && r.y <= 80)) {
            Imgproc.putText(im, label, pt, fontface, scale, new Scalar(255, 0, 0), thickness);
            rBottomView.setBackgroundResource(R.drawable.rectangel_gree);
        }

    }
}