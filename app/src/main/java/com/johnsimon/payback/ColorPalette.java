package com.johnsimon.payback;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 2014-09-23.
 */
public class ColorPalette {
	private static ColorPalette instance = null;

	private int[] palette;
	public ColorPalette(Resources resources) {
		palette = new int[] {
			resources.getColor(R.color.color1),
			resources.getColor(R.color.color2),
			resources.getColor(R.color.color3)
		};
	}

	public int nextColor() {
		SparseIntArray usedColors = new SparseIntArray(palette.length);
		for(int color : palette) {
			usedColors.put(color, 0);
		}

		for (Person person : Resource.people) {
			if(person.color != null) {
				usedColors.put(person.color, usedColors.get(person.color) + 1);
			}
		}

		int color = 0, smallest = -1;
		for(int i = 0; i < usedColors.size(); i++) {
			int value = usedColors.valueAt(i);
			if(smallest == -1 || value < smallest) {
				smallest = value;
				color = usedColors.keyAt(i);
			}
		}

		return color;
	}

	public static ColorPalette getInstance(Context context) {
		if(instance != null) {
			return instance;
		} else {
			return (instance = new ColorPalette(context.getResources()));
		}
	}
}
