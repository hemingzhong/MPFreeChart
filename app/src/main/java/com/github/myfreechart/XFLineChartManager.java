package com.github.myfreechart;

import com.acorn.xfreechart.library.dataset.XFreeLineDataSet;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.hamz.hfreechart.library.XFreeLineChart;

public class XFLineChartManager {

    private XFreeLineChart xFreeLineChart;
    private YAxis leftAxis;
    private XAxis xAxis;
    private LineData lineData;
    private XFreeLineDataSet lineDataSet;

    //一条曲线
    public XFLineChartManager(XFreeLineChart mLineChart, String name, int color) {
        this.xFreeLineChart = mLineChart;
        leftAxis = xFreeLineChart.getAxisLeft();
        xFreeLineChart.getAxisRight().setEnabled(false);
        xFreeLineChart.setLogEnabled(true);
        xAxis = xFreeLineChart.getXAxis();
        initLineChart();
        initLineDataSet(name, color);

    }

    /**
     * 初始化LineChar
     */
    private void initLineChart() {

        xFreeLineChart.setDrawGridBackground(true);
        //显示边界
        xFreeLineChart.setDrawBorders(true);
        //折线图例 标签 设置
        Legend legend = xFreeLineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        //显示位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(0.1f);
        xAxis.setLabelCount(8,false);


        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(0.8f);


        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
    }

    /**
     * 初始化折线(一条线)
     *
     * @param name
     * @param color
     */
    private void initLineDataSet(String name, int color) {

        lineDataSet = new XFreeLineDataSet(xFreeLineChart,null,name);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setCircleRadius(1.5f);
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setHighLightColor(color);
        //设置曲线填充
        lineDataSet.setDrawFilled(false);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setMode(XFreeLineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues(false);//不显示Y轴数据值
        //添加一个空的 LineData
        lineData = new LineData();
        xFreeLineChart.setData(lineData);
        xFreeLineChart.invalidate();

    }

    /**
     * 动态添加数据（一条折线图）
     *
     * @param number
     */
    public void addEntry(float xValue,float number) {

        //最开始的时候才添加 lineDataSet（一个lineDataSet 代表一条线）
        if (lineDataSet.getEntryCount() == 0) {
            lineData.addDataSet(lineDataSet);
        }
//        lineChart.setData(lineData);

        Entry entry = new Entry(xValue, number);
        lineData.addEntry(entry, 0);
        //通知数据已经改变
        lineData.notifyDataChanged();
        xFreeLineChart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
//        lineChart.setVisibleXRangeMaximum(10);
        //移到某个位置
//        lineChart.moveViewToX(lineData.getEntryCount() - 5);
    }

    /**
     * 设置Y轴值
     *
     * @param max
     * @param min
     * @param labelCount
     */
    public void setYAxis(float max, float min, int labelCount) {
        if (max < min) {
            return;
        }
        leftAxis.setAxisMaximum(max);
        leftAxis.setAxisMinimum(min);
        leftAxis.setLabelCount(labelCount, false);

        xFreeLineChart.invalidate();
    }

    /**
     * 设置高限制线
     *
     * @param high
     * @param name
     */
    public void setHightLimitLine(float high, String name, int color) {
        if (name == null) {
            name = "高限制线";
        }
        LimitLine hightLimit = new LimitLine(high, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        hightLimit.setLineColor(color);
        hightLimit.setTextColor(color);
        leftAxis.addLimitLine(hightLimit);
        xFreeLineChart.invalidate();
    }

    /**
     * 设置低限制线
     *
     * @param low
     * @param name
     */
    public void setLowLimitLine(int low, String name, int color) {
        if (name == null) {
            name = "低限制线";
        }
        LimitLine hightLimit = new LimitLine(low, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        hightLimit.setTextColor(color);
        hightLimit.setLineColor(color);
        leftAxis.addLimitLine(hightLimit);
        xFreeLineChart.invalidate();
    }

    /**
     * 设置描述信息
     *
     * @param str
     */
    public void setDescription(String str) {
        Description description = new Description();
        description.setText(str);
        xFreeLineChart.setDescription(description);
        xFreeLineChart.invalidate();
    }

}
