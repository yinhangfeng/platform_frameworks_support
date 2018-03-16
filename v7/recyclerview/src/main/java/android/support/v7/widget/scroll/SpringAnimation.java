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

import android.util.AndroidRuntimeException;

public final class SpringAnimation extends DynamicAnimation<SpringAnimation> {

    private SpringForce mSpring = null;
    private float mPendingPosition = UNSET;
    private static final float UNSET = Float.MAX_VALUE;
//    private boolean mEndRequested = false;

    /**
     * Returns the spring that the animation uses for animations.
     *
     * @return the spring that the animation uses for animations
     */
    public SpringForce getSpring() {
        return mSpring;
    }

    /**
     * Uses the given spring as the force that drives this animation. If this spring force has its
     * parameters re-configured during the animation, the new configuration will be reflected in the
     * animation immediately.
     *
     * @param force a pre-defined spring force that drives the animation
     * @return the animation that the spring force is set on
     */
    public SpringAnimation setSpring(SpringForce force) {
        mSpring = force;
        return this;
    }

    @Override
    public void start() {
//        sanityCheck();
        mSpring.setValueThreshold(getValueThreshold());
        super.start();
    }

    /**
     * Updates the final position of the spring.
     * <p/>
     * When the animation is running, calling this method would assume the position change of the
     * spring as a continuous movement since last frame, which yields more accurate results than
     * changing the spring position directly through {@link SpringForce#setFinalPosition(float)}.
     * <p/>
     * If the animation hasn't started, calling this method will change the spring position, and
     * immediately start the animation.
     *
     * @param finalPosition rest position of the spring
     */
//    public void animateToFinalPosition(float finalPosition) {
//        if (isRunning()) {
//            mPendingPosition = finalPosition;
//        } else {
//            if (mSpring == null) {
//                mSpring = new SpringForce(finalPosition);
//            }
//            mSpring.setFinalPosition(finalPosition);
//            start();
//        }
//    }

    /**
     * Skips to the end of the animation. If the spring is undamped, an
     * {@link IllegalStateException} will be thrown, as the animation would never reach to an end.
     * It is recommended to check {@link #canSkipToEnd()} before calling this method. This method
     * should only be called on main thread. If animation is not running, no-op.
     *
     * @throws IllegalStateException if the spring is undamped (i.e. damping ratio = 0)
     * @throws AndroidRuntimeException if this method is not called on the main thread
     */
//    public void skipToEnd() {
//        if (!canSkipToEnd()) {
//            throw new UnsupportedOperationException("Spring animations can only come to an end"
//                    + " when there is damping");
//        }
//        if (Looper.myLooper() != Looper.getMainLooper()) {
//            throw new AndroidRuntimeException("Animations may only be started on the main thread");
//        }
//        if (mRunning) {
//            mEndRequested = true;
//        }
//    }

    /**
     * Queries whether the spring can eventually come to the rest position.
     *
     * @return {@code true} if the spring is damped, otherwise {@code false}
     */
//    public boolean canSkipToEnd() {
//        return mSpring.mDampingRatio > 0;
//    }

    /************************ Below are private APIs *************************/

//    private void sanityCheck() {
//        if (mSpring == null) {
//            throw new UnsupportedOperationException("Incomplete SpringAnimation: Either final"
//                    + " position or a spring force needs to be set.");
//        }
//        double finalPosition = mSpring.getFinalPosition();
//        if (finalPosition > mMaxValue) {
//            throw new UnsupportedOperationException("Final position of the spring cannot be greater"
//                    + " than the max value.");
//        } else if (finalPosition < mMinValue) {
//            throw new UnsupportedOperationException("Final position of the spring cannot be less"
//                    + " than the min value.");
//        }
//    }

    @Override
    boolean updateValueAndVelocity(long deltaT) {
        // If user had requested end, then update the value and velocity to end state and consider
        // animation done.
//        if (mEndRequested) {
//            if (mPendingPosition != UNSET) {
//                mSpring.setFinalPosition(mPendingPosition);
//                mPendingPosition = UNSET;
//            }
//            mValue = mSpring.getFinalPosition();
//            mVelocity = 0;
//            mEndRequested = false;
//            return true;
//        }

        if (mPendingPosition != UNSET) {
            double lastPosition = mSpring.getFinalPosition();
            // Approximate by considering half of the time spring position stayed at the old
            // position, half of the time it's at the new position.
            MassState massState = mSpring.updateValues(mValue, mVelocity, deltaT / 2);
            mSpring.setFinalPosition(mPendingPosition);
            mPendingPosition = UNSET;

            massState = mSpring.updateValues(massState.mValue, massState.mVelocity, deltaT / 2);
            mValue = massState.mValue;
            mVelocity = massState.mVelocity;

        } else {
            MassState massState = mSpring.updateValues(mValue, mVelocity, deltaT);
            mValue = massState.mValue;
            mVelocity = massState.mVelocity;
        }

        mValue = Math.max(mValue, mMinValue);
        mValue = Math.min(mValue, mMaxValue);

        if (isAtEquilibrium(mValue, mVelocity)) {
            mValue = mSpring.getFinalPosition();
            mVelocity = 0f;
            return true;
        }
        return false;
    }

    @Override
    float getAcceleration(float value, float velocity) {
        return mSpring.getAcceleration(value, velocity);
    }

    @Override
    boolean isAtEquilibrium(float value, float velocity) {
        return mSpring.isAtEquilibrium(value, velocity);
    }

    @Override
    void setValueThreshold(float threshold) {
    }
}
