package bit.facetracker;

import android.app.Application;
import android.support.annotation.Nullable;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.stetho.Stetho;

import bit.facetracker.manager.JobQueueManager;
import bit.facetracker.manager.PreferenceManager;
import bit.facetracker.net.FrescoOkHttpImagePipelineConfigFactory;
import bit.facetracker.tools.HttpUtils;

/**
 * Created by blade on 06/01/2017.
 */

public class AndroidApplication extends Application {

    private static AndroidApplication sInstance;
    private static final boolean DEBUG = BuildConfig.DEVELOPER_MODE;


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initFresco();
        if (isDebug()) {
            Stetho.initializeWithDefaults(sInstance);
        }
    }

    private void initFresco() {
        ImagePipelineConfig config =  FrescoOkHttpImagePipelineConfigFactory.newBuilder(sInstance, HttpUtils.getHttpClient()).build();
        Fresco.initialize(sInstance);
    }

    @Nullable
    public static AndroidApplication getInstance() {
        return sInstance;
    }

    @Nullable
    public JobQueueManager getJobManager() {
        if (sInstance != null) {
            return JobQueueManager.getJobManager(sInstance);
        }
        return null;
    }

    @Nullable
    public PreferenceManager getPreferenceManager() {
        if (sInstance != null) {
            return PreferenceManager.getInstantce(sInstance);
        }
        return null;
    }

    public boolean isDebug() {
        return DEBUG;
    }
}
