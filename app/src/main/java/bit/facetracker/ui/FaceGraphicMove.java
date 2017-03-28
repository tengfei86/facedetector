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
import android.graphics.RectF;

import com.google.android.gms.vision.face.Face;

import bit.facetracker.R;
import bit.facetracker.tools.LogUtils;
import bit.facetracker.ui.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphicMove extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

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

    private static int mCurrentColorIndex = 0;

    private Paint mBoxPaint;

    private volatile Face mFace;

    private PointF[] startDrawPoint = {new PointF(), new PointF(), new PointF(), new PointF()};

    private RectF commonRectF = new RectF();

    // 0 step one | 1 step two
    private int type = 0;
    private float haftWidth;
    private float halfHeight;

    private float fullwidth;
    private float fullheight;
    private int mFaceId;

    FaceGraphicMove(GraphicOverlay overlay) {
        super(overlay);
        mBoxPaint = new Paint();
        mBoxPaint.setColor(Color.WHITE);
        mBoxPaint.setStyle(Paint.Style.FILL);
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

            float xOffset = scaleX(face.getWidth() * 1 / 2.0f);
            float yOffset = scaleY(face.getHeight() * 1 / 2.0f);

            drawRect(canvas, mBoxPaint, originalX, originalY, xOffset, yOffset, progress);
        }

    }

    private void updateProgress() {
        progress += OFFSET;
        if (progress > 1.0) {
            progress = 0.0f;
            type = ++type % 2;
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




}
