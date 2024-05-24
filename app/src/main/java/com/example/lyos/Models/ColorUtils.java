package com.example.lyos.Models;

import android.graphics.Color;

import java.util.ArrayList;

public class ColorUtils {
    public static ArrayList<Integer> getColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 0, 0)); // Red
        colors.add(Color.rgb(0, 255, 0)); // Green
        colors.add(Color.rgb(0, 0, 255)); // Blue
        colors.add(Color.rgb(255, 255, 0)); // Yellow
        colors.add(Color.rgb(0, 255, 255)); // Cyan
        colors.add(Color.rgb(255, 0, 255)); // Magenta
        colors.add(Color.rgb(169, 169, 169)); // Dark Gray
        colors.add(Color.rgb(255, 165, 0)); // Orange
        colors.add(Color.rgb(128, 0, 128)); // Purple
        colors.add(Color.rgb(0, 128, 128)); // Teal
        colors.add(Color.rgb(0, 128, 0)); // Dark Green
        colors.add(Color.rgb(128, 0, 0)); // Maroon
        colors.add(Color.rgb(0, 0, 128)); // Navy
        colors.add(Color.rgb(255, 192, 203)); // Pink
        colors.add(Color.rgb(128, 128, 0)); // Olive
        colors.add(Color.rgb(75, 0, 130)); // Indigo
        colors.add(Color.rgb(245, 222, 179)); // Wheat
        colors.add(Color.rgb(220, 20, 60)); // Crimson
        colors.add(Color.rgb(95, 158, 160)); // Cadet Blue
        colors.add(Color.rgb(240, 230, 140)); // Khaki
        return colors;
    }
}
