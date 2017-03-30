/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bit.facetracker.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bit.facetracker.AndroidApplication;
import bit.facetracker.R;
import bit.facetracker.job.FaceDetectorJob;
import bit.facetracker.model.FaceDetectResult;
import bit.facetracker.model.FaceModel;
import bit.facetracker.tools.Blur;
import bit.facetracker.tools.LogUtils;
import bit.facetracker.tools.ToastUtils;
import bit.facetracker.ui.camera.CameraSourcePreview;
import bit.facetracker.ui.camera.GraphicOverlay;
import bit.facetracker.ui.widget.CustomDraweeView;
import bit.facetracker.ui.widget.ProgressView;
import bit.facetracker.ui.widget.TextContainer;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivityMultiNew extends BaseActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_EXTERNAL = 3;

    private int mScreenWidth = 1080;

    public volatile  boolean mIsGetBitmap;
    public volatile  boolean mIsGetDetectResult;


    private volatile Face mFace;
    private volatile Frame mFrame;
    private volatile Frame mFrameToBlur;
    private static final int MAXOFFSET_X = 5;
    private static final int MAXOFFSET_Y = 5;
    private static volatile String CAPTUREPATHDIR = "/sdcard/pics/";
    private static volatile String CAPTURECROPIMGPATH = "";
    private static volatile String CAPTUREIMGPATH = "";
    private static final int MAXSHOTCOUNT = 15;


    private Handler mHandler;
    private static final Integer HANDLER_RENDER_BLURBACKGROUND = 1;

    public Map<Integer, FaceContainer> mDetectedFaces = new HashMap<>();

    public Face mCurrentGotFace = null;



    // new
    @BindView(R.id.background_animation)
    LottieAnimationView animationView;

    @BindView(R.id.background_animation2)
    LottieAnimationView animationView2;

    // hash key
    private static final float TIME_DISPLAYATTATIVE = 200;
    private static final float TIME_DISPLAYAGE = 267;
    private static final float TIME_DISPLAYCHARM = 333;
    private static final float TIME_MATCHSTARIMAGE = 1000;
    private static final float TIME_MATCHSTARNAME = 1100;
    private static final float TIME_QUITFIRST = 5033;


    private static final float TIME_IMAGEMAIN = 1533;
    private static final float TIME_IMAGESIDE1 = 1666;
    private static final float TIME_IMAGESIDE2 = 1800;
    private static final float TIME_IMAGESIDE3 = 1933;
    private static final float TIME_IMAGESIDE4 = 2067;


    private Map<Float, Boolean> mIsMarked = new HashMap<>();
    private Map<Float, Boolean> mImageMarked = new HashMap<>();

    private boolean mIsQuitAnimation;

    // attractive
    @BindView(R.id.attractive_progressview)
    ProgressView mAttractiveProgressView;
    @BindView(R.id.attractive_textcontainer)
    TextContainer mAttractiveTextContainer;

    // age
    @BindView(R.id.age_progressview)
    ProgressView mAgeProgressView;
    @BindView(R.id.age_textcontainer)
    TextContainer mAgeTextContainer;

    // charm
    @BindView(R.id.charm_progressview)
    ProgressView mCharmProgressView;
    @BindView(R.id.charm_textcontainer)
    TextContainer mCharmTextContainer;


    @BindView(R.id.self_avatar)
    CustomDraweeView mSelfAvatar;
    @BindView(R.id.match_star)
    CustomDraweeView mStarView;

    @BindView(R.id.matchstart_textcontainer)
    TextContainer mMatchStartTextContainer;


    // second result
    @BindView(R.id.image_main)
    CustomDraweeView mMainImage;

    @BindView(R.id.image_side1)
    CustomDraweeView mSide1Image;

    @BindView(R.id.image_side2)
    CustomDraweeView mSide2Image;

    @BindView(R.id.image_side3)
    CustomDraweeView mSide3Image;

    @BindView(R.id.image_side4)
    CustomDraweeView mSide4Image;

    CountDownTimer mTimer;
    @BindView(R.id.time)
    TextView mTime;


    @BindView(R.id.first_result)
    RelativeLayout mFirstResult;
    @BindView(R.id.second_result)
    RelativeLayout mSecondResult;

    @BindView(R.id.blur_background)
    FrameLayout mBlurBackground;

    Blur mBlurTools;

    volatile BitmapDrawable mBackgroundDrawable;

    AnimatorSet mSelfAvatarSet;

    volatile FaceDetectResult mResult = null;

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_new);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);


        LogUtils.e("AndroidRuntime","dpi ="  + getResources().getDisplayMetrics().densityDpi);

        Display display = getWindowManager().getDefaultDisplay();
        mScreenWidth = display.getWidth();
        mPreview.setScreenWidth(mScreenWidth);
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int ext = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (rc == PackageManager.PERMISSION_GRANTED && ext == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        EventBus.getDefault().register(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    if(mIsGetBitmap) {
                        LogUtils.d("Count", "restart set");

                    }
                } else if (msg.what == HANDLER_RENDER_BLURBACKGROUND) {
                    mBlurBackground.setBackground(mBackgroundDrawable);
                }
            }
        };

        ButterKnife.bind(this);

        mBlurTools = new Blur(this, mBlurBackground,5.0f,25f);

        init();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        animationView.playAnimation();

        animationView.addAnimatorUpdateListener((animation -> {

            LogUtils.d("animator", "time = " + animation.getCurrentPlayTime());
            if (animation.getCurrentPlayTime() >= TIME_DISPLAYATTATIVE && !mIsMarked.get(TIME_DISPLAYATTATIVE)) {
                mIsMarked.put(TIME_DISPLAYATTATIVE, true);
                mAttractiveProgressView.setMaxProgress(0.7f);
                mAttractiveTextContainer.setDisplayText(getString(R.string.label_displayattractive));
                mSelfAvatar.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490460448295&di=8c0cabacc4d5fa33ca680fe6f02d5a08&imgtype=0&src=http%3A%2F%2Fhiphotos.baidu.com%2Ftcoh%2Fpic%2Fitem%2Fffa39b25a7fbf33f908f9d7f.jpg%3Fv%3Dtbs");
            }

            if (animation.getCurrentPlayTime() >= TIME_DISPLAYAGE && !mIsMarked.get(TIME_DISPLAYAGE)) {
                mIsMarked.put(TIME_DISPLAYAGE, true);
                mAgeProgressView.setMaxProgress(0.5f);
                mAgeTextContainer.setDisplayText(getString(R.string.label_displayage));
                mStarView.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490461006594&di=1fee390ec7b8cf488a0e0623455a0f8e&imgtype=0&src=http%3A%2F%2Fa.hiphotos.baidu.com%2Fzhidao%2Fpic%2Fitem%2F342ac65c103853438b3c5f8b9613b07ecb8088ad.jpg");
            }

            if (animation.getCurrentPlayTime() >= TIME_DISPLAYCHARM && !mIsMarked.get(TIME_DISPLAYCHARM)) {
                mIsMarked.put(TIME_DISPLAYCHARM, true);
                mCharmProgressView.setMaxProgress(0.2f);
                mCharmTextContainer.setDisplayText(getString(R.string.label_displaycharm));
            }

            if (animation.getCurrentPlayTime() >= TIME_MATCHSTARIMAGE && !mIsMarked.get(TIME_MATCHSTARIMAGE)) {
                mIsMarked.put(TIME_MATCHSTARIMAGE, true);
                mStarView.setVisibility(View.VISIBLE);
                mStarView.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490524083796&di=03286777e18a55b3e39e4a66dd8f0992&imgtype=0&src=http%3A%2F%2Fscimg.jb51.net%2Ftouxiang%2F201703%2F2017032517032229.jpg");
            }


            if (animation.getCurrentPlayTime() >= TIME_MATCHSTARNAME && !mIsMarked.get(TIME_MATCHSTARNAME)) {
                mIsMarked.put(TIME_MATCHSTARNAME, true);
                mMatchStartTextContainer.setDisplayText("撞脸明星 方大同");
            }


            if (animation.getCurrentPlayTime() >= TIME_QUITFIRST && !mIsMarked.get(TIME_QUITFIRST)) {
                mIsMarked.put(TIME_QUITFIRST, true);
                firstQuit();
            }


        }));




        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mStarView.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490461341161&di=f3fdb65d26887946104e893855c3bcb6&imgtype=0&src=http%3A%2F%2Fcdnq.duitang.com%2Fuploads%2Fitem%2F201412%2F30%2F20141230085749_tr24F.jpeg");

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animationView2.addAnimatorUpdateListener(animation ->  {

            if (animation.getCurrentPlayTime() >= TIME_IMAGEMAIN && !mImageMarked.get(TIME_IMAGEMAIN)) {
                mImageMarked.put(TIME_IMAGEMAIN, true);
                mMainImage.setVisibility(View.VISIBLE);
                mMainImage.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490512396376&di=bdbb723411b0ab542ae61f036cef315e&imgtype=0&src=http%3A%2F%2Fpic36.photophoto.cn%2F20150707%2F0047045135399298_b.jpg");
            }

            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE1 && !mImageMarked.get(TIME_IMAGESIDE1)) {
                mImageMarked.put(TIME_IMAGESIDE1, true);

                mSide1Image.setVisibility(View.VISIBLE);
                mSide1Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490520865259&di=116bf1d4c4673b755a46c440e079f345&imgtype=0&src=http%3A%2F%2Ffile06.16sucai.com%2F2016%2F0921%2Fda78bbfe5a27798a8d300f30d5ad594e.jpg");
            }

            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE2 && !mImageMarked.get(TIME_IMAGESIDE2)) {
                mImageMarked.put(TIME_IMAGESIDE2, true);
                mSide2Image.setVisibility(View.VISIBLE);
                mSide2Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490520885166&di=a09e54d037b48b201b5667118cf99981&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2015%2Fxll%2F05%2F8%2Fstar29.jpg");
            }

            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE3 && !mImageMarked.get(TIME_IMAGESIDE3)) {
                mImageMarked.put(TIME_IMAGESIDE3, true);
                mSide3Image.setVisibility(View.VISIBLE);
                mSide3Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1490510814&di=9f9ddb699f4c03feffeb03cb71d48efa&src=http://img02.tooopen.com/images/20150703/tooopen_sy_132761691991.jpg");
            }

            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE4 && !mImageMarked.get(TIME_IMAGESIDE4)) {
                mImageMarked.put(TIME_IMAGESIDE4, true);
                mSide4Image.setVisibility(View.VISIBLE);
                mSide4Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1490510837&di=a6c87767b4eb689df8396c57796d2111&src=http://5.66825.com/download/pic/000/326/d7b6e3f5f063dfbeec1635627988aa48.jpg");
            }

        });

        animationView2.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                init();
//                animationView.playAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        startTimer();
        LogUtils.d("MAC",getMacAddress());



//        startDisplay();

//        BlurBackgourndTask taskblur = new BlurBackgourndTask();
//        taskblur.start();

//        mBlurBackground.setBackgroundResource(R.drawable.blur_background);

    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_EXTERNAL);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(true)
                .build();

        MyFaceDetector myFaceDetector = new MyFaceDetector(detector);


        myFaceDetector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, myFaceDetector)
                .setRequestedPreviewSize(1280,720)
                .setAutoFocusEnabled(true)
                .setFacing(1)
                .setRequestedFps(30.0f)
                .build();

        Log.d("NavBar","result = " + hasNavBar(getResources()));

    }

    public boolean hasNavBar (Resources resources)
    {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    public int getNavigationbarHeight(Resources resources) {
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
//        getPreviewList();
        startCameraSource();
        mIsGetBitmap = false;
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        mTimer.cancel();
        mBlurTools.destroyTs();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }

    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */

    private FaceGraphicMove mFaceGraphic;

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {

            //TODO  split face
            mOverlay.clear();

            mFaceGraphic = new FaceGraphicMove(mOverlay);
            mFaceGraphic.setScanBodyCompleteListener(new FaceGraphicMove.ScanBodyCompleteListener() {
                @Override
                public void complete() {

                    mFaceGraphic.acclerateSpeed();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    startDisplay();
                }
            });


            FaceContainer container = new FaceContainer(item);
            mDetectedFaces.put(faceId, container);
            mFaceGraphic.setId(faceId);
            mOverlay.add(mFaceGraphic);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */

        long timestamp = System.currentTimeMillis();
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

            // Split Face
            timestamp = System.currentTimeMillis();

            if (!mIsGetBitmap) {

                float currentX  = (float)(face.getPosition().x + face.getWidth() / 2.0);
                float currentY  = (float)(face.getPosition().y + face.getHeight() / 2.0);

                float lastX = 0f;
                float lastY = 0f;

                mFace = mDetectedFaces.get(face.getId()).face;

                if (mFace != null) {
                    lastX = (float) (mFace.getPosition().x + mFace.getWidth() / 2.0);
                    lastY  = (float)(mFace.getPosition().y + mFace.getHeight() / 2.0);
                }

//                if(mFace != null) {
//                    LogUtils.d("Count","offsetx = " + Math.abs(mFace.getPosition().x - face.getPosition().x) + "offsetY = " + Math.abs(mFace.getPosition().y - face.getPosition().y));
//                }

                if (Math.abs(currentX - lastX) <= MAXOFFSET_X && Math.abs(currentY - lastY) <= MAXOFFSET_Y && !mIsGetBitmap && mFace != null) {
                    mDetectedFaces.get(face.getId()).count++;
                } else {
                    mDetectedFaces.get(face.getId()).count = 0;
                }

                // update face
                mDetectedFaces.get(face.getId()).face = face;

                if (mDetectedFaces.get(face.getId()).count >= MAXSHOTCOUNT) {

                    mCurrentGotFace = face;
                    mIsGetBitmap = true;
                    CAPTURECROPIMGPATH = CAPTUREPATHDIR + System.currentTimeMillis() + "_" + "capture.png";
                    CAPTUREIMGPATH = CAPTUREPATHDIR + System.currentTimeMillis() + "_" + "full" + "_" + "capture.png";
                    LogUtils.d("Count","got it==============================");

                    CropPreviewFrame capturetask = new CropPreviewFrame(CAPTURECROPIMGPATH,CAPTUREIMGPATH);
                    capturetask.execute(mFrame);

                }
            }

            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
            if (mIsGetBitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                    }
                });
            }

        }

    }

    public void getPreviewList() {
        Camera camera = Camera.open(0);
        final Camera.Parameters params = camera.getParameters();
        final List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        int length = sizes.size();
        for(int i  = 0;i < length;i++) {
           Log.d("PreviewList","height = " + sizes.get(i).height + " width = " + sizes.get(i).width);
        }
        camera.release();
    }

    public void detectorface(String path,String facilityId,String faceRect) {
        FaceDetectorJob job = new FaceDetectorJob(path,facilityId,faceRect);
        AndroidApplication.getInstance().getJobManager().addJob(job);
    }

    class MyFaceDetector extends Detector<Face> {
        private Detector<Face> mDelegate;

        MyFaceDetector(Detector<Face> delegate) {
            mDelegate = delegate;
        }

        public SparseArray<Face> detect(Frame frame) {
            // *** add your custom frame processing code here
            if (!mIsGetBitmap) {
                mFrame = frame;
            }

            mFrameToBlur = frame;
            return mDelegate.detect(frame);
        }

        public boolean isOperational() {
            return mDelegate.isOperational();
        }

        public boolean setFocus(int id) {
            return mDelegate.setFocus(id);
        }
    }

    class CropPreviewFrame extends AsyncTask<Frame,String,String> {

        String filecroppath;
        String filefullpath;

        public CropPreviewFrame(String filepath,String fullimagepath) {
            this.filecroppath = filepath;
            this.filefullpath  = fullimagepath;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(Frame... params) {
            Frame frame = params[0];
            File file = null;
            if (frame != null) {
                Bitmap bitmap = getBitmap(frame);
                if (bitmap != null) {

                    double x = Math.max(0,mFace.getPosition().x - 20);
                    double y = Math.max(0,mFace.getPosition().y - 20);

                    double w = mFace.getWidth() + 40;
                    double h = mFace.getHeight() + 40;

                    if (mFace.getPosition().x < 0) {
                        w += mFace.getPosition().x;
                    }

                    if ((w + x) > bitmap.getWidth()) {
                        w = bitmap.getWidth() - x;
                    }

                    if (mFace.getPosition().y < 0) {
                        h += mFace.getPosition().y;
                    }

                    if ((h + y) > bitmap.getHeight()) {
                        h = bitmap.getHeight() - y;
                    }

                    final  Bitmap disbitmap = Bitmap.createBitmap(bitmap,(int)x,(int)y,(int)w,(int)h);
                    File dir = new File(CAPTUREPATHDIR);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    file = new File(filecroppath);
                    try {
                        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                        disbitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    file = new File(filefullpath);
                    try {
                        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            return filefullpath;
        }

        @Override
        protected void onPostExecute(String file) {
            super.onPostExecute(file);
            detectorface(file, getMacAddress(),getFaceRect(mCurrentGotFace));
        }

        private Bitmap getBitmap(Frame frame) {
            ByteBuffer byteBuffer = frame.getGrayscaleImageData();
            byte[] bytes = byteBuffer.array();
            int w = frame.getMetadata().getWidth();
            int h = frame.getMetadata().getHeight();

            YuvImage yuvimage=new YuvImage(bytes, ImageFormat.NV21, w, h, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, w, h), 100, baos); // Where 100 is the quality of the generated jpeg
            byte[] jpegArray = baos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap disbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            return disbitmap;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FaceDetectResult result) {
//        if (result != null && result.code.equals("200") && mIsGetBitmap) {
//
//            if (result.result != null && result.result.face.face_num > 0) {
//                mResult = result;
//                mIsGetDetectResult = true;
//                mFaceGraphic.setScanBody(mIsGetDetectResult);
//            } else {
//                ToastUtils.showLong(this,"没毛病 ！ O(∩_∩)O ");
//            }
//
//        }else {
//            ToastUtils.showLong(this,"没毛病 ！ O(∩_∩)O ");
//        }

        // Test
        mIsGetDetectResult = true;
        mFaceGraphic.setScanBody(mIsGetDetectResult);
    }


    class FaceContainer {

        FaceContainer(Face face) {
            this.face = face;
        }

        Face face  = null;
        int count  = 0;
    }





    private void init() {
        mIsMarked.put(TIME_DISPLAYATTATIVE, false);
        mIsMarked.put(TIME_DISPLAYAGE, false);
        mIsMarked.put(TIME_DISPLAYCHARM, false);
        mIsMarked.put(TIME_MATCHSTARIMAGE, false);
        mIsMarked.put(TIME_MATCHSTARNAME, false);
        mIsMarked.put(TIME_QUITFIRST, false);

        mImageMarked.put(TIME_IMAGEMAIN, false);
        mImageMarked.put(TIME_IMAGESIDE1, false);
        mImageMarked.put(TIME_IMAGESIDE2, false);
        mImageMarked.put(TIME_IMAGESIDE3, false);
        mImageMarked.put(TIME_IMAGESIDE4, false);


        mSelfAvatar.setAlpha(1.0f);
        mAttractiveProgressView.setAlpha(1.0f);
        mAttractiveProgressView.setTextVisible(true);
        mAgeProgressView.setAlpha(1.0f);
        mAgeProgressView.setTextVisible(true);
        mCharmProgressView.setAlpha(1.0f);
        mCharmProgressView.setTextVisible(true);
        mAttractiveTextContainer.setVisibility(View.VISIBLE);
        mAgeTextContainer.setVisibility(View.VISIBLE);
        mCharmTextContainer.setVisibility(View.VISIBLE);
        mStarView.setAlpha(1.0f);
        mStarView.setVisibility(View.INVISIBLE);
        mMatchStartTextContainer.setVisibility(View.VISIBLE);
        mFirstResult.setVisibility(View.GONE);
        mSecondResult.setVisibility(View.GONE);
        mMainImage.setVisibility(View.INVISIBLE);
        mSide1Image.setVisibility(View.INVISIBLE);
        mSide2Image.setVisibility(View.INVISIBLE);
        mSide3Image.setVisibility(View.INVISIBLE);
        mSide4Image.setVisibility(View.INVISIBLE);
        animationView.setVisibility(View.VISIBLE);
        animationView2.setVisibility(View.GONE);
        mBlurBackground.setBackgroundColor(Color.TRANSPARENT);

        mIsGetBitmap = false;
        mIsGetDetectResult = false;
        mCurrentGotFace = null;
        mResult = null;
        if(mFaceGraphic != null)
        mFaceGraphic.resetSpeed();

    }



    public void createQuitAnimationSet() {
        ObjectAnimator selfAvatarAnimator = ObjectAnimator.ofFloat(mSelfAvatar,"alpha",1.0f,0.0f);
        selfAvatarAnimator.setDuration(333);
        ObjectAnimator matchStartAnimator = ObjectAnimator.ofFloat(mStarView,"alpha",1.0f,0.0f);
        matchStartAnimator.setDuration(333);
        ObjectAnimator attractiveprogressviewanimator = ObjectAnimator.ofFloat(mAttractiveProgressView,"alpha",1.0f,0.0f);
        attractiveprogressviewanimator.setDuration(200);
        ObjectAnimator ageprogressviewanimator = ObjectAnimator.ofFloat(mAgeProgressView,"alpha",1.0f,0.0f);
        ageprogressviewanimator.setDuration(200);
        ObjectAnimator charmprogressviewanimator = ObjectAnimator.ofFloat(mCharmProgressView,"alpha",1.0f,0.0f);
        charmprogressviewanimator.setDuration(200);
        List<Animator> animators = new ArrayList<>();
        animators.add(selfAvatarAnimator);
        animators.add(attractiveprogressviewanimator);
        animators.add(ageprogressviewanimator);
        animators.add(charmprogressviewanimator);
        animators.add(matchStartAnimator);
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animators);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFirstResult.setVisibility(View.GONE);
                mSecondResult.setVisibility(View.VISIBLE);
                animationView2.setVisibility(View.VISIBLE);
                animationView.setVisibility(View.GONE);
                animationView2.playAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    public void firstQuit() {
        mAttractiveTextContainer.setVisibility(View.INVISIBLE);
        mAgeTextContainer.setVisibility(View.INVISIBLE);
        mCharmTextContainer.setVisibility(View.INVISIBLE);
        mMatchStartTextContainer.setVisibility(View.INVISIBLE);

        mAttractiveProgressView.setTextVisible(false);
        mAgeProgressView.setTextVisible(false);
        mCharmProgressView.setTextVisible(false);

        createQuitAnimationSet();
    }


    public void startTimer() {
        mTimer = new CountDownTimer(1000000000, 60000) {

            public void onTick(long millisUntilFinished) {
                Calendar c = Calendar.getInstance();
                mTime.setText((c.get(Calendar.HOUR_OF_DAY) >= 10 ? c.get(Calendar.HOUR_OF_DAY) : "0" + c.get(Calendar.HOUR_OF_DAY))+":" + (c.get(Calendar.MINUTE) >= 10 ? c.get(Calendar.MINUTE) : "0" + c.get(Calendar.MINUTE)));
            }
            public void onFinish() {

            }
        };
        mTimer.start();
    }


    class BlurBackgourndTask extends Thread {

        @Override
        public void run() {
            super.run();
            int count = 0;
            while (!isInterrupted() && count <= 5) {

                if (mFrameToBlur != null) {
                    mBackgroundDrawable =  mBlurTools.blur(mFrameToBlur,count + 1,25f);
                    count++;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.removeMessages(HANDLER_RENDER_BLURBACKGROUND);
                mHandler.sendEmptyMessage(HANDLER_RENDER_BLURBACKGROUND);
            }
        }
    }

    // got result from server
    private void startDisplay() {

        mFirstResult.setVisibility(View.VISIBLE);
        mSelfAvatar.setImageURI("file://" + CAPTURECROPIMGPATH);
        BlurBackgourndTask blurBackgourndTask = new BlurBackgourndTask();
        blurBackgourndTask.start();
        TranslateAnimation transformation = new TranslateAnimation(Animation.ABSOLUTE,mCurrentGotFace.getPosition().x-288, Animation.ABSOLUTE,0, Animation.ABSOLUTE,mCurrentGotFace.getPosition().y-250,Animation.ABSOLUTE,0);
        transformation.setDuration(1000);
        transformation.setFillAfter(true);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(transformation);
        ScaleAnimation scaleAnimation = new ScaleAnimation((float) (mCurrentGotFace.getWidth() / 504),1.0f,(float) (mCurrentGotFace.getHeight() / 496),1.0f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setFillAfter(true);
        set.addAnimation(scaleAnimation);

        mSelfAvatar.setAnimation(set);
        set.start();
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                        animationView.playAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private String getFaceRect(Face face) {
        if (face != null) {
            StringBuilder builder = new StringBuilder("");
            return  builder.append(face.getPosition().x).append(",").append(face.getPosition().y).append(",").append(face.getWidth()).append(",").append(face.getHeight()).toString();
        }
        return null;
    }

    public String getMacAddress() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface iF = interfaces.nextElement();

            byte[] addr = new byte[0];
            try {
                addr = iF.getHardwareAddress();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if (addr == null || addr.length == 0) {
                continue;
            }

            StringBuilder buf = new StringBuilder();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            String mac = buf.toString();
            return  mac;
        }

        return "";
    }

}
