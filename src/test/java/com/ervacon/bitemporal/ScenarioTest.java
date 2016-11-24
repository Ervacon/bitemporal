/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import junit.framework.TestCase;

public class ScenarioTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		TimeUtils.clearReference();
	}

	/**
	 * The example scenarion described on Wikipedia:
	 * http://en.wikipedia.org/wiki/Temporal_database
	 */
	public void testScenario() {
		// 3/4/1975 John Doe is born
		// nothing happens

		// 4/4/1975 John's father registers the baby
		TimeUtils.setReference(TimeUtils.day(4, 4, 1975));
		Person johnDoe = new Person("John Doe");
		johnDoe.alive().set(
				true,
				TimeUtils.from(TimeUtils.day(3, 4, 1975)));
		johnDoe.address().set(
				new Address("Some Street 8", "Smallville", "FL, USA"),
				TimeUtils.from(TimeUtils.day(3, 4, 1975)));

		// 26/8/1994 John moves to Bigtown, but forgets to register
		// nothing happens
		// 27/12/1994 John registers his move
		TimeUtils.setReference(TimeUtils.day(27, 12, 1994));
		johnDoe.address().set(
				new Address("Some Avenue 773", "Bigtown", "FL, USA"),
				TimeUtils.from(TimeUtils.day(26, 8, 1994)));

		// 1/4/2001 John is killed in an accident, reported by the coroner that same day
		TimeUtils.setReference(TimeUtils.day(1, 4, 2001));
		johnDoe.alive().set(false);

		// tests
		TimeUtils.setReference(TimeUtils.day(1, 1, 2007));

		assertFalse(johnDoe.alive().hasValueOn(TimeUtils.day(1, 1, 1975)));
		assertTrue(johnDoe.alive().on(TimeUtils.day(3, 4, 1975)));
		assertFalse(johnDoe.alive().hasValueOn(TimeUtils.day(3, 4, 1975), TimeUtils.day(3, 4, 1975)));
		assertFalse(johnDoe.alive().now());

		assertEquals(
				new Address("Some Street 8", "Smallville", "FL, USA"),
				johnDoe.address().on(TimeUtils.day(3, 4, 1975)));
		assertEquals(
				new Address("Some Avenue 773", "Bigtown", "FL, USA"),
				johnDoe.address().on(TimeUtils.day(26, 8, 1994)));
		assertEquals(
				new Address("Some Street 8", "Smallville", "FL, USA"),
				johnDoe.address().on(TimeUtils.day(26, 8, 1994), TimeUtils.day(26, 8, 1994)));
		assertEquals(
				new Address("Some Avenue 773", "Bigtown", "FL, USA"),
				johnDoe.address().on(TimeUtils.day(26, 8, 1994), TimeUtils.day(27, 12, 1994)));
		assertEquals(
				new Address("Some Avenue 773", "Bigtown", "FL, USA"),
				johnDoe.address().now());

		System.out.println("The database now looks like this:\n");
		System.out.println(johnDoe.address().getTrace().toString());
	}

}
