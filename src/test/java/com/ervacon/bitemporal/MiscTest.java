/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

import org.junit.After;
import org.junit.Test;

public class MiscTest {

	@After
	public void tearDown() throws Exception {
		TimeUtils.clearReference();
	}

	@Test
	public void testAbuttingIntervals() {
		Person person = new Person("John Doe");

		TimeUtils.setReference(TimeUtils.day(1, 1, 2000));
		person.address().set(new Address("A", "B", "C"),
				TimeUtils.interval(TimeUtils.day(1, 2, 2000), TimeUtils.day(1, 3, 2000)));

		TimeUtils.setReference(TimeUtils.day(1, 1, 2001));
		person.address().set(new Address("X", "Y", "Z"),
				TimeUtils.interval(TimeUtils.day(1, 1, 2000), TimeUtils.day(1, 2, 2000)));

		assertEquals(2, person.address().getTrace().getData().size());
	}

	@Test
	public void testOverlapAtTheEnd() {
		Person person = new Person("John Doe");

		TimeUtils.setReference(TimeUtils.day(1, 1, 2000));
		person.address().set(new Address("A", "B", "C"), TimeUtils.from(TimeUtils.day(1, 2, 2000)));

		TimeUtils.setReference(TimeUtils.day(1, 1, 2001));
		person.address().set(new Address("X", "Y", "Z"),
				TimeUtils.interval(TimeUtils.day(1, 1, 2000), TimeUtils.day(1, 4, 2000)));

		System.out.println(person.address().getTrace());
		assertEquals(3, person.address().getTrace().getData().size());
	}

	@Test
	public void testVisionFromTheFuture() {
		Person person = new Person("John Doe");

		TimeUtils.setReference(TimeUtils.day(2, 1, 2000));
		person.address().set(new Address("A", "B", "C"),
				TimeUtils.interval(TimeUtils.day(1, 2, 2000), TimeUtils.day(1, 3, 2000)));

		TimeUtils.setReference(TimeUtils.day(1, 1, 2000));
		try {
			person.address().set(new Address("X", "Y", "Z"),
					TimeUtils.interval(TimeUtils.day(1, 1, 2000), TimeUtils.day(1, 2, 2000)));
			fail();
		} catch (IllegalStateException e) {
			// expected
		}
	}
}
