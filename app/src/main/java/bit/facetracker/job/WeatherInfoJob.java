package bit.facetracker.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import bit.facetracker.constant.Config;
import bit.facetracker.constant.RequestParamers;
import bit.facetracker.constant.URL;
import bit.facetracker.model.FaceDetectResult;
import bit.facetracker.model.WeatherModel;
import bit.facetracker.tools.GsonUtils;
import bit.facetracker.tools.HttpUtils;
import bit.facetracker.tools.LogUtils;

/**
 * Created by blade on 06/01/2017.
 */

public class WeatherInfoJob extends BaseJob {

    String cityId;

    public WeatherInfoJob(String cityId) {
        this.cityId = cityId;
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        Map<String, String> params = new HashMap<>();
        params.put(RequestParamers.Weather.APPID, Config.OPENWEATHER_APIKEY);
        params.put(RequestParamers.Weather.UNIT, Config.WEATHER_METRICUNIT);
        params.put(RequestParamers.Weather.LANG, "zh_cn");
        params.put(RequestParamers.Weather.CITYID,cityId);
        String result = HttpUtils.getInstance().get(URL.WEATHERURL, null, params);
        WeatherModel resultObj = GsonUtils.fromJson(result, WeatherModel.class);

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
