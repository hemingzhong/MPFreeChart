## MPFreeChart
当我们使用 MPAndroidChart 时提示 java.lang.NegativeArraySizeException

# 问题处理步骤

MPFreeChart 继承自 [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart),实现此库的目的是解决[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)的LineDataChart的X轴无法添加未排序数据(只能添加递增的x轴数据)的问题
当添加未排序数据到[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)时,会报如下错误

```java

    E/AndroidRuntime: FATAL EXCEPTION: main
        Process: com.acorn.myframeapp, PID: 13758
        java.lang.NegativeArraySizeException: -2
            at com.github.mikephil.charting.utils.Transformer.generateTransformedValuesLine(Transformer.java:178)
            at com.github.mikephil.charting.renderer.LineChartRenderer.drawValues(LineChartRenderer.java:567)
            at com.github.mikephil.charting.charts.BarLineChartBase.onDraw(BarLineChartBase.java:297)

```

## 用图说话
数据：
```data
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
```

![github](https://github.com/hemingzhong/MPFreeChart/blob/main/freechart.png)




### Gradle


#### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of the repositories:

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

#### Step 2. Add the dependency

```groovy
	dependencies {
	        implementation 'com.github.hemingzhong:MPFreeChart:v1.0.4'
	}
```

## Usage

MPFreeChart大多数使用方式和[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)相同.

不同点如下

1. Create the instance object of chart view
```xml
    <com.hamz.hfreechart.library.XFreeLineChart
            android:id="@+id/xf_lineChart"
            android:layout_width="match_parent"
            android:layout_height="280dp"/>
  ```

2. 创建XFLineChartManager管理类

3. 添加点位信息

```activity
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
```