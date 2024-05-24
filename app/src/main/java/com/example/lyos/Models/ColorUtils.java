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
    public static ArrayList<Integer> getBrightColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 105, 180)); // Hot Pink
        colors.add(Color.rgb(255, 182, 193)); // Light Pink
        colors.add(Color.rgb(255, 255, 224)); // Light Yellow
        colors.add(Color.rgb(144, 238, 144)); // Light Green
        colors.add(Color.rgb(173, 216, 230)); // Light Blue
        colors.add(Color.rgb(240, 128, 128)); // Light Coral
        colors.add(Color.rgb(255, 250, 205)); // Lemon Chiffon
        colors.add(Color.rgb(250, 250, 210)); // Light Goldenrod Yellow
        colors.add(Color.rgb(255, 228, 181)); // Moccasin
        colors.add(Color.rgb(255, 222, 173)); // Navajo White
        colors.add(Color.rgb(255, 228, 225)); // Misty Rose
        colors.add(Color.rgb(255, 239, 213)); // Papaya Whip
        colors.add(Color.rgb(255, 218, 185)); // Peach Puff
        colors.add(Color.rgb(255, 240, 245)); // Lavender Blush
        colors.add(Color.rgb(255, 228, 196)); // Bisque
        colors.add(Color.rgb(255, 248, 220)); // Cornsilk
        colors.add(Color.rgb(250, 235, 215)); // Antique White
        colors.add(Color.rgb(240, 255, 240)); // Honeydew
        colors.add(Color.rgb(245, 255, 250)); // Mint Cream
        colors.add(Color.rgb(255, 245, 238)); // Seashell
        return colors;
    }
}
