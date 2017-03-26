package bit.facetracker.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewDebug;

/**
 * Created by blade on 18/03/2017.
 */

public class FlashTextView extends android.support.v7.widget.AppCompatTextView {


    // display attributes
    private TextPaint mTextPaint;
    private Paint mRectPaint;
    private int mRectPaintAlpha = 255;
    @ViewDebug.ExportedProperty(category = "text")
    private int mGravity = Gravity.TOP | Gravity.START;

    public FlashTextView(Context context) {
        super(context);
        init(context);
    }

    public FlashTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlashTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = context.getResources().getDisplayMetrics().density;
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setColor(Color.WHITE);
        mRectPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setColor(Color.WHITE);
        mRectPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int compoundPaddingLeft = getCompoundPaddingLeft();
        final int compoundPaddingTop = getCompoundPaddingTop();
        final int compoundPaddingRight = getCompoundPaddingRight();
        final int compoundPaddingBottom = getCompoundPaddingBottom();
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        final int right = getRight();
        final int left = getLeft();
        final int bottom = getBottom();
        final int top = getTop();
        drawSingleWord(canvas, getText().toString());
    }

    private void drawSingleWord(Canvas canvas, String word) {

        int vheight = getHeight();
        int hwidth = getWidth();

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float fontHeight = Math.abs(fontMetrics.top) + fontMetrics.bottom;
        float fontWidth = fontHeight;

        int left = (int) (hwidth - fontWidth) / 2;
        int top = (int) (vheight - fontHeight) / 2;

        Rect rect = new Rect(left, top, (int) (left + fontWidth), (int) (top + fontHeight));
        mRectPaint.setAlpha(mRectPaintAlpha);

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        int baselineX = rect.centerX();
        int baseLineY = (int) (rect.centerY() - fontMetrics.top / 2 - fontMetrics.bottom / 2);
        canvas.drawText(word, baselineX, baseLineY, mTextPaint);

        canvas.drawRect(rect, mRectPaint);

    }

    public void setRectAlpha(int alpha) {
        mRectPaintAlpha = alpha;
        invalidate();
    }

    public int getRectAlpha() {
        return mRectPaintAlpha;
    }


}
