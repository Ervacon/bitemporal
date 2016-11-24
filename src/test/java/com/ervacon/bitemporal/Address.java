/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import java.io.Serializable;

public class Address implements Serializable {

	private String line1;
	private String line2;
	private String line3;

	/**
	 * For Hibernate.
	 */
	@SuppressWarnings("unused")
	private Address() {
	}

	public Address(String line1, String line2, String line3) {
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
	}

	public String getLine1() {
		return line1;
	}

	public String getLine2() {
		return line2;
	}

	public String getLine3() {
		return line3;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Address) {
			Address other = (Address) obj;
			return other.line1.equals(this.line1)
					&& other.line2.equals(this.line2)
					&& other.line3.equals(this.line3);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.line1.hashCode() + this.line2.hashCode() + this.line3.hashCode();
	}

	@Override
	public String toString() {
		return this.line1 + " " + line2 + " " + line3;
	}
}
