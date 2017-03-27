package bit.facetracker.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.FloatRange;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Author:      Melodyxxx
 * Email:       95hanjie@gmail.com
 * Created at:  16/09/26.
 * Description: 图片模糊类
 */

/**
 * 图片模糊类<br/>
 * 此类实现的模糊模式为覆盖模式, 即需要模糊的目标 View 应位于输入图片的上方.<br/>
 * 1: 输入图片的宽高均大于目标 View 的宽高, 此时不会处理输入图片的宽高来适应目标 View.<br/>
 * 2: 输入图片的宽或高小于目标 View 的宽或高, 此时会根据输入的图片重新生成一个新的宽高均大于目标 View 的宽高的输入
 * 图片, 以此来适应目标 View.
 */
public class Blur {

    private static final String TAG = "Blur";

    private Context mContext;
    private Bitmap mInputBitmap;
    private View mTargetView;

    private float mScaleFactor;
    private float mRadius;


    Bitmap outputBitmap ;

    Paint paint = new Paint();


    RenderScript rs;
    Allocation input ;
    Allocation output;
    ScriptIntrinsicBlur blur;

    /**
     * @param context     上下文
     * @param inputBitmap 输入图片Bitmap
     * @param targetView  需要被模糊背景的目标View
     */
    public Blur(Context context, Bitmap inputBitmap, View targetView) {
        this.mContext = context;
        this.mInputBitmap = inputBitmap;
        this.mTargetView = targetView;
    }

    /**
     * @param context        上下文
     * @param inputImageView 需要使用背景作为输入图片的ImageView
     * @param targetView     需要被模糊背景的目标View
     */
    public Blur(Context context, ImageView inputImageView, View targetView) {

        inputImageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = inputImageView.getDrawingCache();
        // 取出ImageView的Bitmap
        Bitmap tempBitmap = bitmap.copy(bitmap.getConfig(), true);
        inputImageView.setDrawingCacheEnabled(false);

        this.mContext = context;
        // 设为InputBitmap
        this.mInputBitmap = tempBitmap;
        this.mTargetView = targetView;
    }

    public Blur(Context context, View targetView, @FloatRange(from = 0.0f, fromInclusive = false) final float scaleFactor,
                @FloatRange(from = 0.0f, to = 25f, fromInclusive = false) final float radius) {
        this.mContext = context;
        this.mTargetView = targetView;
        this.mScaleFactor = scaleFactor;
        this.mRadius = radius;

        paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);


    }

    /**
     * 不缩放原图最大化模糊背景
     */
    public void normalMaxBlur() {
        blur(1f, 25f);
    }

    /**
     * 自定义模糊背景
     *
     * @param scaleFactor 缩放大小 (>0.0), 例如若想将输入图片放大2倍然后再模糊(提高模糊效率), 则传入2.
     * @param radius      缩放半径 (0.0 , 25.0]
     */
    public void blur(
            @FloatRange(from = 0.0f, fromInclusive = false) final float scaleFactor,
            @FloatRange(from = 0.0f, to = 25f, fromInclusive = false) final float radius) {

        long startTime = System.currentTimeMillis();

        if (scaleFactor <= 0.0f) {
            throw new IllegalArgumentException("Value must be > 0.0 (was " + scaleFactor + ")");
        }
        if (radius <= 0.0f || radius > 25.0f) {
            throw new IllegalArgumentException("Value must be > 0.0 and ≤ 25.0 (was " + radius + ")");
        }
        if (outputBitmap == null) {
            outputBitmap = Bitmap.createBitmap((int) (mTargetView.getMeasuredWidth() / mScaleFactor),
                    (int) (mTargetView.getMeasuredHeight() / mScaleFactor), Bitmap.Config.ARGB_8888);
        }

        // 调整输入图片以适应目标View
        adjustInputBitmap();

        Canvas canvas = new Canvas(outputBitmap);
        canvas.translate(-mTargetView.getLeft() / scaleFactor, -mTargetView.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        canvas.drawBitmap(mInputBitmap, 0, 0, paint);

        if(rs == null)
        rs = RenderScript.create(mContext);

        if(input == null)
        input = Allocation.createFromBitmap(rs, outputBitmap);

        if(output == null)
        output = Allocation.createTyped(rs, input.getType());

        if(blur == null)
        blur = ScriptIntrinsicBlur.create(
                rs, Element.U8_4(rs));

        blur.setRadius(mRadius);
        blur.setInput(input);
        blur.forEach(output);
        output.copyTo(outputBitmap);
        mTargetView.setBackground(new BitmapDrawable(
                mContext.getResources(), outputBitmap));
//        rs.destroy();
        Log.e(TAG, "spend: " + (System.currentTimeMillis() - startTime) + "ms");
    }





    public BitmapDrawable blur(
            Frame frame,
            @FloatRange(from = 0.0f, fromInclusive = false) final float scaleFactor,
            @FloatRange(from = 0.0f, to = 25f, fromInclusive = false) final float radius) {
        if (frame != null) {
            mInputBitmap = getBitmap(frame);

            long startTime = System.currentTimeMillis();

            if (scaleFactor <= 0.0f) {
                throw new IllegalArgumentException("Value must be > 0.0 (was " + scaleFactor + ")");
            }
            if (radius <= 0.0f || radius > 25.0f) {
                throw new IllegalArgumentException("Value must be > 0.0 and ≤ 25.0 (was " + radius + ")");
            }

            if (outputBitmap == null) {
                outputBitmap = Bitmap.createBitmap((int) (mTargetView.getMeasuredWidth() / mScaleFactor),
                        (int) (mTargetView.getMeasuredHeight() / mScaleFactor), Bitmap.Config.ARGB_8888);
            }

            // 调整输入图片以适应目标View
            adjustInputBitmap();

            Canvas canvas = new Canvas(outputBitmap);
            canvas.translate(-mTargetView.getLeft() / mScaleFactor, -mTargetView.getTop() / mScaleFactor);
            canvas.scale(1 / mScaleFactor, 1 / mScaleFactor);

            canvas.drawBitmap(mInputBitmap, 0, 0, paint);

            if(rs == null)
                rs = RenderScript.create(mContext);

            if(input == null)
                input = Allocation.createFromBitmap(rs, outputBitmap);

            if(output == null)
                output = Allocation.createTyped(rs, input.getType());

            if(blur == null)
                blur = ScriptIntrinsicBlur.create(
                        rs, Element.U8_4(rs));

            blur.setRadius(radius);
            blur.setInput(input);
            blur.forEach(output);
            output.copyTo(outputBitmap);
            BitmapDrawable drawable = new BitmapDrawable(
                    mContext.getResources(), outputBitmap);
            rs.destroy();
            return  drawable;
        }

        return  null;

    }
    // rotate 90
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
        matrix.postRotate(-90);
        Bitmap disbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return disbitmap;
    }


    private void adjustInputBitmap() {

        float inputWidth = mInputBitmap.getWidth();
        float inputHeight = mInputBitmap.getHeight();
        float targetWidth = mTargetView.getMeasuredWidth();
        float targetHeight = mTargetView.getMeasuredHeight();

        if (inputWidth >= targetWidth && inputHeight >= targetHeight) {
            // 不需要处理
            return;
        }

        // 需要处理
        float scale = inputWidth / inputHeight;
        float dstWidth;
        float dstHeight;
        if ((targetWidth - inputWidth) >= (targetHeight - inputHeight)) {
            // 放大宽度至目标View的宽度, 并且保持比例不变
            dstWidth = targetWidth;
            dstHeight = dstWidth / scale;
        } else {
            // 放大高度至目标View的高度, 并且保持比例不变
            dstHeight = targetHeight;
            dstWidth = dstHeight / scale;
        }
        mInputBitmap = mInputBitmap.createScaledBitmap(mInputBitmap, (int) dstWidth, (int) dstHeight, true);
    }


    public void destroyTs() {
        if (rs != null) {
            rs.destroy();
        }
    }

}
