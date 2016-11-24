/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.threeten.extra.Interval;

/**
 * Represents a bitemporally tracked property of a class (for instance the name of a person).
 * <p>
 * The {@link BitemporalProperty} class provides a high-level API expressed in terms of actual value classes
 * (e.g. String), layered on top of low-level contructs such as a {@link BitemporalTrace} and {@link Bitemporal} objects.
 * To be able to provide an API at the level of actual value classes, the {@link BitemporalProperty} uses
 * a {@link ValueAccessor} to extract actual values from {@link Bitemporal} objects.
 *
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
@SuppressWarnings("unchecked")
public class BitemporalProperty<V, T extends Bitemporal> implements Serializable {

	private BitemporalTrace trace;
	private ValueAccessor<V, T> accessor;

	/**
	 * Create a new bitemporal property setting up a trace using given data, and using given value accessor.
	 */
	public BitemporalProperty(Collection<? extends Bitemporal> data, ValueAccessor<V, T> accessor) {
		this(new BitemporalTrace((Collection<Bitemporal>) data), accessor);
	}

	/**
	 * Create a new bitemporal property wrapping given trace and using given value accessor.
	 */
	public BitemporalProperty(BitemporalTrace trace, ValueAccessor<V, T> accessor) {
		if (trace == null) {
			throw new IllegalArgumentException("The bitemporal trace is required");
		}
		if (accessor == null) {
			throw new IllegalArgumentException("The value accessor is required");
		}
		this.trace = trace;
		this.accessor = accessor;
	}

	/**
	 * Returns the wrapped bitemporal trace.
	 */
	public BitemporalTrace getTrace() {
		return trace;
	}

	/**
	 * Returns the value valid {@link TimeUtils#now() now} as currently known.
	 */
	public V now() {
		return accessor.extractValue(get());
	}

	/**
	 * Returns the value valid on specified date as currently known.
	 */
	public V on(Instant validOn) {
		return accessor.extractValue(get(validOn));
	}

	/**
	 * Returns the value valid on specified date as known on given date.
	 */
	public V on(Instant validOn, Instant knownOn) {
		return accessor.extractValue(get(validOn, knownOn));
	}

	/**
	 * Returns the bitemporal valid {@link TimeUtils#now() now} as currently known.
	 */
	public T get() {
		return get(TimeUtils.now());
	}

	/**
	 * Returns the bitemporal valid on specified date as currently known.
	 */
	public T get(Instant validOn) {
		return get(validOn, TimeUtils.now());
	}

	/**
	 * Returns the bitemporal valid on specified date as known on given date.
	 */
	public T get(Instant validOn, Instant knownOn) {
		Collection<T> coll = (Collection<T>) trace.get(validOn, knownOn);
		if (coll.isEmpty()) {
			return null;
		} else {
			// assume single valued
			return coll.iterator().next();
		}
	}

	/**
	 * Returns the history of the value as currently known.
	 * This informs you about how the valid value changed, as we currently know it.
	 */
	public List<T> getHistory() {
		return getHistory(TimeUtils.now());
	}

	/**
	 * Returns the history of the value as known on given date.
	 * This informs you about how the valid value changed, as known on given date.
	 */
	public List<T> getHistory(Instant knownOn) {
		return (List<T>) trace.getHistory(knownOn);
	}

	/**
	 * Returns the evolution of the value currently valid.
	 * This informs you about how our knowledge about the value currently valid evolved.
	 */
	public List<T> getEvolution() {
		return getEvolution(TimeUtils.now());
	}

	/**
	 * Returns the evolution of the value valid on given date.
	 * This informs you about how our knowledge about the value valid on given date evolved.
	 */
	public List<T> getEvolution(Instant validOn) {
		return (List<T>) trace.getEvolution(validOn);
	}

	/**
	 * Set the value of this bitemporal property. The new value will be valid {@link TimeUtils#fromNow() from now on}.
	 */
	public void set(V value) {
		set(value, TimeUtils.fromNow());
	}

	/**
	 * Set the value of this bitemporal property for specified validity interval.
	 */
	public void set(V value, Interval validityInterval) {
		trace.add(accessor.wrapValue(value, validityInterval));
	}

	/**
	 * <i>Forget</i> the currently valid value.
	 */
	public void end() {
		end(TimeUtils.now());
	}

	/**
	 * <i>Forget</i> the valid valid on given date.
	 */
	public void end(Instant validOn) {
		trace.get(validOn, TimeUtils.now()).forEach(bt -> bt.end());
	}

	/**
	 * Returns whether or not this property has a known value currently valid.
	 */
	public boolean hasValue() {
		return hasValueOn(TimeUtils.now());
	}

	/**
	 * Returns whether or not this property has a value valid on given date.
	 */
	public boolean hasValueOn(Instant validOn) {
		return hasValueOn(validOn, TimeUtils.now());
	}

	/**
	 * Returns whether or not this property had a value valid on given date as known on specified date.
	 */
	public boolean hasValueOn(Instant validOn, Instant knownOn) {
		return !trace.get(validOn, knownOn).isEmpty();
	}

	@Override
	public String toString() {
		return String.valueOf(now());
	}
}
