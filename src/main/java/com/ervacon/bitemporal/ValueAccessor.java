/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import java.io.Serializable;
import org.threeten.extra.Interval;

/**
 * Simple strategy to access a value in a {@link Bitemporal} object.
 *
 * @see BitemporalProperty
 *
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
public interface ValueAccessor<V, T extends Bitemporal> extends Serializable {

	/**
	 * Extract the value from given bitemporal.
	 */
	public V extractValue(T t);

	/**
	 * Create a bitemporal wrapping given value, valid for specified validity interval.
	 */
	public T wrapValue(V value, Interval validityInterval);
}
