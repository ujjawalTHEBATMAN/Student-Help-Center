package com.example.abcd.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CustomGraphView extends View {

    private Paint axisPaint, linePaint, labelPaint, dotPaint, gridPaint;
    private final int paddingLeft = 80;
    private final int paddingRight = 40;
    private final int paddingTop = 40;
    private final int paddingBottom = 80;
    private final int primaryColor = Color.parseColor("#6200EE"); // Adjust to match @color/primary_color
    private final int chartLineColor = Color.parseColor("#3B8AFF");

    private TreeMap<String, Integer> dataPoints = new TreeMap<>();
    private int maxValue = 100;

    public CustomGraphView(Context context) {
        super(context);
        init();
    }

    public CustomGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Paint for axes
        axisPaint = new Paint();
        axisPaint.setColor(Color.DKGRAY);
        axisPaint.setStrokeWidth(2f);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setAntiAlias(true);

        // Paint for the chart line
        linePaint = new Paint();
        linePaint.setColor(chartLineColor);
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        // Paint for data point markers
        dotPaint = new Paint();
        dotPaint.setColor(primaryColor);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        // Paint for labels
        labelPaint = new Paint();
        labelPaint.setColor(Color.DKGRAY);
        labelPaint.setTextSize(30f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setAntiAlias(true);

        // Paint for grid lines
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{5, 5}, 0));
        gridPaint.setAntiAlias(true);
    }

    public void setData(Map<String, Integer> data) {
        dataPoints.clear();
        if (data != null && !data.isEmpty()) {
            dataPoints.putAll(data);
            maxValue = Collections.max(data.values()) + 10; // Add padding to max value
        } else {
            maxValue = 100; // Default max
        }
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints.isEmpty()) {
            // Draw placeholder text if no data
            Paint textPaint = new Paint();
            textPaint.setColor(Color.GRAY);
            textPaint.setTextSize(40f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int graphWidth = width - paddingLeft - paddingRight;
        int graphHeight = height - paddingTop - paddingBottom;

        // Draw X and Y axes
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, axisPaint); // X-axis
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, axisPaint); // Y-axis

        // Draw grid lines
        int numHorizontalLines = 5;
        for (int i = 0; i < numHorizontalLines; i++) {
            float y = paddingTop + (graphHeight * i / (float) (numHorizontalLines - 1));
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);

            // Draw Y-axis labels
            int labelValue = maxValue - (maxValue * i / (numHorizontalLines - 1));
            canvas.drawText(String.valueOf(labelValue), paddingLeft / 2f, y + 10, labelPaint);
        }

        // Only draw graph if we have data points
        if (dataPoints.size() > 0) {
            float segmentWidth = graphWidth / (float) (dataPoints.size() - 1 > 0 ? dataPoints.size() - 1 : 1);

            // Prepare path for chart line
            Path path = new Path();
            boolean firstPoint = true;
            int index = 0;
            List<String> keys = new ArrayList<>(dataPoints.keySet());

            for (String key : keys) {
                Integer value = dataPoints.get(key);
                if (value == null) continue;

                float x = paddingLeft + (index * segmentWidth);
                float y = paddingTop + graphHeight - ((value / (float) maxValue) * graphHeight);

                if (firstPoint) {
                    path.moveTo(x, y);
                    firstPoint = false;
                } else {
                    path.lineTo(x, y);
                }

                // Draw points
                canvas.drawCircle(x, y, 8, dotPaint);

                // Draw X-axis labels (dates)
                // This could be formatted to show just day, or month+day depending on your needs
                String displayKey = key;
                if (key.length() > 5) {  // Truncate long keys
                    displayKey = key.substring(key.length() - 5);
                }
                canvas.drawText(displayKey, x, height - paddingBottom / 2f, labelPaint);

                index++;
            }

            // Draw the path
            canvas.drawPath(path, linePaint);

            // Optional: Draw area under the line
            Paint fillPaint = new Paint();
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(Color.argb(50, 59, 138, 255)); // Semi-transparent blue

            // Close the path to fill area under the line
            Path fillPath = new Path(path);
            fillPath.lineTo(paddingLeft + ((keys.size() - 1) * segmentWidth), height - paddingBottom);
            fillPath.lineTo(paddingLeft, height - paddingBottom);
            fillPath.close();
            canvas.drawPath(fillPath, fillPaint);
        }

        // Draw title
        Paint titlePaint = new Paint();
        titlePaint.setColor(primaryColor);
        titlePaint.setTextSize(40f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("Daily App Views", width / 2f, paddingTop / 2f, titlePaint);
    }
}