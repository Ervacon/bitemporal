/*
 * (c) Copyright Ervacon 2007.
 * All Rights Reserved.
 */

package com.ervacon.bitemporal;

import java.util.Collection;

import org.joda.time.Interval;

/**
 * {@link BitemporalProperty} implementation that uses {@link BitemporalWrapper}s.
 * 
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
public class WrappedBitemporalProperty<V> extends BitemporalProperty<V, BitemporalWrapper<V>> {

	public WrappedBitemporalProperty(Collection<? extends Bitemporal> data) {
		super(data, new ValueAccessor<V, BitemporalWrapper<V>>() {
			public V extractValue(BitemporalWrapper<V> t) {
				if (t == null) {
					return null;
				} else {
					return t.getValue();
				}
			}

			public BitemporalWrapper<V> wrapValue(V value, Interval validityInterval) {
				return new BitemporalWrapper<V>(value, validityInterval);
			}
		});
	}
}
