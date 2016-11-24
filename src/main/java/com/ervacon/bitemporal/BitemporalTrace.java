/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import static com.ervacon.bitemporal.TimeUtils.endOfTime;
import static java.util.stream.Collectors.toCollection;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import org.threeten.extra.Interval;

/**
 * A trace of {@link Bitemporal} objects, bitemporally tracking some value (for instance a person's name).
 * A bitemporal trace works on top of (wraps) a collection of {@link Bitemporal} objects, representing the raw
 * data to query and manipulate.
 * <p>
 * Together with {@link Bitemporal}, {@link BitemporalTrace} provides a low level API for bitemporal data tracking
 * and manipulation expressed in terms of {@link Bitemporal} objects.
 * <p>
 * A bitemporal trace will be serializable if all the bitemporals it contains are serializable.
 * <p>
 * A bitemporal trace is not thread-safe.
 *
 * @author Erwin Vervaet
 * @author Christophe Vanfleteren
 */
public class BitemporalTrace implements Serializable {

	private Collection<Bitemporal> data;

	/**
	 * Create a new bitemporal trace working on top of given data collection.
	 */
	public BitemporalTrace(Collection<Bitemporal> data) {
		if (data == null) {
			throw new IllegalArgumentException("The bitemporal data is required");
		}
		this.data = data;
	}

	/**
	 * Returns the wrapped data collection.
	 */
	public Collection<Bitemporal> getData() {
		return this.data;
	}

	/**
	 * Returns the {@link Bitemporal} objects valid on given date as known on specified date.
	 */
	public Collection<Bitemporal> get(Instant validOn, Instant knownOn) {
		return data.stream()
				.filter(bt -> bt.getValidityInterval().contains(validOn) && bt.getRecordInterval().contains(knownOn))
				.collect(toCollection(LinkedList::new));
	}

	/**
	 * Returns the history of the tracked value, as known on specified time.
	 * The history informs you about how the valid value changed over time.
	 */
	public Collection<Bitemporal> getHistory(Instant knownOn) {
		return data.stream()
				.filter(bt -> bt.getRecordInterval().contains(knownOn))
				.collect(toCollection(LinkedList::new));
	}

	/**
	 * Returns the evolution of the tracked value for a specified validity date.
	 * The evolution informs you about how knowledge about the value valid at a certain date evolved.
	 */
	public Collection<Bitemporal> getEvolution(Instant validOn) {
		return data.stream()
				.filter(bt -> bt.getValidityInterval().contains(validOn))
				.collect(toCollection(LinkedList::new));
	}

	/**
	 * Add given {@link Bitemporal} to the trace, manipulating the trace as necessary.
	 * This is essentially the basic bitemporal data manipulation operation.
	 */
	public void add(Bitemporal newValue) {
		sanityCheck();

		Collection<Bitemporal> toEnd = new HashSet<>();
		Collection<Bitemporal> toAdd = new LinkedList<>();

		for (Bitemporal possibleOverlap : getHistory(TimeUtils.now())) {
			if (newValue.getValidityInterval().overlaps(possibleOverlap.getValidityInterval())) {
				toEnd.add(possibleOverlap);
			}
		}

		Instant validityStartOfNewValue = newValue.getValidityInterval().getStart();
		for (Bitemporal validOnStartOfNewValue : get(validityStartOfNewValue, TimeUtils.now())) {
			if (validityStartOfNewValue.compareTo(validOnStartOfNewValue.getValidityInterval().getStart()) > 0) {
				Interval validityInterval = TimeUtils.interval(validOnStartOfNewValue.getValidityInterval().getStart(),
						validityStartOfNewValue);
				toAdd.add(validOnStartOfNewValue.copyWith(validityInterval));
			}
		}

		if (!newValue.getValidityInterval().getEnd().equals(endOfTime())) {
			Instant validityEndOfNewValue = newValue.getValidityInterval().getEnd();
			for (Bitemporal validOnEndOfNewValue : get(validityEndOfNewValue, TimeUtils.now())) {
				if (validityEndOfNewValue.compareTo(validOnEndOfNewValue.getValidityInterval().getStart()) > 0) {
					Interval validityInterval = TimeUtils.interval(validityEndOfNewValue, validOnEndOfNewValue
							.getValidityInterval().getEnd());
					toAdd.add(validOnEndOfNewValue.copyWith(validityInterval));
				}
			}
		}

		toEnd.forEach(bt -> bt.end());
		toAdd.forEach(bt -> data.add(bt));
		data.add(newValue.copyWith(newValue.getValidityInterval()));
	}

	@Override
	public String toString() {
		StringWriter buf = new StringWriter();
		PrintWriter bufWriter = new PrintWriter(buf);
		data.forEach(bt -> bufWriter.println(bt));
		return buf.toString();
	}

	/**
	 * Make sure we're not in the past relative to the recording intervals in the trace.
	 */
	private void sanityCheck() throws IllegalStateException {
		if (data.stream().anyMatch(bt -> bt.getRecordInterval().getStart().isAfter(TimeUtils.now()))) {
			throw new IllegalStateException("Cannot manipulate bitemporal trace; trace contains data from the future");
		}
	}
}
