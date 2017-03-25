package bit.facetracker.ui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by blade on 22/03/2017.
 */

public class ProgressView extends View {

    private RectF mProgressRectF = new RectF();

    private  float mProgress = 0.0f;

    private Paint mRectPaint;
    private TextPaint mTextPaint;

    float fontHeight = 0.0f;
    float fontWidth = 0.0f;
    final int PROGRESSBAR_COLOR = 0xFFE2A923;

    Paint.FontMetrics fontMetrics;

    private final  int MARGIN = 20;

    private final int PROGRESSBARMAXLENGTH = 352;

    private float mMax = 1.0f;


    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public ProgressView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p>
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     * @see #View(Context, AttributeSet, int)
     */
    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply <code>R.attr.buttonStyle</code> for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @see #View(Context, AttributeSet)
     */
    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setColor(PROGRESSBAR_COLOR);
        mProgressRectF = new RectF();

        mTextPaint = new TextPaint();
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(60);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        fontMetrics =  mTextPaint.getFontMetrics();
        fontHeight = Math.abs(fontMetrics.top) + fontMetrics.bottom;
        fontWidth = fontHeight;


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int compoundPaddingLeft = getPaddingLeft();
        final int compoundPaddingTop = getPaddingTop();
        final int compoundPaddingRight = getPaddingRight();
        final int compoundPaddingBottom = getPaddingBottom();

        float rectRight = mProgress * PROGRESSBARMAXLENGTH;

        mProgressRectF.set(compoundPaddingLeft,compoundPaddingTop,rectRight,getHeight() - compoundPaddingBottom);
        canvas.drawRect(mProgressRectF,mRectPaint);

        float fontLeft = PROGRESSBARMAXLENGTH + MARGIN;

        RectF rect = new RectF(fontLeft ,compoundPaddingTop,fontLeft + (getHeight() - compoundPaddingTop - compoundPaddingBottom),getHeight() - compoundPaddingBottom);

        int baselineX = (int) rect.centerX();
        int baseLineY = (int) (rect.centerY() - fontMetrics.top / 2 - fontMetrics.bottom / 2);
        if(mProgress >= 0.01f)
        canvas.drawText(String.valueOf((int)(100 * mProgress)),baselineX,baseLineY,mTextPaint);
        // Draw Progress display


    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        invalidate();
    }


    public void setMaxProgress(float maxProgress) {
        mMax = maxProgress;
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", 0.01f, maxProgress);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.setDuration(1000);
        objectAnimator.start();
    }




}
