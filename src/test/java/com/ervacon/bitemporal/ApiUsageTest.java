/*
 * (c) Copyright Ervacon 2007.
 * All Rights Reserved.
 */

package com.ervacon.bitemporal;

import static com.ervacon.bitemporal.TimeUtils.*;

import junit.framework.TestCase;

public class ApiUsageTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		TimeUtils.clearReference();
	}

	public void testNonTemporalApiUsage() {
		Person pete = new Person("Pete");
		pete.address().set(new Address("Foostreet", "Bartown", "USA"));
		assertTrue(pete.address().hasValue());
		assertEquals("Foostreet", pete.address().now().getLine1());
	}

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
