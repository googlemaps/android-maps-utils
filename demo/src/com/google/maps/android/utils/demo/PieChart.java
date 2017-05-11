/*
 * Created by Amin Yazdanpanah 2016
 * http://www.aminyazdanpanah.com
 */
package com.google.maps.android.utils.demo;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;


class PieChart extends Drawable {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] value_degree;
    private final float[] value_real;
    private int[] COLORS = {0xFF74E370, 0xFF54B8FF, 0xFFFF5754, 0xFF939393};
    private RectF rectf = new RectF(convertDpToPixel(20),convertDpToPixel(17),convertDpToPixel(80), convertDpToPixel(57));
    private RectF rect = new RectF(convertDpToPixel(20),convertDpToPixel(17),convertDpToPixel(80), convertDpToPixel(67));
    private int temp = 0;

    PieChart(float[] values, float[] realValues) {
        value_degree = new float[]{values[0], values[1], values[2], values[3]};
        value_real = new float[]{realValues[0], realValues[1], realValues[2], realValues[3]};
    }

    @Override


    public void draw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
        paint.setStrokeWidth(100);
        paint.setColor(0xAA777777);
        canvas.drawOval(rect, paint);

        for (int i = 0; i < value_degree.length; i++) {
            if (i == 0) {
                paint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
                paint.setStrokeWidth(1);
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, 0, value_degree[i], true, paint);
                paint.setColor(Color.BLACK);
                paint.setTextSize(convertDpToPixel(13));
                if (value_real[i] != 0) {
                    double x = 40 * Math.cos(Math.toRadians(value_degree[i] / 2)) + 40;
                    double y = 35 * Math.sin(Math.toRadians(value_degree[i] / 2)) + 40;
                    canvas.drawText((int) value_real[i]+"", convertDpToPixel((float) x), convertDpToPixel((float) y), paint);
                }
            } else {
                paint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
                temp += (int) value_degree[i - 1];
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, temp, value_degree[i], true, paint);
                paint.setColor(Color.BLACK);
                paint.setTextSize(convertDpToPixel(13));
                if (value_real[i] != 0) {
                    double x = 40 * Math.cos(Math.toRadians(temp + (value_degree[i] / 2))) +40;
                    double y = 35 * Math.sin(Math.toRadians(temp + (value_degree[i] / 2))) + 45;
                    canvas.drawText((int) value_real[i] + "", convertDpToPixel((float) x), convertDpToPixel((float) y), paint);
                }
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    private static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

}
