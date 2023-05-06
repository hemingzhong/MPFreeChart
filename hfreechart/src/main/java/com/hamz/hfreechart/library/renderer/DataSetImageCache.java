package com.hamz.hfreechart.library.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

class DataSetImageCache {

    private Path mCirclePathBuffer = new Path();

    private Bitmap[] circleBitmaps;
    private final Paint mRenderPaint;
    private final Paint mCirclePaintInner;

    public DataSetImageCache(Paint mRenderPaint, Paint mCirclePaintInner) {
        this.mRenderPaint = mRenderPaint;
        this.mCirclePaintInner = mCirclePaintInner;
    }

    /**
     * Sets up the cache, returns true if a change of cache was required.
     *
     * @param set
     * @return
     */
    protected boolean init(ILineDataSet set) {

        int size = set.getCircleColorCount();
        boolean changeRequired = false;

        if (circleBitmaps == null) {
            circleBitmaps = new Bitmap[size];
            changeRequired = true;
        } else if (circleBitmaps.length != size) {
            circleBitmaps = new Bitmap[size];
            changeRequired = true;
        }

        return changeRequired;
    }

    /**
     * Fills the cache with bitmaps for the given dataset.
     *
     * @param set
     * @param drawCircleHole
     * @param drawTransparentCircleHole
     */
    protected void fill(ILineDataSet set, boolean drawCircleHole, boolean drawTransparentCircleHole) {

        int colorCount = set.getCircleColorCount();
        float circleRadius = set.getCircleRadius();
        float circleHoleRadius = set.getCircleHoleRadius();

        for (int i = 0; i < colorCount; i++) {

            Bitmap.Config conf = Bitmap.Config.ARGB_4444;
            Bitmap circleBitmap = Bitmap.createBitmap((int) (circleRadius * 2.1), (int) (circleRadius * 2.1), conf);

            Canvas canvas = new Canvas(circleBitmap);
            circleBitmaps[i] = circleBitmap;
            mRenderPaint.setColor(set.getCircleColor(i));

            if (drawTransparentCircleHole) {
                // Begin path for circle with hole
                mCirclePathBuffer.reset();

                mCirclePathBuffer.addCircle(
                        circleRadius,
                        circleRadius,
                        circleRadius,
                        Path.Direction.CW);

                // Cut hole in path
                mCirclePathBuffer.addCircle(
                        circleRadius,
                        circleRadius,
                        circleHoleRadius,
                        Path.Direction.CCW);

                // Fill in-between
                canvas.drawPath(mCirclePathBuffer, mRenderPaint);
            } else {

                canvas.drawCircle(
                        circleRadius,
                        circleRadius,
                        circleRadius,
                        mRenderPaint);

                if (drawCircleHole) {
                    canvas.drawCircle(
                            circleRadius,
                            circleRadius,
                            circleHoleRadius,
                            mCirclePaintInner);
                }
            }
        }
    }

    /**
     * Returns the cached Bitmap at the given index.
     *
     * @param index
     * @return
     */
    protected Bitmap getBitmap(int index) {
        return circleBitmaps[index % circleBitmaps.length];
    }
}