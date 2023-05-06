package com.github.myfreechart;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.data.Entry;
import com.hamz.hfreechart.library.XFreeLineChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private List<Entry> entryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        XFreeLineChart xFreeLineChart = (XFreeLineChart)findViewById(R.id.xf_lineChart);
        Button bt_add = (Button)findViewById(R.id.bt_add);

        XFLineChartManager xfLineChartManager = new XFLineChartManager(xFreeLineChart, "电压", Color.CYAN);
        xfLineChartManager.setYAxis(15, -15, 6);
        xfLineChartManager.setDescription("电位仪采集");

        entryList.add(new Entry(0f, -1.56f));
        entryList.add(new Entry(0.1f, -0.33f));
        entryList.add(new Entry(0.2f, 3.68f));
        entryList.add(new Entry(0.3f, 13.15f));
        entryList.add(new Entry(0.4f, 5.11f));
        entryList.add(new Entry(0.5f, 2.98f));
        entryList.add(new Entry(0.6f, 1.84f));
        entryList.add(new Entry(0.5f, 0.93f));
        entryList.add(new Entry(0.4f, -0.43f));
        entryList.add(new Entry(0.3f, -9.02f));
        entryList.add(new Entry(0.2f, -8.78f));
        entryList.add(new Entry(0.1f, -3.63f));
        entryList.add(new Entry(0.0f, -2.27f));
        for (int i = 0; i < entryList.size(); i++) {
            xfLineChartManager.addEntry(entryList.get(i).getX(), entryList.get(i).getY());
        }


        //动态新增--示例
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entryList.add(new Entry(new Random().nextFloat(), 1f + new Random().nextFloat()));
                XFLineChartManager xfLineChartManager = new XFLineChartManager(xFreeLineChart, "电压", Color.CYAN);
                xfLineChartManager.setYAxis(15, -15, 6);
                xfLineChartManager.setDescription("电位仪采集");
                for (int i = 0; i < entryList.size(); i++) {
                    xfLineChartManager.addEntry(entryList.get(i).getX(), entryList.get(i).getY());
                }
            }
        });


    }
}