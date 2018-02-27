/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yinhf on 2017/7/13.
 */
public class UltraRecyclerView extends RecyclerView implements NestedScrollingParent {

    NestedScrollingParentHelper mNestedScrollingParentHelper;

    public UltraRecyclerView(Context context) {
        super(context);
    }

    public UltraRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UltraRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private NestedScrollingParentHelper getScrollingParentHelper() {
        if (mNestedScrollingParentHelper == null) {
            mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        }
        return mNestedScrollingParentHelper;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return mLayout != null && (mLayout.canScrollVertically()
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                || mLayout.canScrollHorizontally()
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0);

    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        getScrollingParentHelper().onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        getScrollingParentHelper().onStopNestedScroll(target);
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
            int dyUnconsumed) {
        // TODO
        // 1. scroll
        // 2. dispatchNestedScroll
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            // TODO fling
            return true;
        }
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return getScrollingParentHelper().getNestedScrollAxes();
    }
}
