package bit.facetracker.job;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import bit.facetracker.constant.URL;
import bit.facetracker.model.Result;
import bit.facetracker.model.WearResult;
import bit.facetracker.tools.GsonUtils;
import bit.facetracker.tools.HttpUtils;
import bit.facetracker.tools.LogUtils;

/**
 * Created by blade on 06/01/2017.
 */

public class WearJob extends BaseJob {

    public WearJob() {

    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        String result = HttpUtils.getInstance().post(URL.WEARRESULT, null, null);
        WearResult resultObj = GsonUtils.fromJson(result, WearResult.class);
        EventBus.getDefault().post(resultObj);
        LogUtils.d("WearResult", "result = "  + result);
    }
}
