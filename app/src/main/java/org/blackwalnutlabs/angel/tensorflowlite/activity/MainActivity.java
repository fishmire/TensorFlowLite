package org.blackwalnutlabs.angel.tensorflowlite.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.blackwalnutlabs.angel.tensorflowlite.R;
import org.blackwalnutlabs.angel.tensorflowlite.model.TensorFlowLiteDetector;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.blackwalnutlabs.angel.tensorflowlite.util.PermissionUtils;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.blackwalnutlabs.angel.tensorflowlite.setting.ImageSetting.MAXHEIGHT;
import static org.blackwalnutlabs.angel.tensorflowlite.setting.ImageSetting.MAXWIDTH;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    /**
     * System
     */
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        initDebug();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            initCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
    }

    /*
     * Debug
     */
    private TextView displayResult;

    private void initDebug() {
        displayResult = findViewById(R.id.displayResult);
    }

    /*
     * Permission
     * */
    private void requestPermission() {
        PermissionUtils.requestMultiPermissions(this, mPermissionGrant);
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = requestCode -> {
        switch (requestCode) {
            case PermissionUtils.CODE_CAMERA:
                Toast.makeText(MainActivity.this, "Result Permission Grant CODE_CAMERA", Toast.LENGTH_SHORT).show();
                break;
            case PermissionUtils.CODE_READ_EXTERNAL_STORAGE:
                Toast.makeText(MainActivity.this, "Result Permission Grant CODE_READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                break;
            case PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE:
                Toast.makeText(MainActivity.this, "Result Permission Grant CODE_WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(MainActivity.this, "Result Permission Grant CODE_MULTI_PERMISSION", Toast.LENGTH_SHORT).show();
                break;
        }
    };

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
        initCamera();
    }

    /*
     * OpenCV
     * */

    private JavaCameraView openCvCameraView;
    private Mat tmpMat;
    private Mat zeroMat;
    private Mat currentMat;
    private Mat kernel;
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    private void initCamera() {
        openCvCameraView = findViewById(R.id.HelloOpenCvView);
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);
        openCvCameraView.setMaxFrameSize(MAXWIDTH, MAXHEIGHT);
        openCvCameraView.enableFpsMeter();
        openCvCameraView.enableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        tmpMat = new Mat();
        zeroMat = new Mat(new Size(width, height), CvType.CV_8U, new Scalar(0));
        currentMat = new Mat(new Size(width, height), CvType.CV_8U, new Scalar(0));
        kernel = Imgproc.getStructuringElement(MORPH_RECT, new Size(2, 2));
    }

    private void initModel() {
        Map<String, Object> othersMap = new HashMap<>();
        othersMap.put("activity", this);

        detector = new TensorFlowLiteDetector(othersMap);
    }

    @Override
    public void onCameraViewStopped() {
    }

    private TensorFlowLiteDetector detector;

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        currentMat.copyTo(tmpMat);
        Core.rectangle(tmpMat, new Point(MAXWIDTH / 2 - MAXHEIGHT / 2, 0), new Point(MAXWIDTH / 2 + MAXHEIGHT / 2, MAXHEIGHT), new Scalar(255), 3);
        return tmpMat;
    }

    private Handler displayHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            displayResult.setText(msg.getData().getString("Result"));
            super.handleMessage(msg);
        }
    };
}
