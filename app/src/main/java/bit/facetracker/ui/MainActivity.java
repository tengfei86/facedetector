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
    private static final float TIME_DISPLAYATTATIVE = 200;
    private static final float TIME_DISPLAYAGE = 267;
    private static final float TIME_DISPLAYCHARM = 333;
    private static final float TIME_MATCHSTARIMAGE = 1000;
    private static final float TIME_MATCHSTARNAME = 1100;
    private static final float TIME_QUITFIRST = 5033;


    private static final float TIME_IMAGEMAIN = 1533;
    private static final float TIME_IMAGESIDE1 = 1666;
    private static final float TIME_IMAGESIDE2 = 1800;
    private static final float TIME_IMAGESIDE3 = 1933;
    private static final float TIME_IMAGESIDE4 = 2067;



    private Map<Float, Boolean> mIsMarked = new HashMap<>();
    private Map<Float, Boolean> mImageMarked = new HashMap<>();


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


    // second result
    @BindView(R.id.image_main)
    CustomDraweeView mMainImage;

    @BindView(R.id.image_side1)
    CustomDraweeView mSide1Image;

    @BindView(R.id.image_side2)
    CustomDraweeView mSide2Image;

    @BindView(R.id.image_side3)
    CustomDraweeView mSide3Image;

    @BindView(R.id.image_side4)
    CustomDraweeView mSide4Image;


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

            if (animation.getCurrentPlayTime() >= TIME_MATCHSTARIMAGE && !mIsMarked.get(TIME_MATCHSTARIMAGE)) {
                mIsMarked.put(TIME_MATCHSTARIMAGE, true);
                mStarView.setVisibility(View.VISIBLE);
                mStarView.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490524083796&di=03286777e18a55b3e39e4a66dd8f0992&imgtype=0&src=http%3A%2F%2Fscimg.jb51.net%2Ftouxiang%2F201703%2F2017032517032229.jpg");
            }


            if (animation.getCurrentPlayTime() >= TIME_MATCHSTARNAME && !mIsMarked.get(TIME_MATCHSTARNAME)) {
                mIsMarked.put(TIME_MATCHSTARNAME, true);
                mMatchStartTextContainer.setDisplayText("撞脸明星 方大同");
            }



            if (animation.getCurrentPlayTime() >= TIME_QUITFIRST && !mIsMarked.get(TIME_QUITFIRST)) {
                mIsMarked.put(TIME_QUITFIRST,true);
                firstQuit();
            }


//            if (animation.getCurrentPlayTime() >= TIME_IMAGEMAIN && !mImageMarked.get(TIME_IMAGEMAIN)) {
//                mImageMarked.put(TIME_IMAGEMAIN, true);
//                mMainImage.setVisibility(View.VISIBLE);
//                mMainImage.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490512396376&di=bdbb723411b0ab542ae61f036cef315e&imgtype=0&src=http%3A%2F%2Fpic36.photophoto.cn%2F20150707%2F0047045135399298_b.jpg");
//            }
//
//            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE1 && !mImageMarked.get(TIME_IMAGESIDE1)) {
//                mImageMarked.put(TIME_IMAGESIDE1, true);
//
//                mSide1Image.setVisibility(View.VISIBLE);
//                mSide1Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490520865259&di=116bf1d4c4673b755a46c440e079f345&imgtype=0&src=http%3A%2F%2Ffile06.16sucai.com%2F2016%2F0921%2Fda78bbfe5a27798a8d300f30d5ad594e.jpg");
//            }
//
//            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE2 && !mImageMarked.get(TIME_IMAGESIDE2)) {
//                mImageMarked.put(TIME_IMAGESIDE2, true);
//                mSide2Image.setVisibility(View.VISIBLE);
//                mSide2Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490520885166&di=a09e54d037b48b201b5667118cf99981&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2015%2Fxll%2F05%2F8%2Fstar29.jpg");
//            }
//
//            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE3 && !mImageMarked.get(TIME_IMAGESIDE3)) {
//                mImageMarked.put(TIME_IMAGESIDE3, true);
//                mSide3Image.setVisibility(View.VISIBLE);
//                mSide3Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1490510814&di=9f9ddb699f4c03feffeb03cb71d48efa&src=http://img02.tooopen.com/images/20150703/tooopen_sy_132761691991.jpg");
//            }
//
//            if (animation.getCurrentPlayTime() >= TIME_IMAGESIDE4 && !mImageMarked.get(TIME_IMAGESIDE4)) {
//                mImageMarked.put(TIME_IMAGESIDE4, true);
//                mSide4Image.setVisibility(View.VISIBLE);
//                mSide4Image.setImageURI("https://timgsa.baidu.com/timg?image&quality=80&size=b10000_10000&sec=1490510837&di=a6c87767b4eb689df8396c57796d2111&src=http://5.66825.com/download/pic/000/326/d7b6e3f5f063dfbeec1635627988aa48.jpg");
//            }



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
        mIsMarked.put(TIME_MATCHSTARIMAGE, false);
        mIsMarked.put(TIME_MATCHSTARNAME, false);
        mIsMarked.put(TIME_QUITFIRST, false);

        mImageMarked.put(TIME_IMAGEMAIN, false);
        mImageMarked.put(TIME_IMAGESIDE1, false);
        mImageMarked.put(TIME_IMAGESIDE2, false);
        mImageMarked.put(TIME_IMAGESIDE3, false);
        mImageMarked.put(TIME_IMAGESIDE4, false);

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

        mAttractiveProgressView.setTextVisible(false);
        mAgeProgressView.setTextVisible(false);
        mCharmProgressView.setTextVisible(false);

        createQuitAnimationSet();
    }


}
