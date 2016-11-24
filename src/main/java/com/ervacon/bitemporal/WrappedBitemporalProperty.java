/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import java.util.Collection;
import org.threeten.extra.Interval;

/**
 * {@link BitemporalProperty} implementation that uses {@link BitemporalWrapper}s.
 *
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
public class WrappedBitemporalProperty<V> extends BitemporalProperty<V, BitemporalWrapper<V>> {

	public WrappedBitemporalProperty(Collection<? extends Bitemporal> data) {
		super(data, new ValueAccessor<V, BitemporalWrapper<V>>() {
			@Override
			public V extractValue(BitemporalWrapper<V> t) {
				if (t == null) {
					return null;
				} else {
					return t.getValue();
				}
			}

			@Override
			public BitemporalWrapper<V> wrapValue(V value, Interval validityInterval) {
				return new BitemporalWrapper<>(value, validityInterval);
			}
		});
	}
}
