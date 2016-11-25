/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.threeten.extra.Interval;

/**
 * Static utilities dealing with <i>time</i>.
 *
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
public final class TimeUtils {

	/**
	 * Instant representing the end of time.
	 * <p>
	 * Note that this does not use {@link Instant#MAX} because most databases put limits on the maximum timestamp that
	 * can be stored.
	 */
	public static final Instant END_OF_TIME = Instant.ofEpochMilli(32503676400000L); // 1/1/3000; adapt for your needs

	// no need to instantiate this class
	private TimeUtils() {
	}

	// time framing functionality
	private static final ThreadLocal<Clock> REFERENCE = new ThreadLocal<>();

	/**
	 * Determines whether or not a reference time has been set.
	 */
	public static boolean isReferenceSet() {
		return REFERENCE.get() != null;
	}

	/**
	 * Returns the reference time, or <i>wallclock now</i> if no reference time has been set.
	 */
	public static Instant reference() {
		return isReferenceSet() ? REFERENCE.get().instant() : Clock.systemDefaultZone().instant();
	}

	/**
	 * Set the reference time to the specified time.
	 * @param dateTime the reference time to set
	 */
	public static void setReference(Instant dateTime) {
		REFERENCE.set(Clock.fixed(dateTime, ZoneId.systemDefault()));
	}

	/**
	 * Clear the reference time.
	 */
	public static void clearReference() {
		REFERENCE.remove();
	}

	/**
	 * Create a {@link Instant} object representing given day of given month in given year.
	 */
	public static Instant day(int day, int month, int year) {
		return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
	}

	/**
	 * Returns the current time. If a reference time is set, it is that reference time that will be returned.
	 * @see #reference()
	 */
	public static Instant now() {
		return reference();
	}

	/**
	 * Returns a interval running for the specified period. The returned interval is half-open: it includes the
	 * start time, but not the end time.
	 * @see Interval
	 */
	public static Interval interval(Instant start, Instant end) {
		return Interval.of(start, end);
	}

	/**
	 * Returns an interval running from given start time till the end of time.
	 * @see #endOfTime()
	 */
	public static Interval from(Instant start) {
		return interval(start, END_OF_TIME);
	}

	/**
	 * Returns an interval running from now till the end of time.
	 */
	public static Interval fromNow() {
		return from(now());
	}
}
