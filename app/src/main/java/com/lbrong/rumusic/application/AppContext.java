package com.lbrong.rumusic.application;

import android.app.Application;
import com.lbrong.rumusic.BuildConfig;
import com.lbrong.rumusic.common.utils.DensityUtils;
import com.tencent.bugly.crashreport.CrashReport;
import org.litepal.LitePal;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/7/17
 */
public class AppContext extends Application {
    private static AppContext INSTANCE;
    private CompositeDisposable compositeDisposable;

    public static AppContext getContext(){
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        compositeDisposable = new CompositeDisposable();

        if(INSTANCE == null){
            INSTANCE = this;
        }

        // 设置适配
        DensityUtils.setDensity(this);

        // bugly建议不要在异步线程中初始化
        initBugly();

        // 延时加载视情况修改，目前不需要
        compositeDisposable.add(
                Observable.timer(0, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.computation())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong){
                                initDB();
                            }
                        }));
    }

    @Override
    public void onTerminate() {
        compositeDisposable.clear();
        super.onTerminate();
    }

    /**
     * 加载数据库
     */
    private void initDB(){
        LitePal.initialize(INSTANCE);
    }

    private void initBugly(){
        CrashReport.initCrashReport(INSTANCE, BuildConfig.BUGLYAPPID, BuildConfig.DEBUG);
    }
}
