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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bit.facetracker.AndroidApplication;
import bit.facetracker.R;
import bit.facetracker.job.FaceDetectorJob;
import bit.facetracker.job.WearJob;
import bit.facetracker.model.Result;
import bit.facetracker.model.WearResult;
import bit.facetracker.tools.LogUtils;
import bit.facetracker.ui.camera.CameraSourcePreview;
import bit.facetracker.ui.camera.GraphicOverlay;
import bit.facetracker.ui.widget.CustomDraweeView;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends BaseActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_EXTERNAL = 3;

    private int mScreenWidth = 1080;

    public volatile  Bitmap mMarkedBitmap;

    public volatile  boolean mIsGetBitmap;

    private TextView mAttractive;
    private TextView mAgeView;
    private TextView mTimeView;
    private TextView mDayView;
    private TextView mWeekView;
    private TextView mTemperatureRangeView;
    private TextView mTemperatureView;
    private TextView mConditionView;

    private View mFragmeLayout;
    private volatile Face mFace;
    private volatile Frame mFrame;
    private Button mCaptureBtn;
    private int mFaceId;
    private static final int MAXOFFSET_X = 20;
    private static final int MAXOFFSET_Y = 20;
    private static volatile String CAPTUREPATHDIR = "/sdcard/pics/";
    private static volatile String CAPTUREIMGPATH = "";
    private static final int MAXSHOTCOUNT = 15;
    private int mCount = 0;

    private CustomDraweeView mUserAvatar;
    private CustomDraweeView mStarAvatar;
    private TextView mStarName;

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    View mWearSubPanel1;
    CustomDraweeView mWearMainImgView;
    CustomDraweeView mWearOneImgView;
    CustomDraweeView mWearTwoImgView;
    CustomDraweeView mWearThreeImgView;

    View mWearSubPanel2;
    CustomDraweeView mWearMainImgView1;
    CustomDraweeView mWearOneImgView1;
    CustomDraweeView mWearTwoImgView1;
    CustomDraweeView mWearThreeImgView1;

    View mWearSubPanel3;
    CustomDraweeView mWearMainImgView2;
    CustomDraweeView mWearOneImgView2;
    CustomDraweeView mWearTwoImgView2;
    CustomDraweeView mWearThreeImgView2;

    private Handler mHandler;
    private View mResultPanel;
    private View mWearPanel;
    private AnimatorSet mWearpanelAnimationSet = new AnimatorSet();
    private ObjectAnimator animator1;
    private ObjectAnimator animator2;
    private ObjectAnimator animator3;
    boolean mIsAnimationend ;

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        mAttractive = (TextView) findViewById(R.id.attractive);
        mAgeView = (TextView) findViewById(R.id.age);


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

        mTimeView = (TextView)findViewById(R.id.time);
        mDayView = (TextView)findViewById(R.id.day);
        mWeekView = (TextView)findViewById(R.id.week);

        mTemperatureRangeView = (TextView)findViewById(R.id.temperature_range);
        mTemperatureView = (TextView)findViewById(R.id.temperature);
        mConditionView = (TextView)findViewById(R.id.condition);

        mUserAvatar = (CustomDraweeView) findViewById(R.id.self);
        mStarAvatar = (CustomDraweeView) findViewById(R.id.star);

        Calendar c = Calendar.getInstance();

        mTimeView.setText(c.get(Calendar.HOUR_OF_DAY) + ":" +  ((c.get(Calendar.MINUTE) <= 9) ? "0" + c.get(Calendar.MINUTE) : "" + c.get(Calendar.MINUTE)));
        mDayView.setText("公历 " + (c.get(Calendar.MONTH) + 1 )  + "月" +  c.get(Calendar.DAY_OF_MONTH) + "日");

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        mWeekView.setText(dayOfTheWeek);

        mFragmeLayout = findViewById(R.id.topLayout);
        mStarName = (TextView) findViewById(R.id.star_name);

        EventBus.getDefault().register(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    if(mIsGetBitmap) {
                        LogUtils.d("Count", "restart set");
                        mWearpanelAnimationSet.start();
                    }
                }
            }
        };
        mResultPanel = findViewById(R.id.result_panel);
        mWearPanel = findViewById(R.id.wear_panel);
        initWearView();

        animator1 = new ObjectAnimator().ofFloat(mWearSubPanel1, "alpha", 0, 1);
        animator2 = new ObjectAnimator().ofFloat(mWearSubPanel2, "alpha", 0, 1);
        animator3 = new ObjectAnimator().ofFloat(mWearSubPanel3, "alpha", 0, 1);

        mWearSubPanel1.setAlpha(0f);
        mWearSubPanel2.setAlpha(0f);
        mWearSubPanel3.setAlpha(0f);

        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

                mWearSubPanel1.setAlpha(0f);
                mWearSubPanel2.setAlpha(0f);
                mWearSubPanel3.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LogUtils.d("Count","animator1");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        animator2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mWearSubPanel1.setAlpha(0f);
                mWearSubPanel2.setAlpha(0f);
                mWearSubPanel3.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LogUtils.d("Count","animator2");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        animator3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mWearSubPanel1.setAlpha(0f);
                mWearSubPanel2.setAlpha(0f);
                mWearSubPanel3.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LogUtils.d("Count","animator3");
                mIsAnimationend = true;
//                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });



//        mWearpanelAnimationSet.addListener(new AnimatorListenerAdapter() {
//            /**
//             * {@inheritDoc}
//             *
//             * @param animation
//             */
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                LogUtils.d("Count","onAnimationEnd Set");
//                if(mIsGetBitmap) {
//                    mWearpanelAnimationSet.end();
//                    mWearpanelAnimationSet.start();
//                }
//            }
//        });

//        mWearpanelAnimationSet.setDuration(1000);

//        View decorview = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE ;
//        decorview.setSystemUiVisibility(uiOptions);
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
                .setRequestedPreviewSize(1920,1080)
                .setFacing(0)
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
        mCount = 0 ;
        mFragmeLayout.setVisibility(View.GONE);
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
                Log.e(TAG, "Unable to start camera source.", e);
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
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
            mIsGetBitmap = false;
            mCount = 0 ;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWearpanelAnimationSet.end();
                    mFragmeLayout.setVisibility(View.GONE);
                    mWearSubPanel1.setAlpha(0f);
                    mWearSubPanel2.setAlpha(0f);
                    mWearSubPanel3.setAlpha(0f);
                }
            });
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            if (!mIsGetBitmap) {
                mFace = face;
            }

            if (Math.abs(mFace.getPosition().x - face.getPosition().x) <= MAXOFFSET_X && Math.abs(mFace.getPosition().y - face.getPosition().y) <= MAXOFFSET_Y && !mIsGetBitmap) {
                mCount ++;
            }else {
                mCount = 0;
            }

            if (mCount >= MAXSHOTCOUNT && !mIsGetBitmap) {
                mIsGetBitmap = true;
                CAPTUREIMGPATH = CAPTUREPATHDIR + System.currentTimeMillis() + "_" + "capture.png";
                LogUtils.d("Count","got it");
                CropPreviewFrame capturetask = new CropPreviewFrame(CAPTUREIMGPATH);
                capturetask.execute(mFrame);
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
            mCount = 0;
            mIsGetBitmap = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWearpanelAnimationSet.end();
                    mFragmeLayout.setVisibility(View.GONE);
                    mWearSubPanel1.setAlpha(0f);
                    mWearSubPanel2.setAlpha(0f);
                    mWearSubPanel3.setAlpha(0f);
                }
            });

        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
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

    public void detectorface(String path) {
        FaceDetectorJob job = new FaceDetectorJob(path);
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
            return mDelegate.detect(frame);
        }

        public boolean isOperational() {
            return mDelegate.isOperational();
        }

        public boolean setFocus(int id) {
            return mDelegate.setFocus(id);
        }
    }

    class CropPreviewFrame extends AsyncTask<Frame,String,File> {

        String filepath;

        public CropPreviewFrame(String filepath) {
            this.filepath = filepath;
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
        protected File doInBackground(Frame... params) {
            Frame frame = params[0];
            File file = null;
            if (frame != null) {
                Bitmap bitmap = getBitmap(frame);
                if (bitmap != null) {

                    double x = Math.max(0,mFace.getPosition().x);
                    double y = Math.max(0,mFace.getPosition().y);

                    double w = mFace.getWidth();
                    double h = mFace.getHeight();

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

                    LogUtils.d("Position","x = " + (int)mFace.getPosition().x + "y = " + mFace.getPosition().y + "width = " + mFace.getWidth() + "heith = "  + mFace.getHeight());

                    LogUtils.d("Position","x = " + (int)mFace.getPosition().x + "y = " + mFace.getPosition().y + "width = " + mFace.getWidth() + "heith = "  + mFace.getHeight());
                    File dir = new File(CAPTUREPATHDIR);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    file = new File(filepath);
                    try {
                        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                        disbitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            detectorface(file.getAbsolutePath());
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
    public void onEvent(Result result) {
        if (result != null) {

            mFragmeLayout.setVisibility(View.VISIBLE);
            mResultPanel.setVisibility(View.VISIBLE);
            mResultPanel.setAlpha(1f);
            mWearPanel.setVisibility(View.GONE);

            mAttractive.setText(getString(R.string.displayAttractive,result.attributes.attractive));
            mAgeView.setText(getString(R.string.displayAge,result.attributes.age));


            mStarName.setText(getString(R.string.displayStarName,result.name));
            mUserAvatar.setCircleImageURI("file://" + CAPTUREIMGPATH);
            mStarAvatar.setCircleImageURI(result.cel_image.thumbnail);

            ObjectAnimator anim = ObjectAnimator.ofFloat(mFragmeLayout, "alpha", 0, 1);
            anim.setDuration(1500);
            anim.start();


            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (result.attributes.gender.size() > 1) {
                                int gender = 0;
                                if(result.attributes.gender.get(0).probability < result.attributes.gender.get(1).probability) {
                                    gender = 1;
                                }
                                WearJob wearJob = new WearJob(gender);
                                AndroidApplication.getInstance().getJobManager().addJob(wearJob);
                            }else {
                                WearJob wearJob = new WearJob();
                                AndroidApplication.getInstance().getJobManager().addJob(wearJob);
                            }
                        }
                    }, 3000);


                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WearResult wearResult) {
        if (wearResult != null && wearResult.code.equals("200")) {
//            mWearpanelAnimationSet = new AnimatorSet();
            mWearpanelAnimationSet.end();
            mWearpanelAnimationSet.setDuration(4000);

            if (wearResult.result != null && wearResult.result.size() > 0) {

                int size = wearResult.result.size();
                if (size == 1) {
                    mWearMainImgView.setImageURI( wearResult.result.get(0).MainImg);
                    mWearOneImgView.setImageURI(wearResult.result.get(0).ImgOne);
                    mWearTwoImgView.setImageURI(wearResult.result.get(0).ImgTwo);
                    mWearThreeImgView.setImageURI(wearResult.result.get(0).ImgThree);

                    mWearpanelAnimationSet.playSequentially(animator1);

                } else if (size == 2) {
                    mWearMainImgView.setImageURI( wearResult.result.get(0).MainImg);
                    mWearOneImgView.setImageURI(wearResult.result.get(0).ImgOne);
                    mWearTwoImgView.setImageURI(wearResult.result.get(0).ImgTwo);
                    mWearThreeImgView.setImageURI(wearResult.result.get(0).ImgThree);

                    mWearMainImgView1.setImageURI( wearResult.result.get(1).MainImg);
                    mWearOneImgView1.setImageURI(wearResult.result.get(1).ImgOne);
                    mWearTwoImgView1.setImageURI(wearResult.result.get(1).ImgTwo);
                    mWearThreeImgView1.setImageURI(wearResult.result.get(1).ImgThree);

                    mWearpanelAnimationSet.playSequentially(animator1,animator2);
                } else if(size == 3) {
                    mWearMainImgView.setImageURI( wearResult.result.get(0).MainImg);
                    mWearOneImgView.setImageURI(wearResult.result.get(0).ImgOne);
                    mWearTwoImgView.setImageURI(wearResult.result.get(0).ImgTwo);
                    mWearThreeImgView.setImageURI(wearResult.result.get(0).ImgThree);

                    mWearMainImgView1.setImageURI( wearResult.result.get(1).MainImg);
                    mWearOneImgView1.setImageURI(wearResult.result.get(1).ImgOne);
                    mWearTwoImgView1.setImageURI(wearResult.result.get(1).ImgTwo);
                    mWearThreeImgView1.setImageURI(wearResult.result.get(1).ImgThree);

                    mWearMainImgView2.setImageURI( wearResult.result.get(2).MainImg);
                    mWearOneImgView2.setImageURI(wearResult.result.get(2).ImgOne);
                    mWearTwoImgView2.setImageURI(wearResult.result.get(2).ImgTwo);
                    mWearThreeImgView2.setImageURI(wearResult.result.get(2).ImgThree);
                    mWearpanelAnimationSet.playSequentially(animator1,animator2,animator3);
                }

                AnimatorSet set = new AnimatorSet();
                ObjectAnimator animWear = ObjectAnimator.ofFloat(mWearPanel, "alpha", 0, 1);
                ObjectAnimator animResult = ObjectAnimator.ofFloat(mResultPanel, "alpha", 1, 0);
                mResultPanel.setVisibility(View.GONE);
                mWearPanel.setVisibility(View.VISIBLE);
                set.playTogether(animWear, animResult);
                set.start();

                animWear.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mIsAnimationend = false;
                        mWearpanelAnimationSet.start();
                        new Thread(new Alarm()).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });



            }
        }

    }

    public void initWearView() {
        mWearMainImgView = (CustomDraweeView)findViewById(R.id.img_main);
        mWearOneImgView = (CustomDraweeView)findViewById(R.id.img_one);
        mWearTwoImgView = (CustomDraweeView)findViewById(R.id.img_two);
        mWearThreeImgView = (CustomDraweeView)findViewById(R.id.img_three);
        mWearSubPanel1 = findViewById(R.id.wear_subpanel1);

        mWearMainImgView1 = (CustomDraweeView)findViewById(R.id.img_main1);
        mWearOneImgView1 = (CustomDraweeView)findViewById(R.id.img_one1);
        mWearTwoImgView1 = (CustomDraweeView)findViewById(R.id.img_two1);
        mWearThreeImgView1 = (CustomDraweeView)findViewById(R.id.img_three1);
        mWearSubPanel2 = findViewById(R.id.wear_subpanel2);

        mWearMainImgView2 = (CustomDraweeView)findViewById(R.id.img_main2);
        mWearOneImgView2 = (CustomDraweeView)findViewById(R.id.img_one2);
        mWearTwoImgView2 = (CustomDraweeView)findViewById(R.id.img_two2);
        mWearThreeImgView2 = (CustomDraweeView)findViewById(R.id.img_three2);
        mWearSubPanel3 = findViewById(R.id.wear_subpanel3);
    }
    class Alarm implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            while (mIsGetBitmap) {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mIsAnimationend) {
                    mHandler.sendEmptyMessage(0);
                    mIsAnimationend = false;
                }
            }
        }
    }


}
