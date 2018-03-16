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

package cn.yinhf.aosp.support.test;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.scroll.OverScroller;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class ScrollerChartActivity extends AppCompatActivity {
    private static final String TAG = "ScrollerChartActivity";

    // 单位 px
    private int NORMAL_VELOCITY;
    private int MIN_L;
    private int MAX_L;
    private int OVER;

    // 图表纵轴坐标单位 dp
    private LineChart mChart;
    private float prevTime = 0;
    private float prevV = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroller_chart);

        float DENSITY = getResources().getDisplayMetrics().density;

        NORMAL_VELOCITY = (int) (2500 * DENSITY);
        MIN_L = (int) (500 * DENSITY);
        MAX_L = (int) (2000 * DENSITY);
        OVER = (int) (500 * DENSITY);

        Log.i(TAG, "onCreate: DENSITY:" + DENSITY + " NORMAL_VELOCITY:" + NORMAL_VELOCITY);

        mChart = (LineChart) findViewById(R.id.line_chart);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line

        LimitLine ll1 = new LimitLine(MAX_L, "Max");
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(MIN_L, "Min");
        ll2.setLineWidth(2f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll2.setTextSize(10f);

        LimitLine ll3 = new LimitLine((MAX_L + OVER), "Over");
        ll3.setLineWidth(2f);
        ll3.enableDashedLine(10f, 10f, 0f);
        ll3.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll3.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.addLimitLine(ll3);
//        leftAxis.setAxisMaximum(MAX_L + OVER + OVER / 10);
//        leftAxis.setAxisMinimum(0f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        YAxis rightAxis = mChart.getAxisRight();
//        rightAxis.setAxisMaximum(NORMAL_VELOCITY * 2);
//        rightAxis.setAxisMinimum(0);

        initLineData();
    }

    private void initLineData() {
        LineData data = new LineData();
        // add empty data
        mChart.setData(data);
    }

    /**
     * @param time 时间
     * @param l 距离
     * @param v 速度
     */
    private void addEntry(float time, float l, float v) {
        Log.d(TAG, "addEntry() called with " + "time = " + time + ", l = " + l + ", v = " + v);

        LineData data = mChart.getData();

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            LineDataSet lset = new LineDataSet(null, "L");
            lset.setAxisDependency(YAxis.AxisDependency.LEFT);
            lset.setColor(Color.BLUE);
            lset.setCircleColor(Color.BLUE);
            lset.setLineWidth(2f);
            lset.setCircleRadius(2f);
            lset.setHighLightColor(Color.rgb(244, 117, 117));
//            lset.setValueTextColor(Color.WHITE);
            lset.setValueTextSize(9f);
            lset.setDrawValues(false);
            lset.setDrawCircleHole(false);
            data.addDataSet(lset);

            LineDataSet vset = new LineDataSet(null, "V");
            vset.setAxisDependency(YAxis.AxisDependency.RIGHT);
            vset.setColor(Color.GREEN);
            vset.setCircleColor(Color.GREEN);
            vset.setLineWidth(2f);
            vset.setCircleRadius(2f);
            vset.setHighLightColor(Color.rgb(244, 117, 117));
//            vset.setValueTextColor(Color.WHITE);
            vset.setValueTextSize(9f);
            vset.setDrawValues(false);
            vset.setDrawCircleHole(false);
            data.addDataSet(vset);

            LineDataSet aset = new LineDataSet(null, "A");
            aset.setAxisDependency(YAxis.AxisDependency.RIGHT);
            aset.setColor(Color.RED);
            aset.setCircleColor(Color.RED);
            aset.setLineWidth(2f);
            aset.setCircleRadius(2f);
            aset.setHighLightColor(Color.rgb(244, 117, 117));
            aset.setValueTextSize(9f);
            aset.setDrawValues(false);
            aset.setDrawCircleHole(false);
            data.addDataSet(aset);
        }


        data.addEntry(new Entry(time, l), 0);
        data.addEntry(new Entry(time, v), 1);

        if (prevTime != 0) {
            data.addEntry(new Entry((time + prevTime) / 2, (v - prevV) / (time - prevTime)), 2);
        } else {
            data.addEntry(new Entry(time, 0), 2);
        }

        prevV = v;
        prevTime = time;

        data.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private void reset() {
        LineData data = mChart.getData();
        data.clearValues();
        mChart.invalidate();
        prevTime = 0;
    }

    private void startAnimation(final OverScroller overScroller, float startL, float startV) {
        final long startTime = AnimationUtils.currentAnimationTimeMillis();
        addEntry(0, startL, startV);

        Runnable runnable = new Runnable() {
            double lastTime = startTime;
            double lastL = 0;
            @Override
            public void run() {
                if (overScroller.computeScrollOffset()) {
                    long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
                    double time = currentAnimationTimeMillis - startTime;
                    double dt = currentAnimationTimeMillis - lastTime;
                    double currL = overScroller.getCurrY();
                    double dl = currL - lastL;

                    lastTime = currentAnimationTimeMillis;
                    lastL = currL;

                    mChart.postOnAnimation(this);
                    addEntry((float) time, overScroller.getCurrY(), overScroller.getCurrVelocity());
                }
            }
        };

        mChart.postOnAnimation(runnable);
    }

    // in => in
    public void test1() {
        reset();
        final OverScroller overScroller = new OverScroller(this);
        int startL = (int) (OVER * 1.1);
        int startV = NORMAL_VELOCITY;
        int minL = MIN_L;
        int maxL = MAX_L;
        int overL = OVER;
        overScroller.fling(0, startL, 0, startV, 0, 0, minL, maxL, 0, overL);

        startAnimation(overScroller, startL, startV);
    }

    // in => over
    public void test2() {
        reset();
        final OverScroller overScroller = new OverScroller(this);
        int startL = MAX_L - OVER;
        int startV = NORMAL_VELOCITY;
        int minL = MIN_L;
        int maxL = MAX_L;
        int overL = OVER;
        overScroller.fling(0, startL, 0, startV, 0, 0, minL, maxL, 0, overL);

        startAnimation(overScroller, startL, startV);
    }

    // over => same over
    public void test3() {
        reset();
        final OverScroller overScroller = new OverScroller(this);
        int startL = 0;
        int startV = 100;
        int minL = MIN_L;
        int maxL = MAX_L;
        int overL = OVER;
        overScroller.fling(0, startL, 0, startV, 0, 0, minL, maxL, 0, overL);

        startAnimation(overScroller, startL, startV);
    }

    // over => in
    public void test4() {
        reset();
        final OverScroller overScroller = new OverScroller(this);
        int startL = OVER / 2;
        int startV = NORMAL_VELOCITY * 2;
        int minL = MIN_L;
        int maxL = MAX_L;
        int overL = OVER;
        overScroller.fling(0, startL, 0, startV, 0, 0, minL, maxL, 0, overL);

        startAnimation(overScroller, startL, startV);
    }

    // over => other over
    public void test5() {
        reset();
        final OverScroller overScroller = new OverScroller(this);
        int startL = OVER / 2;
        int startV = (int) (NORMAL_VELOCITY * 5);
        int minL = MIN_L;
        int maxL = MAX_L;
        int overL = OVER;
        overScroller.fling(0, startL, 0, startV, 0, 0, minL, maxL, 0, overL);

        startAnimation(overScroller, startL, startV);
    }

    public void test6() {
    }

    public void test7() {
        reset();
        final OverScroller overScroller = new OverScroller(this);

        int startL = 0;
        int dl = MAX_L;
        int minL = MIN_L;
        int maxL = MAX_L;
        int overL = OVER;
        int duration = 3000;
        overScroller.startScroll(0, startL, 0, dl, duration);
        float startV = overScroller.getCurrVelocity();

        startAnimation(overScroller, startL, startV);
    }

    public void test8() {

    }

    public void test9() {
        reset();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.test1:
            test1();
            return true;
        case R.id.test2:
            test2();
            return true;
        case R.id.test3:
            test3();
            return true;
        case R.id.test4:
            test4();
            return true;
        case R.id.test5:
            test5();
            return true;
        case R.id.test6:
            test6();
            return true;
        case R.id.test7:
            test7();
            return true;
        case R.id.test8:
            test8();
            return true;
        case R.id.test9:
            test9();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
