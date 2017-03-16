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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.android.gms.vision.face.Face;

import bit.facetracker.R;
import bit.facetracker.tools.LogUtils;
import bit.facetracker.ui.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphicNew extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private float progress = 0.0f;
    private float rectBand = 50.0f;

    private float OFFSET = (float)(1.0 / 8);

    private static final int COLOR_CHOICES[] = {
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    };

    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;
    private Bitmap mFocusBitmap;

    private PointF[] startDrawPoint = { new PointF(),new PointF(),new PointF(),new PointF()};

    private RectF commonRectF = new RectF();


    FaceGraphicNew(GraphicOverlay overlay) {
        super(overlay);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(Color.WHITE);
        mBoxPaint.setStyle(Paint.Style.FILL);

        mFocusBitmap = BitmapFactory.decodeResource(overlay.getResources(), R.mipmap.focus);
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

        updateProgress();

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        initStartDrawPoint(x, y, xOffset, yOffset);
        drawRect(canvas, mBoxPaint, xOffset, yOffset);
    }

    private void updateProgress() {
        LogUtils.d("Graphic","progress = " + progress);
        progress += OFFSET;
        if(progress > 1.0) {
            progress = 0.0f;
        }
    }

    private void initStartDrawPoint(float centerX,float centerY,float xOffset,float yOffset) {
        for(int i = 0; i < startDrawPoint.length;i++) {
            PointF point = startDrawPoint[i];
            LogUtils.d("Graphic","i = " + i + "x = " + point.x + " y = " + point.y);
            if (i == 0) {
                float x = centerX - xOffset / 2;
                float y = centerY - yOffset;
                point.set(x,y);
            } else if (i == 1) {
                float x = centerX + xOffset  - rectBand;
                float y = centerY - yOffset / 2;
                point.set(x,y);
            } else if (i == 2) {
                float x = centerX - xOffset / 2;
                float y = centerY + yOffset  - rectBand;
                point.set(x,y);
            } else {
                float x = centerX - xOffset ;
                float y = centerY - yOffset / 2 ;
                point.set(x,y);
            }

        }
    }

    private void drawRect(Canvas canvas,Paint paint,float xOffset,float yOffset) {

        float progressX = xOffset * progress;
        float progressY = yOffset * progress;

        for(int i = 0; i < startDrawPoint.length;i++) {
            if (i == 0) {
                commonRectF.set(startDrawPoint[i].x,startDrawPoint[i].y,startDrawPoint[i].x + progressX,startDrawPoint[i].y + rectBand);
            } else if (i == 1) {
                commonRectF.set(startDrawPoint[i].x,startDrawPoint[i].y,startDrawPoint[i].x + rectBand,startDrawPoint[i].y + progressY);
            } else if (i == 2) {
                commonRectF.set(startDrawPoint[i].x,startDrawPoint[i].y,startDrawPoint[i].x + progressX,startDrawPoint[i].y + rectBand);
            } else {
                commonRectF.set(startDrawPoint[i].x,startDrawPoint[i].y,startDrawPoint[i].x + rectBand,startDrawPoint[i].y + progressY);
            }
            canvas.drawRect(commonRectF,paint);
        }
    }



}
