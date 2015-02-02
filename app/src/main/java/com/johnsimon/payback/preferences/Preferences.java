package com.johnsimon.payback.preferences;

import com.johnsimon.payback.currency.UserCurrency;

import java.util.Currency;
import java.util.Locale;

public class Preferences {

	private final static String DEFAULT_BACKGROUND = "mountains";

	public Preference<String> background;
	public Preference<UserCurrency> currency;


	public static Preferences defaultPreferences() {
		Preferences preferences = new Preferences();

		preferences.background = new Preference<>(null);
		preferences.currency = new Preference<>(null);

		return preferences;
	}

	private <T> T getWithDefault(Preference<T> preference, T defaultValue) {
		T value = preference.getValue();
		return value == null ? defaultValue : value;
	}

	public UserCurrency getCurrency() {
		UserCurrency value = currency.getValue();

		if(value == null) {
			String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
			return new UserCurrency(symbol, symbol, true, UserCurrency.DECIMAL_SEPARATOR_COMMA, UserCurrency.THOUSAND_SEPARATOR_SPACE);
		}

		return value;
	}

	public String getBackground() {
		return getWithDefault(background, DEFAULT_BACKGROUND);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Preferences))return false;
		Preferences other = (Preferences) o;

		return other.background.equals(background)
			&& other.currency.equals(currency);
	}
}