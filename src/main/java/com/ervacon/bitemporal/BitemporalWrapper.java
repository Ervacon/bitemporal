/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import static com.ervacon.bitemporal.TimeUtils.fromNow;
import static com.ervacon.bitemporal.TimeUtils.interval;
import static com.ervacon.bitemporal.TimeUtils.now;

import java.io.Serializable;
import org.threeten.extra.Interval;

/**
 * Decorates a value with bitemporal information, making it possible to bitemporally track the value in a
 * {@link BitemporalTrace}. A {@link BitemporalWrapper} allows you to bitemporally track existing value classes,
 * for instance strings.
 * <p>
 * Due to the nature of bitemporality, the wrapped value should be immutable. The value itself will never change,
 * instead new values will be added to the {@link BitemporalTrace} to represent changes in the value.
 * A {@link BitemporalWrapper} itself is not immutable, its record interval can be {@link #end() ended}.
 * <p>
 * Instances of this class are serializable if the wrapped value is serializable.
 * <p>
 * Objects of this class are not thread-safe.
 *
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
public class BitemporalWrapper<V> implements Bitemporal, Serializable {

	private Long id;
	private V value;
	private Interval validityInterval;
	private Interval recordInterval;

	/**
	 * For Hibernate.
	 */
	@SuppressWarnings("unused")
	private BitemporalWrapper() {
	}

	/**
	 * Bitemporally wrap given value. Validity will be as specified, and the recording interval will
	 * be {@link TimeUtils#fromNow() from now on}.
	 * @param value the value to wrap (can be null)
	 * @param validityInterval the validity of the value
	 */
	public BitemporalWrapper(V value, Interval validityInterval) {
		if (validityInterval == null) {
			throw new IllegalArgumentException("The validity interval is required");
		}
		this.validityInterval = validityInterval;
		this.recordInterval = fromNow();
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the wrapped value, possibly null.
	 */
	public V getValue() {
		return value;
	}

	@Override
	public Interval getValidityInterval() {
		return validityInterval;
	}

	@Override
	public Interval getRecordInterval() {
		return recordInterval;
	}

	@Override
	public void end() {
		this.recordInterval = interval(getRecordInterval().getStart(), now());
	}

	@Override
	public Bitemporal copyWith(Interval validityInterval) {
		// force record interval to be 'from now'
		return new BitemporalWrapper<>(getValue(), validityInterval);
	}

	@Override
	public String toString() {
		return getValidityInterval() + "  ~  " + getRecordInterval() + "  ~  " + getValue();
	}
}
