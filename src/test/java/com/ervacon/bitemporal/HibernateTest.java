/*
 * (c) Copyright Ervacon 2007.
 * All Rights Reserved.
 */

package com.ervacon.bitemporal;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateTest extends TestCase {

	private SessionFactory sessionFactory;

	@Override
	protected void setUp() throws Exception {
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	@Override
	protected void tearDown() throws Exception {
		sessionFactory.close();
		TimeUtils.clearReference();
	}

	public void testPersistence() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		TimeUtils.setReference(TimeUtils.day(4, 4, 1975));
		Person johnDoe = new Person("John Doe");
		johnDoe.alive().set(
				true,
				TimeUtils.from(TimeUtils.day(3, 4, 1975)));
		johnDoe.address().set(
				new Address("Some Street 8", "Smallville", "FL, USA"),
				TimeUtils.from(TimeUtils.day(3, 4, 1975)));

		session.save(johnDoe);

		session.getTransaction().commit();
		session.close();

		System.out.println("--------------------------------");

		Long id = johnDoe.getId();

		session = sessionFactory.openSession();
		session.beginTransaction();

		johnDoe = (Person) session.get(Person.class, id);

		TimeUtils.setReference(TimeUtils.day(27, 12, 1994));
		johnDoe.address().set(
				new Address("Some Avenue 773", "Bigtown", "FL, USA"),
				TimeUtils.from(TimeUtils.day(26, 8, 1994)));

		session.getTransaction().commit();
		session.close();

		System.out.println("--------------------------------");

		session = sessionFactory.openSession();
		session.beginTransaction();

		johnDoe = (Person) session.get(Person.class, id);

		TimeUtils.setReference(TimeUtils.day(1, 4, 2001));
		johnDoe.alive().set(false);

		session.getTransaction().commit();
		session.close();
	}
}
