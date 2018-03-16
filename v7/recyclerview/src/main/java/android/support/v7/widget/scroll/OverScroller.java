/*
 * Copyright 2018 The Android Open Source Project
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

package android.support.v7.widget.scroll;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 *
 * 支持Bounce 的OverScroller
 * 部分API 与非Spring 部分实现基于
 * https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/android/widget/OverScroller.java
 * https://github.com/aosp-mirror/platform_frameworks_base/blob/cdac51539809beef498741570ddd4e8ade6c4b8c/core/java/android/widget/OverScroller.java
 * commit 2018-03-13 SHA: 6fc1a2f0d4f6a44d5a0ccc91a1e1c8d152b94879
 *
 * Spring 部分基于
 * https://github.com/aosp-mirror/platform_frameworks_support/blob/master/dynamic-animation/src/main/java/android/support/animation/SpringAnimation.java
 * support-dynamic-animation:27.0.2
 * commit 2018-01-25 SHA: 57f7e35572a20b6ff4bd99fb714e2efcbf8023bb
 */
public class OverScroller {
    private int mMode;

    private final SplineOverScroller mScrollerX;
    private final SplineOverScroller mScrollerY;

    private Interpolator mInterpolator;

    private final boolean mFlywheel;

    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;

    private static final float DEFAULT_DAMPING_RATIO = 1;
    private static final float DEFAULT_STIFFNESS = 120;

    private static final int UNSET = Integer.MAX_VALUE;

    /**
     * Creates an OverScroller with a viscous fluid scroll interpolator and flywheel.
     * @param context
     */
    public OverScroller(Context context) {
        this(context, null);
    }

    /**
     * Creates an OverScroller with flywheel enabled.
     * @param context The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     * be used.
     */
    public OverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    /**
     * Creates an OverScroller.
     * @param context The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     * be used.
     * @param flywheel If true, successive fling motions will keep on increasing scroll speed.
     */
    public OverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        if (interpolator == null) {
            mInterpolator = new ViscousFluidInterpolator();
        } else {
            mInterpolator = interpolator;
        }
        mFlywheel = flywheel;
        mScrollerX = new SplineOverScroller(context);
        mScrollerY = new SplineOverScroller(context);
    }

    void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            mInterpolator = new ViscousFluidInterpolator();
        } else {
            mInterpolator = interpolator;
        }
    }

    /**
     * The amount of friction applied to flings. The default value
     * is {@link ViewConfiguration#getScrollFriction}.
     *
     * @param friction A scalar dimension-less value representing the coefficient of
     *         friction.
     */
    public final void setFriction(float friction) {
        mScrollerX.setFriction(friction);
        mScrollerY.setFriction(friction);
    }

    public final void setStiffness(float stiffness) {
        mScrollerX.setStiffness(stiffness);
        mScrollerY.setStiffness(stiffness);
    }

    /**
     *
     * Returns whether the scroller has finished scrolling.
     *
     * @return True if the scroller has finished scrolling, false otherwise.
     */
    public final boolean isFinished() {
        return mScrollerX.mFinished && mScrollerY.mFinished;
    }

    /**
     * Force the finished field to a particular value. Contrary to
     * {@link #abortAnimation()}, forcing the animation to finished
     * does NOT cause the scroller to move to the final x and y
     * position.
     *
     * @param finished The new finished value.
     */
    public final void forceFinished(boolean finished) {
        mScrollerX.mFinished = mScrollerY.mFinished = finished;
    }

    /**
     * Returns the current X offset in the scroll.
     *
     * @return The new X offset as an absolute distance from the origin.
     */
    public final int getCurrX() {
        return Math.round(mScrollerX.mCurrPosition);
    }

    /**
     * Returns the current Y offset in the scroll.
     *
     * @return The new Y offset as an absolute distance from the origin.
     */
    public final int getCurrY() {
        return Math.round(mScrollerY.mCurrPosition);
    }

    /**
     * Returns the absolute value of the current velocity.
     *
     * @return The original velocity less the deceleration, norm of the X and Y velocity vector.
     */
    public float getCurrVelocity() {
        return (float) Math.hypot(mScrollerX.mCurrVelocity, mScrollerY.mCurrVelocity);
    }

    /**
     * Returns the start X offset in the scroll.
     *
     * @return The start X offset as an absolute distance from the origin.
     */
    public final int getStartX() {
        return mScrollerX.mStart;
    }

    /**
     * Returns the start Y offset in the scroll.
     *
     * @return The start Y offset as an absolute distance from the origin.
     */
    public final int getStartY() {
        return mScrollerY.mStart;
    }

    /**
     * Returns where the scroll will end. Valid only for "fling" scrolls.
     *
     * @return The final X offset as an absolute distance from the origin.
     */
    public final int getFinalX() {
        return mScrollerX.mFinal;
    }

    /**
     * Returns where the scroll will end. Valid only for "fling" scrolls.
     *
     * @return The final Y offset as an absolute distance from the origin.
     */
    public final int getFinalY() {
        return mScrollerY.mFinal;
    }

    /**
     * Call this when you want to know the new location. If it returns true, the
     * animation is not yet finished.
     */
    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }

        switch (mMode) {
            case SCROLL_MODE:
                long time = AnimationUtils.currentAnimationTimeMillis();
                // Any scroller can be used for time, since they were started
                // together in scroll mode. We use X here.
                final long elapsedTime = time - mScrollerX.mStartTime;

                final int duration = mScrollerX.mDuration;
                if (elapsedTime < duration) {
                    final float q = mInterpolator.getInterpolation(elapsedTime / (float) duration);
                    mScrollerX.updateScroll(q);
                    mScrollerY.updateScroll(q);
                } else {
                    abortAnimation();
                }
                break;

            case FLING_MODE:
                if (!mScrollerX.mFinished) {
                    if (!mScrollerX.update()) {
                        mScrollerX.finish();
                    }
                }

                if (!mScrollerY.mFinished) {
                    if (!mScrollerY.update()) {
                        mScrollerY.finish();
                    }
                }

                break;
        }

        return true;
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     * The scroll will use the default value of 250 milliseconds for the
     * duration.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     *        numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     *        will scroll the content up.
     * @param dx Horizontal distance to travel. Positive numbers will scroll the
     *        content to the left.
     * @param dy Vertical distance to travel. Positive numbers will scroll the
     *        content up.
     */
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     *        numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     *        will scroll the content up.
     * @param dx Horizontal distance to travel. Positive numbers will scroll the
     *        content to the left.
     * @param dy Vertical distance to travel. Positive numbers will scroll the
     *        content up.
     * @param duration Duration of the scroll in milliseconds.
     */
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mScrollerX.startScroll(startX, dx, duration);
        mScrollerY.startScroll(startY, dy, duration);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, UNSET, UNSET);
    }

    /**
     * Start scrolling based on a fling gesture. The distance traveled will
     * depend on the initial velocity of the fling.
     *
     * @param startX Starting point of the scroll (X)
     * @param startY Starting point of the scroll (Y)
     * @param velocityX Initial velocity of the fling (X) measured in pixels per
     *            second.
     * @param velocityY Initial velocity of the fling (Y) measured in pixels per
     *            second
     * @param minX Minimum X value. The scroller will not scroll past this point
     *            unless overX > 0. If overfling is allowed, it will use minX as
     *            a springback boundary.
     * @param maxX Maximum X value. The scroller will not scroll past this point
     *            unless overX > 0. If overfling is allowed, it will use maxX as
     *            a springback boundary.
     * @param minY Minimum Y value. The scroller will not scroll past this point
     *            unless overY > 0. If overfling is allowed, it will use minY as
     *            a springback boundary.
     * @param maxY Maximum Y value. The scroller will not scroll past this point
     *            unless overY > 0. If overfling is allowed, it will use maxY as
     *            a springback boundary.
     * @param overX Overfling range. If > 0, horizontal overfling in either
     *            direction will be possible.
     * @param overY Overfling range. If > 0, vertical overfling in either
     *            direction will be possible.
     */
    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY, int overX, int overY) {
        // Continue a scroll or fling in progress
        if (mFlywheel && !isFinished()) {
            float oldVelocityX = mScrollerX.mCurrVelocity;
            float oldVelocityY = mScrollerY.mCurrVelocity;
            if (Math.signum(velocityX) == Math.signum(oldVelocityX) &&
                    Math.signum(velocityY) == Math.signum(oldVelocityY)) {
                velocityX += oldVelocityX;
                velocityY += oldVelocityY;
            }
        }

        mMode = FLING_MODE;
        mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    public void updateHorizontalEdge(int minX, int maxX) {
        mScrollerX.updateEdge(minX, maxX);
    }

    public void updateVerticalEdge(int minY, int maxY) {
        mScrollerX.updateEdge(minY, maxY);
    }

    /**
     * Returns whether the current Scroller is currently returning to a valid position.
     * Valid bounds were provided by the
     * {@link #fling(int, int, int, int, int, int, int, int, int, int)} method.
     *
     * One should check this value before calling
     * {@link #startScroll(int, int, int, int)} as the interpolation currently in progress
     * to restore a valid position will then be stopped. The caller has to take into account
     * the fact that the started scroll will start from an overscrolled position.
     *
     * @return true when the current position is overscrolled and in the process of
     *         interpolating back to a valid value.
     */
    public boolean isOverScrolled() {
        return ((!mScrollerX.mFinished &&
                mScrollerX.mState != SplineOverScroller.SPLINE) ||
                (!mScrollerY.mFinished &&
                        mScrollerY.mState != SplineOverScroller.SPLINE));
    }

    /**
     * Stops the animation. Contrary to {@link #forceFinished(boolean)},
     * aborting the animating causes the scroller to move to the final x and y
     * positions.
     *
     * @see #forceFinished(boolean)
     */
    public void abortAnimation() {
        mScrollerX.finish();
        mScrollerY.finish();
    }

    /**
     * Returns the time elapsed since the beginning of the scrolling.
     *
     * @return The elapsed time in milliseconds.
     *
     */
    public int timePassed() {
        final long time = AnimationUtils.currentAnimationTimeMillis();
        final long startTime = Math.min(mScrollerX.mStartTime, mScrollerY.mStartTime);
        return (int) (time - startTime);
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        final int dx = mScrollerX.mFinal - mScrollerX.mStart;
        final int dy = mScrollerY.mFinal - mScrollerY.mStart;
        return !isFinished() && Math.signum(xvel) == Math.signum(dx) &&
                Math.signum(yvel) == Math.signum(dy);
    }

    static class SplineOverScroller {
        // 所有Initial 成员指的是当前 mState 下的初始值

        // Initial position
        private int mStart;

        // Current position
        private float mCurrPosition;

        // Final position
        private int mFinal;

        private float mPrevPosition;

        // Initial velocity
        private int mVelocity;

        // Current velocity
        private float mCurrVelocity;

        private float mPrevVelocity;

        // Animation starting time, in system milliseconds
        private long mStartTime;

        // Animation duration, in milliseconds
        private int mDuration;

        // Distance to travel along spline animation
        private int mSplineDistance;

        // Whether the animation is currently in progress
        private boolean mFinished;

        private long mTime;

        private int mMin;

        private int mMax;

        // The allowed overshot distance before boundary is reached.
        private int mOver;

        // Fling friction
        private float mFlingFriction = ViewConfiguration.getScrollFriction();

        private float mStiffness = DEFAULT_STIFFNESS;

        // Current state of the animation.
        private int mState = SPLINE;

        // A context-specific coefficient adjusted to physical values.
        private float mPhysicalCoeff;

        private SpringAnimation mSpringAnimation;

        private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
        private static final float START_TENSION = 0.5f;
        private static final float END_TENSION = 1.0f;
        private static final float P1 = START_TENSION * INFLEXION;
        private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

        private static final int NB_SAMPLES = 100;
        private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
        private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];

        private static final int SPLINE = 0;
        private static final int SPRING = 1;

        static {
            float x_min = 0.0f;
            float y_min = 0.0f;
            for (int i = 0; i < NB_SAMPLES; i++) {
                final float alpha = (float) i / NB_SAMPLES;

                float x_max = 1.0f;
                float x, tx, coef;
                while (true) {
                    x = x_min + (x_max - x_min) / 2.0f;
                    coef = 3.0f * x * (1.0f - x);
                    tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
                    if (Math.abs(tx - alpha) < 1E-5) break;
                    if (tx > alpha) x_max = x;
                    else x_min = x;
                }
                SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

                float y_max = 1.0f;
                float y, dy;
                while (true) {
                    y = y_min + (y_max - y_min) / 2.0f;
                    coef = 3.0f * y * (1.0f - y);
                    dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
                    if (Math.abs(dy - alpha) < 1E-5) break;
                    if (dy > alpha) y_max = y;
                    else y_min = y;
                }
                SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
            }
            SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;
        }

        void setFriction(float friction) {
            mFlingFriction = friction;
        }

        void setStiffness(float stiffness) {
            mStiffness = stiffness;
        }

        SplineOverScroller(Context context) {
            mFinished = true;
            final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
            mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                    * 39.37f // inch/meter
                    * ppi
                    * 0.84f; // look and feel tuning
        }

        void updateScroll(float q) {
            mCurrPosition = mStart + q * (mFinal - mStart);
        }

        void startScroll(int start, int distance, int duration) {
            mFinished = false;

            mCurrPosition = mStart = start;
            mFinal = start + distance;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = duration;

            mVelocity = 0;
        }

        void finish() {
            mCurrPosition = mFinal;
            mCurrVelocity = 0.0f;
            mFinished = true;
        }

        void fling(int start, int velocity, int min, int max, int over) {
            mOver = over;
            mFinished = false;
            mCurrVelocity = mPrevVelocity = mVelocity = velocity;
            mDuration = 0;
            mStartTime = mTime = AnimationUtils.currentAnimationTimeMillis();
            mCurrPosition = mPrevPosition = mStart = start;
            mMin = min;
            mMax = max;

            if (start > max || start < min) {
                if (start < min) {
                    mFinal = min;
                } else if (start > max) {
                    mFinal = max;
                }
                startSpringInternal();
                return;
            }

            mState = SPLINE;
            double totalDistance = 0.0;

            if (velocity != 0) {
                mDuration = getSplineFlingDuration(velocity);
                totalDistance = getSplineFlingDistance(velocity);
            }

            mSplineDistance = (int) (totalDistance * Math.signum(velocity));
            mFinal = start + mSplineDistance;
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
        }

        private double getSplineFlingDistance(int velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
        }

        /* Returns the duration, expressed in milliseconds */
        private int getSplineFlingDuration(int velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return (int) (1000.0 * Math.exp(l / decelMinusOne));
        }

        void updateEdge(int min, int max) {
            if (mState == SPLINE) {
                mMin = min;
                mMax = max;
                if (mCurrPosition < max && mCurrPosition > min) {
                    boolean changed = adjustEdgeState();
                    startSpringInternal();

                    if (changed) {
                        update();
                    }
                }
            }
            // TODO SPRING
        }

        private void startFlingInternal() {
            mState = SPLINE;

            mDuration = getSplineFlingDuration(mVelocity);
            double totalDistance = getSplineFlingDistance(mVelocity);

            mSplineDistance = (int) (totalDistance * Math.signum(mVelocity));
            mFinal = mStart + mSplineDistance;
        }

        private void startSpringInternal() {
            mState = SPRING;

            if (mStart >= mMax) {
                mFinal = mMax;
            } else {
                mFinal = mMin;
            }

            if (mSpringAnimation == null) {
                mSpringAnimation = new SpringAnimation();
                mSpringAnimation.setSpring(new SpringForce());
            }

            SpringForce springForce = mSpringAnimation.getSpring();
            springForce.setDampingRatio(DEFAULT_DAMPING_RATIO);
            springForce.setStiffness(mStiffness);
            springForce.setFinalPosition(mFinal);

            mSpringAnimation.setStartValue(mStart);
            mSpringAnimation.setStartVelocity(mVelocity);
            // TODO mOver
//            mSpringAnimation.setMaxValue()
//            mSpringAnimation.setMinValue()
            mSpringAnimation.mLastFrameTime = mStartTime;
            mSpringAnimation.start();
        }

        /**
         * 在超出min 或 max 之后调用
         * 用于SPLINE 与 SPRING 状态的转换时 得到尽量准确的转换点的速度和时间
         * @return 是否被调整
         */
        private boolean adjustEdgeState() {
            // mPrevPosition 到mCurrentPosition 之间短时间内近似为匀变速运动

            Log.i("xxxx", "adjustEdgeState: before mPrevPosition:" + mPrevPosition + " mCurrPosition:" + mCurrPosition
            + " mPrevVelocity:" + mPrevVelocity + " mCurrVelocity:" + mCurrVelocity + " mTime:" + mTime);

            mStartTime = mTime;
            mVelocity = Math.round(mCurrVelocity);

            float edge;
            if (mPrevPosition <= mMin) {
                edge = mMin;
            } else if (mPrevPosition >= mMax) {
                edge = mMax;
            } else if (mCurrPosition < mMin) {
                edge = mMin;
            } else if (mCurrPosition > mMax) {
                edge = mMax;
            } else {
                return false;
            }

            float avgVelocity = (mCurrVelocity + mPrevVelocity) / 2;
            if (avgVelocity * mPrevVelocity <= 0) {
                avgVelocity = mPrevVelocity;
                if (avgVelocity == 0) {
                    return false;
                }
            }

            float distance = mCurrPosition - mPrevPosition;
            float dt = distance / avgVelocity * 1000;
            float overDistance = mCurrPosition - edge;
            float ratio = overDistance / distance;
            mStartTime = mTime = (long) (mTime - dt * ratio);
            mCurrPosition = mStart = (int) edge;
            mCurrVelocity = mVelocity = Math.round(mCurrVelocity - (mCurrVelocity - mPrevVelocity) * ratio);

            Log.i("xxxx", "adjustEdgeState: after mCurrPosition:" + mCurrPosition
                    + " mCurrVelocity:" + mCurrVelocity + " mStartTime:" + mStartTime);

            return true;
        }

        /*
         * Update the current position and velocity for current time. Returns
         * true if update has been done and false if animation duration has been
         * reached.
         */
        boolean update() {
            Log.i("xxxx", "update: dt:" + (AnimationUtils.currentAnimationTimeMillis() - mTime));
            final long time = mTime = AnimationUtils.currentAnimationTimeMillis();
            mPrevVelocity = mCurrVelocity;
            mPrevPosition = mCurrPosition;

            switch (mState) {
                case SPLINE: {
                    final long currentTime = time - mStartTime;

                    if (currentTime == 0) {
                        // Skip work but report that we're still going if we have a nonzero duration.
                        return mDuration > 0;
                    }
                    if (currentTime > mDuration) {
                        return false;
                    }

                    final float t = (float) currentTime / mDuration;
                    final int index = (int) (NB_SAMPLES * t);
                    float distanceCoef = 1.f;
                    float velocityCoef = 0.f;
                    if (index < NB_SAMPLES) {
                        final float t_inf = (float) index / NB_SAMPLES;
                        final float t_sup = (float) (index + 1) / NB_SAMPLES;
                        final float d_inf = SPLINE_POSITION[index];
                        final float d_sup = SPLINE_POSITION[index + 1];
                        velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                        distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                    }

                    double distance = distanceCoef * mSplineDistance;

                    mCurrVelocity = velocityCoef * mSplineDistance / mDuration * 1000.0f;
                    mCurrPosition = (float) (mStart + distance);

                    if (mCurrPosition >= mMax || mCurrPosition <= mMin) {
                        boolean changed = adjustEdgeState();
                        startSpringInternal();

                        if (changed) {
                            update();
                        }
                    }

                    break;
                }

                case SPRING: {
                    boolean finished = mSpringAnimation.doAnimationFrame(time);
                    mCurrVelocity = mSpringAnimation.mVelocity;
                    mCurrPosition = mSpringAnimation.mValue;

                    if (finished) {
                        // spring 回了平衡位置 且速度为0
                        // 有可能是从平衡位置的一边到了另一边又回来了(比如由于掉帧两次update 隔了很久...)
                        return false;
                    }

                    if (mCurrPosition < mMax && mCurrPosition > mMin) {
                        if (mCurrVelocity * mPrevVelocity < 0) {
                            // 从一边到另一边又回来了
                            mCurrVelocity = -mCurrVelocity;
                        }
                        boolean changed = adjustEdgeState();
                        startFlingInternal();

                        if (changed) {
                            update();
                        }

                    }
                    break;
                }
            }

            return true;
        }
    }
}
