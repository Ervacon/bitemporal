/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import static com.ervacon.bitemporal.TimeUtils.END_OF_TIME;
import static com.ervacon.bitemporal.TimeUtils.interval;
import static com.ervacon.bitemporal.TimeUtils.now;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

	private final Collection<Bitemporal> data;

	/**
	 * Create a new bitemporal trace working on top of given data collection.
	 */
	public BitemporalTrace(Collection<Bitemporal> data) {
		this.data = requireNonNull(data, "The bitemporal data is required");
	}

	/**
	 * Returns the wrapped data collection.
	 */
	public Collection<Bitemporal> getData() {
		return this.data;
	}

	/**
	 * Returns the {@link Bitemporal} objects valid on given instant as known on specified instant.
	 */
	public List<Bitemporal> get(Instant validOn, Instant knownOn) {
		return data.stream()
				.filter(bt -> bt.getValidityInterval().contains(validOn) && bt.getRecordInterval().contains(knownOn))
				.collect(toCollection(LinkedList::new));
	}

	/**
	 * Returns the history of the tracked value, as known on specified time.
	 * The history informs you about how the valid value changed over time.
	 */
	public List<Bitemporal> getHistory(Instant knownOn) {
		return data.stream()
				.filter(bt -> bt.getRecordInterval().contains(knownOn))
				.collect(toCollection(LinkedList::new));
	}

	/**
	 * Returns the evolution of the tracked value for a specified validity instant.
	 * The evolution informs you about how knowledge about the value valid at a certain instant evolved.
	 */
	public List<Bitemporal> getEvolution(Instant validOn) {
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

		Collection<Bitemporal> toEnd = getHistory(now())
				.stream()
				.filter(bt -> newValue.getValidityInterval().overlaps(bt.getValidityInterval()))
				.collect(toList());

		Collection<Bitemporal> toAdd = new LinkedList<>();

		Instant validityStartOfNewValue = newValue.getValidityInterval().getStart();
		toAdd.addAll(get(validityStartOfNewValue, now())
				.stream()
				.filter(bt -> validityStartOfNewValue.compareTo(bt.getValidityInterval().getStart()) > 0)
				.map(bt -> bt.copyWith(interval(bt.getValidityInterval().getStart(), validityStartOfNewValue)))
				.collect(toList()));

		if (!newValue.getValidityInterval().getEnd().equals(END_OF_TIME)) {
			Instant validityEndOfNewValue = newValue.getValidityInterval().getEnd();
			toAdd.addAll(get(validityEndOfNewValue, now())
					.stream()
					.filter(bt -> validityEndOfNewValue.compareTo(bt.getValidityInterval().getStart()) > 0)
					.map(bt -> bt.copyWith(interval(validityEndOfNewValue, bt.getValidityInterval().getEnd())))
					.collect(toList()));
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
		if (data.stream().anyMatch(bt -> bt.getRecordInterval().getStart().isAfter(now()))) {
			throw new IllegalStateException("Cannot manipulate bitemporal trace; trace contains data from the future");
		}
	}
}
