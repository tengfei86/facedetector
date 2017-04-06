package bit.facetracker.ui.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import bit.facetracker.tools.LogUtils;

/**
 * Created by blade on 23/03/2017.
 */

public class TextContainer extends LinearLayout {


    public float TIMEOFFSET = 100;
    public float PERCENT = 0.9f;

    public TextContainer(Context context) {
        super(context);
        init(context);
    }

    public TextContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(HORIZONTAL);
    }

    public void setDisplayText(String text) {
        removeAllViews();
        if (!TextUtils.isEmpty(text)) {
            int length = text.length();
            for (int i = 0; i < length; i++) {
                FlashTextView view = new FlashTextView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
                params.weight = 1;
                view.setBackgroundColor(Color.TRANSPARENT);
                view.setRectAlpha(255);
                view.setText(String.valueOf(text.charAt(i)));
                view.setLayoutParams(params);
                if (i != 0)
                    view.setVisibility(View.INVISIBLE);
                addView(view);
            }
            ValueAnimator animator = ValueAnimator.ofFloat(0.0f, (float) (TIMEOFFSET * PERCENT * getChildCount() - 1) + TIMEOFFSET);
            animator.setDuration(500 * text.length());

            CustomListAnimatorListener listAnimatorListener = new CustomListAnimatorListener(getChildCount(), TIMEOFFSET);
            animator.addUpdateListener(listAnimatorListener);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (getChildCount() > 0 && ((FlashTextView) getChildAt(getChildCount() - 1)).getRectAlpha() > 0) {
                        ((FlashTextView) getChildAt(getChildCount() - 1)).setRectAlpha(0);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // TODO
    }

    class CustomListAnimatorListener implements ValueAnimator.AnimatorUpdateListener {


        public int count;

        public float timeoffset;

        public CustomListAnimatorListener(int count, float timeoffset) {
            this.count = count;
            this.timeoffset = timeoffset;
        }

        /**
         * <p>Notifies the occurrence of another frame of the animation.</p>
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            int start = (int) (((float) animation.getAnimatedValue() - timeoffset) / (PERCENT * timeoffset));
            for (int i = Math.min(count - 1, Math.max(start, 0)); i < count; i++) {
                if ((float) animation.getAnimatedValue() <= (i * timeoffset * PERCENT + timeoffset) && getChildAt(i).getVisibility() == VISIBLE && ((FlashTextView) getChildAt(i)).getRectAlpha() > 0) {
                    int alpha = (int) ((1 - ((float) animation.getAnimatedValue() - i * timeoffset * PERCENT) / timeoffset) * 255);

                    int pre = i - 1;
                    if (pre >= 0 && ((FlashTextView) getChildAt(pre)).getRectAlpha() > 0) {
                        ((FlashTextView) getChildAt(pre)).setRectAlpha(0);
                    }
                    ((FlashTextView) getChildAt(i)).setRectAlpha(alpha);
                }
                if ((float) animation.getAnimatedValue() >= ((i * timeoffset * PERCENT))) {
                    if (i < count) {
                        getChildAt(i).setVisibility(VISIBLE);
                    }
                }
            }
        }

    }


    CountDownTimer time = new CountDownTimer(1000,5) {

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {

        }
    };


}
