package com.google.maps.android.kml;

import android.graphics.Color;

/**
 * Created by lavenderc on 12/2/14.
 */
public class Style {

    String style;
    String lineColor;
    int width;
    boolean outline;
    String polyColor;
    String colorMode;

    public Style() {
        style = null;
        lineColor = null;
        width = 0;
        outline = false;
        colorMode = null;
    }


    public void setOutline(boolean value) {
        outline = value;
    }

    public void setStyleID(String styleID) {
        style = styleID;
    }

    public void setLineColor(String color) {
        lineColor = color;
    }

     public void setLineWidth(Integer integer) {
        width = integer;
    }

    public void setPolyFillColor (String color) {
        polyColor = color;
    }

    public void setColorMode(String mode) { colorMode = mode; }

    public String getPolyFillColor (){
        return polyColor;
    }

    public boolean getOutline() {
        return outline;
    }

    public int getLineWidth() {
        return width;
    }

    public String getLineColor() {
        return lineColor;
    }

    public String getStyleID() {
        return style;
    }

    public String getColorMode() { return colorMode; }

    public void setStyle(String style) {
        this.style = style;
    }
}
