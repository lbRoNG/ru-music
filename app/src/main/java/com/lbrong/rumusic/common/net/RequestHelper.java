package com.lbrong.rumusic.common.net;

import com.lbrong.rumusic.BuildConfig;
import com.lbrong.rumusic.common.net.api.ApiService;
import java.util.concurrent.TimeUnit;
import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author lbRoNG
 * @since 2018/7/16
 */
public class RequestHelper {
    private final int CONNECT_TIMEOUT = 30;
    private final int READ_TIMEOUT = 30;

    private static RequestHelper instance;
    private ApiService apiService;

    private RequestHelper() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVERHEAD)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(createOkHttpClient())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
        return ProgressManager.getInstance().with(builder)
                .build();
    }

    public static RequestHelper getInstance() {
        if (instance == null) {
            synchronized (RequestHelper.class) {
                if (instance == null) {
                    instance = new RequestHelper();
                }
            }
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
