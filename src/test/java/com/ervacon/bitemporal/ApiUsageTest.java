/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal;

import static com.ervacon.bitemporal.TimeUtils.day;
import static com.ervacon.bitemporal.TimeUtils.from;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import org.junit.After;
import org.junit.Test;

public class ApiUsageTest {

	@After
	public void tearDown() throws Exception {
		TimeUtils.clearReference();
	}

	@Test
	public void testNonTemporalApiUsage() {
		Person pete = new Person("Pete");
		pete.address().set(new Address("Foostreet", "Bartown", "USA"));
		assertTrue(pete.address().hasValue());
		assertEquals("Foostreet", pete.address().now().getLine1());
	}

	@Test
	public void testTemporalApiUsage() {
		Person pete = new Person("Pete");
		pete.address().set(
				new Address("Foostreet", "Bartown", "USA"),
				from(day(1, 1, 2000)));
		assertTrue(pete.address().hasValueOn(day(1, 1, 2001)));
		assertEquals(
				"Foostreet",
				pete.address().on(day(1, 1, 2001)).getLine1());
		assertEquals(
				from(day(1, 1, 2000)),
				pete.address().get().getValidityInterval());
	}
}
