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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;

import com.google.android.gms.vision.face.Face;

import bit.facetracker.tools.LogUtils;
import bit.facetracker.ui.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphicMove extends GraphicOverlay.Graphic {

    private volatile float progress = 0.0f;
    private float rectBand = 10.0f;
    private float OFFSET = (float) (1.0 / 8);
    private int LOOPCOUNT = 2;
    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };

    private Paint mBoxPaint;
    private Paint mGradientPaint;
    private TextPaint mTextPaint;
    private volatile Face mFace;

    private PointF[] startDrawPoint = {new PointF(), new PointF(), new PointF(), new PointF()};

    private RectF commonRectF = new RectF();
    private RectF scanBodyRectF = new RectF();
    private RectF gradientRectF = new RectF();

    // 0 step one | 1 step two
    private int type = 0;

    private float fullwidth;
    private float fullheight;
    private int mFaceId;

    private boolean mIsScanBody = true;
    private float SCANBODYOFFSET = (float) (1.0 / 30);
    private float mScanBodyProgress = 0.0f;

    private float mNormalRectOffsetRatio  = 0.5f;
    private float mScanBodyRectOffsetRatio = (float)(3.0 / 2.0) ;

    // scanbody down offset
    private static final float DOWNOFFSET = 20f;
    private static  final float ScanBodyRectBand = 10.0f;
    private static  final float MULTI_FACEREGION = 1.0f;
    private static  final float GRADIENT_HEIGHT = 50.0f;

    FaceGraphicMove(GraphicOverlay overlay) {
        super(overlay);
        mBoxPaint = new Paint();
        mBoxPaint.setColor(Color.WHITE);
        mBoxPaint.setAntiAlias(true);
        mBoxPaint.setTextSize(60);
        mBoxPaint.setStyle(Paint.Style.FILL);


        mGradientPaint = new Paint();
        mGradientPaint.setColor(Color.WHITE);
        mGradientPaint.setAntiAlias(true);
        mGradientPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(60);
        mTextPaint.setStyle(Paint.Style.FILL);

    }

    void setId(int id) {
        mFaceId = id;
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        for (int i = 0; i < LOOPCOUNT; i++) {

            updateProgress();

            float originalX = scaleX(face.getPosition().x);
            float originalY = scaleY(face.getPosition().y);

            fullwidth = scaleX(face.getWidth());
            fullheight = scaleY(face.getHeight());

            float xOffset = scaleX(face.getWidth() * mNormalRectOffsetRatio);
            float yOffset = scaleY(face.getHeight() * mNormalRectOffsetRatio);

            drawRect(canvas, mBoxPaint, originalX, originalY, xOffset, yOffset, progress);

            if (mIsScanBody) {
                float xScanBodyOffset = scaleX(face.getWidth() * mScanBodyRectOffsetRatio);
                drawScanBodyRect(canvas, mBoxPaint, originalX, originalY,xScanBodyOffset,mScanBodyProgress, fullheight, fullwidth);
            }

        }

    }

    private void updateProgress() {
        progress += OFFSET;
        if (progress > 1.0) {
            progress = 0.0f;
            type = ++type % 2;
        }

        if (mIsScanBody && mScanBodyProgress <= 2) {
            mScanBodyProgress += SCANBODYOFFSET;
        }

    }

    private void drawRect(Canvas canvas, Paint paint, float originX, float originY, float xOffset, float yOffset, float progress) {


        if (type == 0) {

            float rectProgressX = (fullwidth - xOffset) / 2 * progress;
            float rectProgressY = (fullheight - yOffset) / 2 * progress;

            float rectWidth = (xOffset - rectBand) * progress + rectBand;
            float rectHeight = (yOffset - rectBand) * progress + rectBand;

            for (int i = 0; i < startDrawPoint.length; i++) {
                PointF point = startDrawPoint[i];
                if (i == 0) {
                    float x = originX + rectProgressX;
                    float y = originY;
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectWidth, startDrawPoint[i].y + rectBand);
                } else if (i == 1) {
                    float x = originX + fullwidth - rectBand;
                    float y = originY + rectProgressY;
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectBand, startDrawPoint[i].y + rectHeight);
                } else if (i == 2) {
                    float x = originX + (fullwidth - rectWidth - rectProgressX);
                    float y = originY + fullheight - rectBand;
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectWidth, startDrawPoint[i].y + rectBand);
                } else {
                    float x = originX;
                    float y = originY + (fullheight - rectHeight - rectProgressY);
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectBand, startDrawPoint[i].y + rectHeight);
                }
                canvas.drawRect(commonRectF, paint);

            }
        } else if (type == 1) {

            float rectProgressX = (fullwidth - xOffset) / 2 * (1 - progress);
            float rectProgressY = (fullheight - yOffset) / 2 * (1 - progress);

            float rectWidth = (xOffset - rectBand) * (1 - progress) + rectBand;
            float rectHeight = (yOffset - rectBand) * (1 - progress) + rectBand;

            for (int i = 0; i < startDrawPoint.length; i++) {
                PointF point = startDrawPoint[i];
                if (i == 0) {
                    float x = originX + (fullwidth - rectWidth - rectProgressX);
                    float y = originY;
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectWidth, startDrawPoint[i].y + rectBand);
                } else if (i == 1) {
                    float x = originX + fullwidth - rectBand;
                    float y = originY + (fullheight - rectHeight - rectProgressY);
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectBand, startDrawPoint[i].y + rectHeight);
                } else if (i == 2) {
                    float x = originX + rectProgressX;
                    float y = originY + fullheight - rectBand;
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectWidth, startDrawPoint[i].y + rectBand);
                } else {
                    float x = originX;
                    float y = originY + rectProgressY;
                    point.set(x, y);
                    commonRectF.set(startDrawPoint[i].x, startDrawPoint[i].y, startDrawPoint[i].x + rectBand, startDrawPoint[i].y + rectHeight);
                }
                canvas.drawRect(commonRectF, paint);

            }
        }

    }

    private void drawScanBodyRect(Canvas canvas, Paint paint, float originX, float originY, float xOffset,float progress,float fullheight,float fullwidth) {

        if (progress <= 1) {
            float left = originX - (xOffset - fullwidth) / 2;
            float top = originY + fullheight + DOWNOFFSET;

            float right = left + xOffset * progress;
            float bottom = top  + ScanBodyRectBand;
            scanBodyRectF.set(left,top,right,bottom);
            canvas.drawRect(scanBodyRectF,paint);

        } else  {

            float topmovement = (progress - 1) * MULTI_FACEREGION * fullheight;
            float bottomovement  = fullheight - (progress - 1) * MULTI_FACEREGION * fullheight;

            float movment = Math.min(topmovement,GRADIENT_HEIGHT);

            if (bottomovement <= GRADIENT_HEIGHT) {
                movment = bottomovement;
            }

            float left = originX - (xOffset - fullwidth) / 2;
            float top = originY + fullheight + DOWNOFFSET + topmovement;

            float right = left + xOffset;
            float bottom = top  + ScanBodyRectBand;

            scanBodyRectF.set(left,top,right,bottom);
            canvas.drawRect(scanBodyRectF,paint);

            float gradienttop = originY + fullheight + DOWNOFFSET + (progress - 1) * MULTI_FACEREGION * fullheight - movment;
            float gradientright = left + xOffset;
            float gradientbottom = gradienttop  + movment;
            gradientRectF.set(left,gradienttop,gradientright,gradientbottom);
            Shader shader  = new LinearGradient(left,gradienttop,left,gradientbottom,0x00FFFFFF,0x9FFFFFFF, Shader.TileMode.CLAMP);
            mGradientPaint.setShader(shader);
            canvas.drawRect(gradientRectF,mGradientPaint);
            canvas.drawText(String.valueOf((int) (Math.min(((progress - 1) * 100 + 1),100))) + '%',gradientright + 10,top,mTextPaint);

        }

    }



}
