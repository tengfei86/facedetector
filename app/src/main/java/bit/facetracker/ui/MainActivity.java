package bit.facetracker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bit.facetracker.R;
import bit.facetracker.tools.LogUtils;
import bit.facetracker.ui.widget.CustomDraweeView;
import bit.facetracker.ui.widget.ProgressView;
import bit.facetracker.ui.widget.TextContainer;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.background_animation)
    LottieAnimationView animationView;

    // hash key
    private static final float TIME_DISPLAYATTATIVE = 100;
    private static final float TIME_DISPLAYAGE = 200;
    private static final float TIME_DISPLAYCHARM = 300;
    private static final float TIME_MATCHSTAR = 1000;
    private static final float TIME_QUITFirst = 5000;

    private Map<Float, Boolean> mIsMarked = new HashMap<>();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        animationView.playAnimation();

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

            if (animation.getCurrentPlayTime() >= TIME_MATCHSTAR && !mIsMarked.get(TIME_MATCHSTAR)) {
                mIsMarked.put(TIME_MATCHSTAR, true);
                mMatchStartTextContainer.setDisplayText("撞脸明星 方大同");
            }


            if (animation.getCurrentPlayTime() >= TIME_QUITFirst && !mIsMarked.get(TIME_QUITFirst)) {
                mIsMarked.put(TIME_QUITFirst,true);
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


    }


    private void init() {
        mIsMarked.put(TIME_DISPLAYATTATIVE, false);
        mIsMarked.put(TIME_DISPLAYAGE, false);
        mIsMarked.put(TIME_DISPLAYCHARM, false);
        mIsMarked.put(TIME_MATCHSTAR, false);
        mIsMarked.put(TIME_QUITFirst, false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void createQuitAnimationSet() {
        ObjectAnimator selfAvatarAnimator = ObjectAnimator.ofFloat(mSelfAvatar,"alpha",1.0f,0.0f);
        ObjectAnimator matchStartAnimator = ObjectAnimator.ofFloat(mStarView,"alpha",1.0f,0.0f);
        ObjectAnimator attractiveprogressviewanimator = ObjectAnimator.ofFloat(mAttractiveProgressView,"alpha",1.0f,0.0f);
        ObjectAnimator ageprogressviewanimator = ObjectAnimator.ofFloat(mAgeProgressView,"alpha",1.0f,0.0f);
        ObjectAnimator charmprogressviewanimator = ObjectAnimator.ofFloat(mCharmProgressView,"alpha",1.0f,0.0f);
        List<Animator> animators = new ArrayList<>();
        animators.add(selfAvatarAnimator);
        animators.add(attractiveprogressviewanimator);
        animators.add(ageprogressviewanimator);
        animators.add(charmprogressviewanimator);
        animators.add(matchStartAnimator);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.playSequentially(animators);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

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
        createQuitAnimationSet();
    }


}
