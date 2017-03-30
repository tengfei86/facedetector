package bit.facetracker.job;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import bit.facetracker.constant.RequestParamers;
import bit.facetracker.constant.URL;
import bit.facetracker.model.WearResult;
import bit.facetracker.tools.GsonUtils;
import bit.facetracker.tools.HttpUtils;
import bit.facetracker.tools.LogUtils;

/**
 * Created by blade on 06/01/2017.
 */

public class WearJob extends BaseJob {

    // 0 female 1 male
    public int gender;

    public WearJob(int gender) {
        this.gender = gender;
    }

    public WearJob() {

    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        Map<String, String> params = new HashMap<>();
        params.put(RequestParamers.WearRecommend.GENDER, String.valueOf(gender));
        String result = HttpUtils.getInstance().post(URL.WEARRESULT, null, params);
        WearResult resultObj = GsonUtils.fromJson(result, WearResult.class);
        EventBus.getDefault().post(resultObj);
        LogUtils.d("WearResult", "result = "  + result);
    }
}
