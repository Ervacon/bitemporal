/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import org.threeten.extra.Interval;

/**
 * A bitemporal object. A bitemporal object is essentially a value tracked in two time dimensions:
 * <ol>
 * 	<li>Validity time: indicates when the value was valid</li>
 * 	<li>Recording time: indicates when the value was known</li>
 * </ol>
 * <p>
 * Tracking information bitemporally allows you to aswer questions along the lines of "On january second 1999, what
 * did we think the valid value was for september first 1980.".
 * <p>
 * In most cases, application level could should not directly implement this interface, but should instead
 * wrap existing value classes bitemporally using a {@link BitemporalWrapper}.
 *
 * @see BitemporalTrace
 *
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
public interface Bitemporal {

	/**
	 * Returns the interval in which this object is valid.
	 */
	public Interval getValidityInterval();

	/**
	 * Returns the interval in which this object is known.
	 */
	public Interval getRecordInterval();

	/**
	 * End the recording interval of this bitemporal object, indicating that it has been superceded by a new object,
	 * or is deemed as no longer relevant (i.e. because it was faulty knowledge) and should be 'forgotten'.
	 */
	public void end();

	/**
	 * Create and return a new bitemporal object representing the same value as this object, but with specified
	 * validity. The recording interval of the returned object will always be {@link TimeUtils#fromNow() from now on}.
	 * @param validityInterval the new validity interval
	 * @return a new bitemporal object
	 */
	public Bitemporal copyWith(Interval validityInterval);

}
