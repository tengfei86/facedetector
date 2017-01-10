package bit.facetracker.job;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import bit.facetracker.constant.URL;
import bit.facetracker.model.Result;
import bit.facetracker.tools.GsonUtils;
import bit.facetracker.tools.HttpUtils;
import bit.facetracker.tools.LogUtils;

/**
 * Created by blade on 06/01/2017.
 */

public class FaceDetectorJob extends BaseJob {

    String filePath;
    public FaceDetectorJob(String filepath) {
        this.filePath = filepath;
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        File file = new File(filePath);
        String result = HttpUtils.getInstance().requestContainsFile(URL.FACEDETECTORURL, null, null, "img_file", file);
        Result resultObj = GsonUtils.fromJson(result, Result.class);
        EventBus.getDefault().post(resultObj);
        LogUtils.d("FaceDetecor", "result = "  + result);
    }
}
