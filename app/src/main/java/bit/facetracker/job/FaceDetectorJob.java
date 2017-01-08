package bit.facetracker.job;

import java.io.File;

import bit.facetracker.constant.URL;
import bit.facetracker.tools.HttpUtils;
import bit.facetracker.tools.LogUtils;

/**
 * Created by blade on 06/01/2017.
 */

public class FaceDetectorJob extends BaseJob {

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        File file = new File("/sdcard/pujing1.jpg");
        String result = HttpUtils.getInstance().requestContainsFile(URL.FACEDETECTORURL, null, null, "img_file", file);
        LogUtils.d("FaceDetecor", "result = "  + result);
    }
}
