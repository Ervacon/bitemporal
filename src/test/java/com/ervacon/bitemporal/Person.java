/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class Person implements Serializable {

	private Long id;
	private String name;
	private Collection<BitemporalWrapper<Address>> address = new LinkedList<>();
	private Collection<BitemporalWrapper<Boolean>> alive = new LinkedList<>();

	/**
	 * For Hibernate.
	 */
	@SuppressWarnings("unused")
	private Person() {
	}

	public Person(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name is required");
		}
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public WrappedBitemporalProperty<Address> address() {
		return new WrappedBitemporalProperty<>(address);
	}

	public WrappedBitemporalProperty<Boolean> alive() {
		return new WrappedBitemporalProperty<>(alive);
	}

	@Override
	public String toString() {
		return getName();
	}
}
