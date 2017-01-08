package bit.facetracker.tools;


import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bit.facetracker.AndroidApplication;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by blade on 2/18/16.
 */
public class HttpUtils {

    private static String TAG = HttpUtils.class.getSimpleName();

    private static HttpUtils sInstance;
    private OkHttpClient mOK_HTTP_CLIENT;
    private static final int HTTP_CONNECTION_TIMEOUT = 5000;
    private static final int HTTP_READ_TIMEOUT = 30000;
    CookieManager  mCookieManager;
    private enum RequestType {
        GET, POST, PUT, DELETE,PATCH
    }

    public static synchronized HttpUtils getInstance() {
        if (sInstance == null) {
            sInstance = new HttpUtils();
        }
        return sInstance;
    }

    public static OkHttpClient getHttpClient() {
        return getInstance().mOK_HTTP_CLIENT;
    }

    public static CookieManager getCookieManager() {
        return getInstance().mCookieManager;
    }


    protected HttpUtils() {
//        mCookieManager = new CookieManager(new PersistentCookieStore(AndroidApplication.getInstance()),CookiePolicy.ACCEPT_ALL);
        mOK_HTTP_CLIENT = new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).readTimeout(HTTP_READ_TIMEOUT,TimeUnit.MILLISECONDS).build();
    }


    public String get(String url) {
        return get(url, null, null);
    }

    public String get(String url, Map<String, String> params) {
        return get(url, null, params);
    }


    public String get(String url, Map<String, String> params,boolean isLocal) {
        return get(url, null, params,isLocal);
    }

    /**
     * Http response fail(response code is not 200) return null or success(response code 200) return Response String
     */
    public String get(String url, Map<String, String> headers, Map<String, String> params) {
        return get(url,headers,params,true);
    }

    /**
     * Http response fail(response code is not 200) return null or success(response code 200) return Response String
     * isLocal is third party sdk
     */
    public String get(String url, Map<String, String> headers, Map<String, String> params,boolean isLocal) {

        if (params != null) {
            Set<String> keySet = params.keySet();
            if (!keySet.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                Iterator<String> iterator = keySet.iterator();
                int size = keySet.size();
                for (int i = 0; i < size; i++) {
                    if (i == 0) {
                        if (iterator.hasNext()) {
                            String key = iterator.next();
                            String value = params.get(key);
                            try {
                                sb.append("?").append(URLEncoder.encode(key, "utf-8")).append("=").append(URLEncoder.encode(value, "utf-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (iterator.hasNext()) {
                            String key = iterator.next();
                            String value = params.get(key);
                            try {
                                sb.append("&").append(URLEncoder.encode(key, "utf-8")).append("=").append(URLEncoder.encode(value, "utf-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                url = url + sb.toString();
            }
        }


        Request _request = buildRequest(RequestType.GET, url, headers, null);
        Response _response = null;
        try {
            _response = mOK_HTTP_CLIENT.newCall(_request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_response != null) {
            // TODO define success  according to server dev
            if (_response.code() == 200 || _response.isSuccessful()) {
                try {
                    ResponseBody _body = _response.body();
                    return _body.string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }




    /**
     * no headers
     * @param url
     * @param params
     * @return
     */
    public String post(String url, Map<String, String> params) {
        return post(url,null,params);
    }


    /**
     * Http response fail(response code is not 200) return null or success(response code 200) return Response String
     */
    public String post(String url, Map<String, String> headers, Map<String, String> params) {

        FormBody.Builder _formencodingbuilder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            Set<String> _setkeys = params.keySet();
            Iterator<String> _iteratorkeys = _setkeys.iterator();
            while (_iteratorkeys.hasNext()) {
                String _key = _iteratorkeys.next();
                String _value = params.get(_key);
                _formencodingbuilder.add(_key, _value);
            }
        }

        RequestBody _requestbody = _formencodingbuilder.build();
        Request _request = buildRequest(RequestType.POST, url, headers, _requestbody);
        Response _response = null;
        try {
            _response = mOK_HTTP_CLIENT.newCall(_request).execute();
            if (_response != null) {

                //TODO define success in according to server dev
                if (_response.code() == 200 || _response.isSuccessful()) {
                    return _response.body().string();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * upload ImageFile to server post request
     * ugly design 
     * @param url
     * @param headers
     * @param params
     * @param isLocal 
     * @return
     */
    public String requestContainsFile(String url, Map<String, String> headers, Map<String, String> params, String filekey, File file,boolean isLocal) {
        MultipartBody.Builder _multibuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (params != null && params.size() > 0) {
            Set<String> _setkeys = params.keySet();
            Iterator<String> _iteratorkeys = _setkeys.iterator();
            while (_iteratorkeys.hasNext()) {
                String _key = _iteratorkeys.next();
                String _value = params.get(_key);
                _multibuilder.addFormDataPart(_key, _value);
            }
        }

        if (!TextUtils.isEmpty(filekey) && file != null && file.exists()) {
            // TODO file tagList
            final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
            RequestBody fileBody = RequestBody.create(MEDIA_TYPE_PNG, file);
            _multibuilder.addFormDataPart(filekey, file.getName(), fileBody);
        }

        Request _request = buildRequest(RequestType.POST, url, headers, _multibuilder.build());
        Response _response = null;
        try {
            _response = mOK_HTTP_CLIENT.newCall(_request).execute();

            if (_response != null) {

                //TODO define success in according to server dev
                if (_response.code() == 200 || _response.isSuccessful()) {
                    return _response.body().string();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;

    }


    public String requestContainsFile(String url, Map<String, String> headers, Map<String, String> params, String filekey, File file) {
        return  requestContainsFile(url,headers,params,filekey,file,true);
    }

    /**
     * upload ImageFile to server  post request
     * ugly design
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public String requestContainsFile(String url, Map<String, String> headers, Map<String, String> params, Map<String, File> fileParams) {
        MultipartBody.Builder _multibuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (params != null && params.size() > 0) {
            Set<String> _setkeys = params.keySet();
            Iterator<String> _iteratorkeys = _setkeys.iterator();
            while (_iteratorkeys.hasNext()) {
                String _key = _iteratorkeys.next();
                String _value = params.get(_key);
                _multibuilder.addFormDataPart(_key, _value);
            }
        }
        if (fileParams != null && fileParams.size() > 0) {
            Set<String> _setFileKeys = fileParams.keySet();
            Iterator<String> _iteratorFileKeys = _setFileKeys.iterator();
            while (_iteratorFileKeys.hasNext()) {
                String _key = _iteratorFileKeys.next();
                File _value = fileParams.get(_key);
                if (!TextUtils.isEmpty(_key) && _value != null && _value.exists()) {
                    final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                    RequestBody fileBody = RequestBody.create(MEDIA_TYPE_PNG, _value);
                    _multibuilder.addFormDataPart(_key, _value.getName(), fileBody);
                }
            }
        }

        Request _request = buildRequest(RequestType.POST, url, headers, _multibuilder.build());
        Response _response = null;
        try {
            _response = mOK_HTTP_CLIENT.newCall(_request).execute();

            if (_response != null) {

                //TODO define success in according to server dev
                if (_response.code() == 200 || _response.isSuccessful()) {

//                    return checkLoginState(_response.body().string());
                    return _response.body().string();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;

    }


    /**
     * 上传多张图片
     * ugly design
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public String requestContainsFile_all(String url, Map<String, String> headers, Map<String, String> params,String key , List<String> fileParams) {
        MultipartBody.Builder _multibuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (params != null && params.size() > 0) {
            Set<String> _setkeys = params.keySet();
            Iterator<String> _iteratorkeys = _setkeys.iterator();
            while (_iteratorkeys.hasNext()) {
                String _key = _iteratorkeys.next();
                String _value = params.get(_key);
                _multibuilder.addFormDataPart(_key, _value);
            }
        }
        if (fileParams != null && fileParams.size() > 0) {
            for(int i = 0 ; i < fileParams.size();i++){
                File _value = new File(fileParams.get(i));
//                if ( _value != null && _value.exists()) {
                if ( _value != null ) {
                    final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                    RequestBody fileBody = RequestBody.create(MEDIA_TYPE_PNG, _value);
                    _multibuilder.addFormDataPart(key, _value.getName(), fileBody);
                }
            }
        }

        Request _request = buildRequest(RequestType.POST, url, headers, _multibuilder.build());
        Response _response = null;
        try {
            _response = mOK_HTTP_CLIENT.newCall(_request).execute();

            if (_response != null) {

                //TODO define success in according to server dev
                if (_response.code() == 200 || _response.isSuccessful()) {

                    return _response.body().string();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;

    }

    /**
     * multipar post request(include a file in parameters)
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public String multipartReqeust(String url, Map<String, String> headers, Map<String, String> params) {
        MultipartBody.Builder _multibuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (params != null && params.size() > 0) {
            Set<String> _setkeys = params.keySet();
            Iterator<String> _iteratorkeys = _setkeys.iterator();
            while (_iteratorkeys.hasNext()) {
                String _key = _iteratorkeys.next();
                String _value = params.get(_key);
                _multibuilder.addFormDataPart(_key, _value);
            }
        }

        Request _request = buildRequest(RequestType.POST, url, headers, _multibuilder.build());
        Response _response = null;
        try {
            _response = mOK_HTTP_CLIENT.newCall(_request).execute();

            if (_response != null) {

                //TODO define success in according to server dev
                if (_response.code() == 200 || _response.isSuccessful()) {
                    return _response.body().string();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;

    }



//    public void downFile(final String url, final String tag,final String destFileDir) {
//        final Request request = new Request.Builder().url(url).build();
//        final Call call = mOK_HTTP_CLIENT.newCall(request);
//        final DownFileEvent event = new DownFileEvent();
//        event.init(DownFileJob.DownFileAction.Down_Apk, tag);
//
//        call.enqueue(new Callback() {
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                InputStream is = null;
//                byte[] buf = new byte[2048];
//                int len = 0;
//                FileOutputStream fos = null;
//                try {
//                    is = response.body().byteStream();
//                    File file = new File(destFileDir, getFileName(url));
//                    fos = new FileOutputStream(file);
//                    while ((len = is.read(buf)) != -1) {
//                        fos.write(buf, 0, len);
//                    }
//                    fos.flush();
//                    event.setSuccess(true);
//                    event.setApkName(file.getAbsolutePath());
//                    EventBus.getDefault().post(event);
//                } catch (IOException e) {
//                    android.util.Log.e(TAG,android.util.Log.getStackTraceString(e));
//                    event.setSuccess(false);
//                    EventBus.getDefault().post(event);
//                } finally {
//
//                    try {
//                        if (is != null) is.close();
//                    } catch (IOException e) {
//                    }
//                    try {
//                        if (fos != null) fos.close();
//                    } catch (IOException e) {
//                    }
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                android.util.Log.e(TAG,android.util.Log.getStackTraceString(e));
//                event.setSuccess(false);
//                EventBus.getDefault().post(event);
//            }
//
//        });
//
//    }

    private String getFileName(String path)
    {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    private Request buildRequest(RequestType type, String url, Map<String, String> headers, RequestBody requestBody) {

        Request.Builder builder = new Request.Builder();
        initHttpHeader(builder);
        builder.url(url);

        switch (type) {
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(requestBody);
                break;
            case PUT:
                builder.put(requestBody);
                break;
            case DELETE:
                if (requestBody != null) {
                    builder.delete(requestBody);
                } else {
                    builder.delete();
                }
                break;
            case PATCH:
                builder.patch(requestBody);
                break;
            default:
                break;
        }

        if (headers != null) {
            for(String key : headers.keySet()) {
                builder.addHeader(key, headers.get(key));
            }
        }

        return builder.build();
    }

    /**
     * init http request headers
     *
     * @param builder
     */
    private void initHttpHeader(Request.Builder builder) {
        //TODO necessary headers
        builder.header("uuid", TelephonyUtils.getAndroidID(AndroidApplication.getInstance().getApplicationContext()));
    }


    /**
     * Logout to clean cookies
     */
    public void clearCookie() {
        mCookieManager.getCookieStore().removeAll();
    }

}
