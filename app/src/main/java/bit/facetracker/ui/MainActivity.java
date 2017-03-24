package bit.facetracker.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;

import bit.facetracker.R;
import bit.facetracker.tools.LogUtils;
import bit.facetracker.ui.widget.FlashTextView;
import bit.facetracker.ui.widget.ProgressView;
import bit.facetracker.ui.widget.TextContainer;

public class MainActivity extends AppCompatActivity {

    FlashTextView view;
    ProgressView progressView;
    TextContainer container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (FlashTextView) findViewById(R.id.flash);
        progressView = (ProgressView) findViewById(R.id.progress);
        container = (TextContainer) findViewById(R.id.textcontainer);
        container.setDisplayText("中国好地方");
//
//        PropertyValuesHolder mPropertyValuesHolderScaleX = PropertyValuesHolder.ofInt("rectAlpha", 255,0);
//        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(mPropertyValuesHolderScaleX);
//        animator.addUpdateListener((animation -> {
//            LogUtils.d("FlashView","" + animation.getAnimatedValue());
//            view.setRectAlpha((int)animation.getAnimatedValue());
//        }));
//        animator.setDuration(2000);
//        animator.setTarget(view);
//        animator.start();


        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(view, "rectAlpha", 255, 0);
        objectAnimator.setDuration(1000);
        objectAnimator.start();

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(progressView, "progress", 0, 1);
        objectAnimator2.setInterpolator(new AccelerateInterpolator());
        objectAnimator2.setDuration(5000);
        objectAnimator2.start();


    }
}
