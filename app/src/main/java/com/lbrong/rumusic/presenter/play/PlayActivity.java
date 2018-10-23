package com.lbrong.rumusic.presenter.play;

import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Window;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.view.play.PlayDelegate;

/**
 * @author lbRoNG
 * @since 2018/10/23
 */
public class PlayActivity extends ActivityPresenter<PlayDelegate> {
    @Override
    protected Class<PlayDelegate> getDelegateClass() {
        return PlayDelegate.class;
    }

    @Override
    protected void setting() {
        super.setting();
        // 动画效果
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Transition anim = TransitionInflater.from(this).inflateTransition(R.transition.play_detail_explode);
        getWindow().setExitTransition(anim);
        getWindow().setEnterTransition(anim);
        getWindow().setReenterTransition(anim);
    }
}
