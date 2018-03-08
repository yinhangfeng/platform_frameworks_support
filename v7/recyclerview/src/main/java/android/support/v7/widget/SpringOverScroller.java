package android.support.v7.widget;

import android.support.animation.DynamicAnimation;
import android.support.animation.FloatPropertyCompat;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;

class SpringOverScroller {

    private static final float DAMPING_RATIO = 1;
    private static final float STIFFNESS = 120;

    private Scroll mScrollX;
    private Scroll mScrollY;
    private SpringOverScrollListener mSpringOverScrollListener;

    public SpringOverScroller(SpringOverScrollListener l) {
        mScrollX = new Scroll();
        mScrollY = new Scroll();
        mSpringOverScrollListener = l;
    }

    public void start(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        mScrollX.start(startX, velocityX, minX, maxX);
        mScrollY.start(startY, velocityY, minY, maxY);
    }

    public void stop() {
        mScrollX.stop();
        mScrollY.stop();
    }

    public boolean isFinished() {
        return mScrollX.isFinished() && mScrollY.isFinished();
    }

    private void onUpdate() {
        mSpringOverScrollListener.onSpringOverScroll(
                Math.round(mScrollX.mCurrentValue),
                Math.round(mScrollY.mCurrentValue),
                Math.round(mScrollX.mVelocity),
                Math.round(mScrollY.mVelocity),
                isFinished()
        );
    }

    class Scroll extends FloatPropertyCompat<Object> implements
            DynamicAnimation.OnAnimationUpdateListener {

        SpringAnimation mAnimation;
        float mStartSide;
        float mCurrentValue;
        float mVelocity;

        Scroll() {
            super("scroll");
            mAnimation = new SpringAnimation(null, this);
            SpringForce springForce = new SpringForce(0);
            springForce.setDampingRatio(DAMPING_RATIO);
            springForce.setStiffness(STIFFNESS);
            mAnimation.setSpring(springForce);
            mAnimation.addUpdateListener(this);
        }

        void start(float start, int velocity, int min, int max) {
            mCurrentValue = start;
            mVelocity = velocity;
            if (start == 0 && velocity == 0) {
                return;
            }
            if (start == 0) {
                mStartSide = velocity;
            } else {
                mStartSide = start;
            }
            mAnimation.setStartVelocity(velocity);
            mAnimation.setMinValue(min);
            mAnimation.setMaxValue(max);
            mAnimation.start();
        }

        void stop() {
            mAnimation.cancel();
        }

        boolean isFinished() {
            return !mAnimation.isRunning();
        }

        @Override
        public float getValue(Object object) {
            return mCurrentValue;
        }

        @Override
        public void setValue(Object object, float value) {
            mCurrentValue = value;
        }

        @Override
        public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
            mVelocity = velocity;

            // 判断是否已结束
            // 在springback 最后阶段速度很慢 且 abs(value) < 1 此时可以提前结束
            // 1. velocity == 0 或 velocity 向 startValue 到 0 方向 且 abs(value) < 0.5
            // 2. value 处于 startValue 的另一边
            // 3. value 处于0 速度向着 startValue 到 0 方向
            // TODO value 处于 startValue 另一边时的速度会用于接下去的 fling 但由于已处于另一边 速度已不是处于edge 时的速度 如果遇到卡顿 速度甚至可能是回来的速度
            boolean isVelocityBack = velocity * mStartSide <= 0;
            if ((isVelocityBack && Math.abs(value) < 0.5) || (value * mStartSide < 0) || (value == 0 && isVelocityBack)) {
                mCurrentValue = 0;
                mAnimation.cancel();
            }

            onUpdate();
        }
    }

    interface SpringOverScrollListener {
        void onSpringOverScroll(int x, int y, int velocityX, int velocityY, boolean isFinished);
    }
}
