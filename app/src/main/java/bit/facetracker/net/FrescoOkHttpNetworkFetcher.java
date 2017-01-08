package bit.facetracker.net;

/**
 * Created by blade on 03/11/2016.
 */

import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;

import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.ProducerContext;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import bit.facetracker.tools.HttpUtils;
import bit.facetracker.tools.LogUtils;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 * <p>
 * Created by admin on 2016/1/8.
 * <p>
 * Network fetcher that uses OkHttp as a backend.
 */
public class FrescoOkHttpNetworkFetcher extends BaseNetworkFetcher<FrescoOkHttpNetworkFetcher.MyOkHttpNetworkFetchState> {

    public static class MyOkHttpNetworkFetchState extends FetchState {

        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        public MyOkHttpNetworkFetchState(Consumer<EncodedImage> consumer, ProducerContext context) {
            super(consumer, context);
        }

    }

    private static final String TAG = "MyOkHttpNetworkFetcher";

    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";

    private final OkHttpClient mOkHttpClient;

    private Executor mCancellationExecutor;

    public FrescoOkHttpNetworkFetcher(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
        mCancellationExecutor = okHttpClient.dispatcher().executorService();
    }

    @Override
    public MyOkHttpNetworkFetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        return new MyOkHttpNetworkFetchState(consumer, producerContext);
    }

    @Override
    public void fetch(final MyOkHttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();

        final Uri uri = fetchState.getUri();

        Request.Builder builder = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .url(uri.toString())
                .get();

        StringBuffer cookies = new StringBuffer();
        for (HttpCookie cookie : HttpUtils.getCookieManager().getCookieStore().getCookies()) {
            cookies.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        }

        builder.addHeader("Cookie", cookies.toString());

        final Request request = builder.build();
        final Call call = mOkHttpClient.newCall(request);

        fetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() {
            @Override
            public void onCancellationRequested() {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    call.cancel();
                } else {
                    mCancellationExecutor.execute(call::cancel);
                }
            }
        });

        call.enqueue(new okhttp3.Callback() {
            /**
             * Called when the request could not be executed due to cancellation, a connectivity problem or
             * timeout. Because networks can fail during an exchange, it is possible that the remote server
             * accepted the request before the failure.
             *
             * @param call
             * @param e
             */
            @Override
            public void onFailure(Call call, IOException e) {
                handleException(call, e, callback);
            }

            /**
             * Called when the HTTP response was successfully returned by the remote server. The callback may
             * proceed to read the response body with {@link Response#body}. The response is still live until
             * its response body is closed with {@code response.body().close()}. The recipient of the callback
             * may even consume the response body on another thread.
             * <p>
             * <p>Note that transport-layer success (receiving a HTTP response code, headers and body) does
             * not necessarily indicate application-layer success: {@code response} may still indicate an
             * unhappy HTTP response code like 404 or 500.
             *
             * @param call
             * @param response
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                fetchState.responseTime = SystemClock.elapsedRealtime();
                final ResponseBody body = response.body();
                try {
                    long contentLength = body.contentLength();
                    if (contentLength < 0) {
                        contentLength = 0;
                    }
                    callback.onResponse(body.byteStream(), (int) contentLength);
                } catch (Exception e) {
                    handleException(call, e, callback);
                } finally {
                    try {
                        body.close();
                    } catch (Exception e) {
                        LogUtils.d(TAG, "Exception when closing response body");
                    }
                }
            }

        });

    }

    @Override
    public void onFetchCompletion(MyOkHttpNetworkFetchState fetchState, int byteSize) {
        fetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }

    @Override
    public Map<String, String> getExtraMap(MyOkHttpNetworkFetchState fetchState, int byteSize) {
        Map<String, String> extraMap = new HashMap<>(4);
        extraMap.put(QUEUE_TIME, Long.toString(fetchState.responseTime - fetchState.submitTime));
        extraMap.put(FETCH_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.responseTime));
        extraMap.put(TOTAL_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
        extraMap.put(IMAGE_SIZE, Integer.toString(byteSize));
        return extraMap;
    }

    /**
     * Handles exceptions.
     * <p>
     * <p> OkHttp notifies callers of cancellations via an IOException. If IOException is caught
     * after request cancellation, then the exception is interpreted as successful cancellation
     * and onCancellation is called. Otherwise onFailure is called.
     */
    private void handleException(final Call call, final Exception e, final Callback callback) {
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }
    }

}
