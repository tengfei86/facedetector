package bit.facetracker.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import bit.facetracker.constant.RequestParamers;
import bit.facetracker.constant.URL;
import bit.facetracker.model.FaceDetectResult;
import bit.facetracker.model.FaceModel;
import bit.facetracker.tools.GsonUtils;
import bit.facetracker.tools.HttpUtils;
import bit.facetracker.tools.LogUtils;

/**
 * Created by blade on 06/01/2017.
 */

public class FaceDetectorJob extends BaseJob {

    String filePath;
    String facilityId;
    String faceRect;


    public FaceDetectorJob(String filepath,String faceRect,String facilityId) {
        this.filePath = filepath;
        this.faceRect = faceRect;
        this.facilityId = facilityId;
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        File file = new File(filePath);
        Map<String, String> params = new HashMap<>();
        params.put(RequestParamers.FaceDetector.FACILITYID,facilityId);
        params.put(RequestParamers.FaceDetector.FACERECT,faceRect);
        String result = HttpUtils.getInstance().requestContainsFile(URL.FACEDETECTORURL, null, params, "image", file);
        FaceDetectResult resultObj = GsonUtils.fromJson(result, FaceDetectResult.class);
        if (resultObj != null) {
            EventBus.getDefault().post(resultObj);
            LogUtils.d("FaceDetecor", "result = "  + result);
        } else {

        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        RetryConstraint constraint = new RetryConstraint(false);
        return constraint;
    }
}
