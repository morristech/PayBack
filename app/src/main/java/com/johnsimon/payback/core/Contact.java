package com.johnsimon.payback.core;

import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.util.PhoneNumberUtils;

public class Contact {
	public String name;
	public String[] numbers;
	public String[] emails;
	public String photoURI;
	public long id;

	public Contact(String name, String photoURI, long id) {
		this.name = name;
		this.photoURI = photoURI;
		this.id = id;
	}

    public void setNumbers(String[] numbers) {
        this.numbers = numbers;
    }

	public boolean hasNumbers() {
		return numbers != null;
	}

	public void setEmails(String[] emails) {
		this.emails = emails;
	}

	public boolean hasEmails() {
		return this.emails != null;
	}

	public boolean matchTo(User user) {
		return this.name.equals(user.name)
			|| (hasNumbers() && user.hasNumbers() && matchNumbers(user.numbers))
			|| (hasEmails() && user.hasEmails() && matchEmails(user.emails));
	}

	public boolean matchTo(Person person) {
		return this.name.equals(person.getName());
	}

	private boolean matchNumbers(String[] numbers) {
		for(String a : this.numbers) {
			String aNormalized = PhoneNumberUtils.normalizePhoneNumber(a);
			for(String b : numbers) {
				String bNormalized = PhoneNumberUtils.normalizePhoneNumber(b);
				if(aNormalized.equals(bNormalized)) return true;
			}
		}
		return false;
	}

	private boolean matchEmails(String[] emails) {
		for(String a : this.emails) {
			for(String b : emails) {
				if(a.equals(b)) return true;
			}
		}
		return false;
	}
}
