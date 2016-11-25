/*
 * (c) Copyright Ervacon 2016.
 * All Rights Reserved.
 */
package com.ervacon.bitemporal.support;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.InstantType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.threeten.extra.Interval;

/**
 * Persist {@link Interval} via Hibernate.
 * <p>
 * Based on <tt>org.joda.time.contrib.hibernate.PersistentInterval</tt>.
 */
public class PersistentInterval implements CompositeUserType {

	private static final String[] PROPERTY_NAMES = {"start", "end"};
	private static final Type[] TYPES = {StandardBasicTypes.TIMESTAMP, StandardBasicTypes.TIMESTAMP};

	@Override
	public String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}

	@Override
	public Type[] getPropertyTypes() {
		return TYPES;
	}

	@Override
	public Object getPropertyValue(Object component, int property) throws HibernateException {
		Interval interval = (Interval) component;
		return (property == 0) ? Date.from(interval.getStart()) : Date.from(interval.getEnd());
	}

	@Override
	public void setPropertyValue(Object component, int property, Object value) throws HibernateException {
		throw new UnsupportedOperationException("Immutable Interval");
	}

	@Override
	public Class returnedClass() {
		return Interval.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		}
		if (x == null || y == null) {
			return false;
		}
		return x.equals(y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		if (resultSet == null) {
			return null;
		}

		Instant start = InstantType.INSTANCE.nullSafeGet(resultSet, names[0], session);
		Instant end = InstantType.INSTANCE.nullSafeGet(resultSet, names[1], session);
		if (start == null || end == null) {
			return null;
		}
		return Interval.of(start, end);
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		Instant start = value != null ? ((Interval) value).getStart() : null;
		Instant end = value != null ? ((Interval) value).getEnd() : null;

		InstantType.INSTANCE.nullSafeSet(statement, start, index, session);
		InstantType.INSTANCE.nullSafeSet(statement, end, index + 1, session);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
		return cached;
	}

	@Override
	public Object replace(Object original, Object target, SessionImplementor session, Object owner)
			throws HibernateException {
		return original;
	}
}
