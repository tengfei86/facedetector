package bit.facetracker.ui.widget;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by blade on 3/6/16.
 */
public class CustomDraweeView extends SimpleDraweeView {

    private static final int FADETIME_DURATION = 300;

    public CustomDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        //TODO
//        hierarchy.setFadeDuration(FADETIME_DURATION);
    }

    public CustomDraweeView(Context context) {
        super(context);
    }

    public CustomDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
    }

    public void setImageURI(String uri) {
        setImageURI(uri, null,null,null);
    }

    public void setImageURI(String uri, BaseControllerListener listener) {
        RoundingParams params;
        if (null != getHierarchy() && null != getHierarchy().getRoundingParams()) {
            params = getHierarchy().getRoundingParams().setRoundAsCircle(false);
        } else {
            params = new RoundingParams().setRoundAsCircle(false);
        }
        setImageURI(uri,listener,params,null);
    }

    public void setImageURI(String uri, BaseControllerListener listener, RoundingParams params, ResizeOptions options) {

        if (!TextUtils.isEmpty(uri)) {
            getHierarchy().setRoundingParams(params);
            ImageRequest request = null;
            if (options == null) {
                request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                        .setProgressiveRenderingEnabled(true)
                        .setAutoRotateEnabled(true)
                        .build();
            } else {
                request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                        .setProgressiveRenderingEnabled(true)
                        .setResizeOptions(options)
                        .setAutoRotateEnabled(true)
                        .build();
            }

            DraweeController controller = null;
            if (listener != null) {
                controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setControllerListener(listener)
                        .setOldController(getController())
                        .setAutoPlayAnimations(true)
                        .build();
            } else {
                controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setOldController(getController())
                        .setAutoPlayAnimations(true)
                        .build();
            }

            setController(controller);
        }
    }

    public void setImageURI(String uri, ResizeOptions options) {
        setImageURI(uri,null,null,options);
    }

    public void setImageURI(String uri, float radius) {
        RoundingParams params;
        if (null != getHierarchy() && null != getHierarchy().getRoundingParams()) {
            params = getHierarchy().getRoundingParams().fromCornersRadius(radius);
        } else {
            params = RoundingParams.fromCornersRadius(radius);
        }
        setImageURI(uri,null,params,null);
    }

    public void setCircleImageURI(String uri, BaseControllerListener listener) {
        RoundingParams params;
        if (null != getHierarchy() && null != getHierarchy().getRoundingParams()) {
            params = getHierarchy().getRoundingParams().setRoundAsCircle(true);
        } else {
            params = RoundingParams.asCircle();
        }
        setImageURI(uri,listener,params,null);
    }

    public void setCircleImageURI(String uri) {
        setCircleImageURI(uri, null);
    }

    public void setCircleImageURI(String uri, int borderWidth, @ColorRes int colorId) {
        RoundingParams params;
        if (null != getHierarchy() && null != getHierarchy().getRoundingParams()) {
            params = getHierarchy().getRoundingParams().setRoundAsCircle(true);
        } else {
            params = RoundingParams.asCircle();
        }
        params.setBorder(getResources().getColor(colorId), borderWidth);
        getHierarchy().setRoundingParams(params);
        setCircleImageURI(uri);
    }

    public void setImage(int resId, float radius) {
        RoundingParams params;
        if (null != getHierarchy() && null != getHierarchy().getRoundingParams()) {
            params = getHierarchy().getRoundingParams().fromCornersRadius(radius);
        } else {
            params = RoundingParams.fromCornersRadius(radius);
        }
        setImageURI(resId, null, params, null);
    }


    public void setImage(int resId) {
        RoundingParams params;
        if (null != getHierarchy() && null != getHierarchy().getRoundingParams()) {
            params = getHierarchy().getRoundingParams().setRoundAsCircle(false);
        } else {
            params = new RoundingParams().setRoundAsCircle(false);
        }
        setImageURI(resId, null, params, null);
    }

    public void setCircleImage(int resId) {
        RoundingParams params;
        if (null != getHierarchy() && null != getHierarchy().getRoundingParams()) {
            params = getHierarchy().getRoundingParams().setRoundAsCircle(true);
        } else {
            params = RoundingParams.asCircle();
        }
        setImageURI(resId, null, params, null);
    }

    public void setImageURI(int resId, BaseControllerListener listener, RoundingParams params, ResizeOptions options) {
        getHierarchy().setRoundingParams(params);
        ImageRequest request = null;
        if (options != null) {
            request = ImageRequestBuilder.newBuilderWithResourceId(resId)
                    .setProgressiveRenderingEnabled(true)
                    .build();
        } else {
            request = ImageRequestBuilder.newBuilderWithResourceId(resId)
                    .setProgressiveRenderingEnabled(true).setResizeOptions(options)
                    .build();
        }

        DraweeController controller = null;
        if (listener != null) {
            controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setControllerListener(listener)
                    .setOldController(getController())
                    .setAutoPlayAnimations(true)
                    .build();
        } else {
            controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(getController())
                    .setAutoPlayAnimations(true)
                    .build();
        }

        setController(controller);
    }

}
