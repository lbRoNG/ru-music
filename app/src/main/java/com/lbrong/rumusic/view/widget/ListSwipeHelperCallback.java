package com.lbrong.rumusic.view.widget;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/8/21
 * 列表item移动调整位置，滑动删除
 */
public abstract class ListSwipeHelperCallback<T> extends ItemTouchHelper.Callback {
    // 数据
    private List<T> data;

    protected ListSwipeHelperCallback(List<T> data) {
        this.data = data;
    }

    /**
     * 通知更新
     */
    public abstract void notifyDismiss(int pos, T item);

    /**
     * 通知更新
     */
    public abstract void notifyMove(int fromPosition, int toPosition, T fromItem, T toItem);

    /**
     * 选中背景
     */
    public @ColorInt int selectBg(){
        return Color.parseColor("#a2ffffff");
    }

    /**
     * 默认背景
     */
    public @ColorInt int defaultBg(){
        return Color.parseColor("#ffffff");
    }

    /**
     * 支持的滑动类型
     */
    protected int swipeFlags() {
        return ItemTouchHelper.LEFT;
    }

    /**
     * 支持拖拽类型
     */
    protected int dragFlags(){
        return ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // 处理事件传递
        if (recyclerView instanceof SwipeRecyclerView) {
            ((SwipeRecyclerView) recyclerView).setInterceptTouch(true);
        }

        if (recyclerView.getLayoutManager() instanceof GridLayoutManager || recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            return makeMovementFlags(dragFlags, 0);
        }

        // 支持的事件
        return makeMovementFlags(dragFlags(), swipeFlags());
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        notifyMove(from,to,data.get(from),data.get(to));
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT || direction == ItemTouchHelper.LEFT) {
            int targetPos = viewHolder.getAdapterPosition();
            notifyDismiss(targetPos, data.get(targetPos));
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setBackgroundColor(selectBg());
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setBackgroundColor(defaultBg());
    }
}
