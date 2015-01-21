package com.johnsimon.payback.data;

import android.content.res.Resources;
import android.text.TextUtils;

import com.johnsimon.payback.R;

public class User {
	public String name;
	public String[] numbers;

	public User(String name) {
		this.name = name;
	}

    public void setNumbers(String[] numbers) {
        this.numbers = numbers;
    }

	public boolean hasNumbers() {
		return numbers != null;
	}

	public String getName(Resources resources) {
		return TextUtils.isEmpty(name) ? resources.getString(R.string.you) : name;
	}
}