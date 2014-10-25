# (c) Copyright Ervacon 2010.
# All Rights Reserved.

"""
Unit tests for the bitemporal module.
"""

__author__  = "Erwin Vervaet"


import unittest
from bitemporal import *
from datetime import timedelta


class TimeFrameTest(unittest.TestCase):

	def test_no_reference_set(self):
		self.assertTrue(date.today() <= TimeFrame.reference_date())
		self.assertTrue(datetime.now() <= TimeFrame.reference_datetime())


	def test_set_reference(self):
		ref = datetime.now()
		TimeFrame.set_reference(ref)
		self.assertEquals(TimeFrame.reference_date(), ref.date())
		self.assertEquals(TimeFrame.reference_datetime(), ref)

	def test_clear_reference(self):
		ref = datetime.now()
		TimeFrame.set_reference(ref)
		TimeFrame.clear_reference()
		self.assertTrue(date.today() <= TimeFrame.reference_date())
		self.assertTrue(datetime.now() <= TimeFrame.reference_datetime())


class IntervalTest(unittest.TestCase):

	def test_empty_interval(self):
		interval = Interval(0, 0)
		self.assertFalse(-1 in interval);
		self.assertFalse(0 in interval);
		self.assertFalse(1 in interval);

		interval = Interval(1, 0);
		self.assertFalse(-1 in interval);
		self.assertFalse(0 in interval);
		self.assertFalse(1 in interval);
		self.assertFalse(2 in interval);

	def test_single_element_interval(self):
		interval = Interval(0, 1)
		self.assertFalse(-1 in interval);
		self.assertTrue(0 in interval);
		self.assertFalse(1 in interval);
		self.assertFalse(2 in interval);


	def test_non_empty_interval(self):
		interval = Interval(0, 10)
		self.assertFalse(-1 in interval);
		for x in range(10):
			self.assertTrue(x in interval);
		self.assertFalse(10 in interval);

	def test_str(self):
		self.assertEquals("[0, 5[", str(Interval(0, 5)))

	def test_eq(self):
		a = Interval(0, 10)
		self.assertEquals(a, Interval(0, 10))
		self.assertNotEquals(a, "foo")
		self.assertNotEquals(a, Interval(1, 10))
		self.assertNotEquals(a, Interval(0, 9))

	def test_eq_with_datetime(self):
		t = datetime.now()
		self.assertEquals(Interval(t, datetime.max), Interval(t, datetime.max))



class BitemporalWrapperTest(unittest.TestCase):

	def tearDown(self):
		TimeFrame.set_reference(None)

	def test_default_contruction(self):
		TimeFrame.set_reference(datetime.now())
		bw = BitemporalWrapper("value")
		self.assertEquals("value", bw.value)
		self.assertEquals(Interval(TimeFrame.reference_date(), date.max), bw.validity)
		self.assertEquals(Interval(TimeFrame.reference_datetime(), datetime.max), bw.recording)
			

	def test_explicit_construction(self):
		TimeFrame.set_reference(datetime.now())
		start = TimeFrame.reference_date() + timedelta(days = 5)
		end = TimeFrame.reference_date() + timedelta(days = 10)
		bw = BitemporalWrapper("value", Interval(start, end))
		self.assertEquals("value", bw.value) 
		self.assertEquals(Interval(start, end), bw.validity)
		self.assertEquals(Interval(TimeFrame.reference_datetime(), datetime.max), bw.recording)

	def test_end(self):
		start = datetime.now()
		TimeFrame.set_reference(start)
		bw = BitemporalWrapper("value")

		end = start + timedelta(5)
		TimeFrame.set_reference(end)
		bw.end()
		self.assertEquals("value", bw.value) 
		self.assertEquals(Interval(start.date(), date.max), bw.validity)
		self.assertEquals(Interval(start, end), bw.recording)



class BitemporalTraceTest(unittest.TestCase):

	def test_construction(self):
		trace = BitemporalTrace()
		self.assertEquals(0, len(trace.data))
		trace.add(BitemporalWrapper("value"))
		self.assertEquals(1, len(trace.data))

	def test_get(self):
		trace = BitemporalTrace()
		trace.add(BitemporalWrapper("value"))
		self.assertEquals("value", trace.get(date.today(), datetime.now())[0].value)
		self.assertEquals(0, len(trace.get(date.today() + timedelta(days = -3), datetime.now())))
		self.assertEquals(0, len(trace.get(date.today(), datetime.now() + timedelta(days = -3))))

	def test_get_history(self):
		trace = BitemporalTrace()
		trace.add(BitemporalWrapper("value"))
		self.assertEquals(1, len(trace.get_history(datetime.now())))
		self.assertEquals(0, len(trace.get_history(datetime.now() + timedelta(days = -3))))

	def test_get_evolution(self):
		trace = BitemporalTrace()
		trace.add(BitemporalWrapper("value"))
		self.assertEquals(1, len(trace.get_evolution(date.today())))
		self.assertEquals(0, len(trace.get_evolution(date.today() + timedelta(days = -3))))

	def test_add(self):
		trace = BitemporalTrace()
		trace.add(BitemporalWrapper("foo", Interval(date(2000, 1, 1), date.max)))
		self.assertEquals(1, len(trace.data))
		self.assertEquals("foo", trace.get(date.today(), datetime.now())[0].value)
		self.assertEquals(1, len(trace.get_history(datetime.now())))
		self.assertEquals(1, len(trace.get_evolution(date.today())))

		trace.add(BitemporalWrapper("bar", Interval(date(2005, 1, 1), date(2100, 1, 1))))
		self.assertEquals(4, len(trace.data))
		self.assertEquals("foo", trace.get(date(2000, 1, 1), datetime.now())[0].value)
		self.assertEquals("bar", trace.get(date.today(), datetime.now())[0].value)
		self.assertEquals(3, len(trace.get_history(datetime.now())))
		self.assertEquals(2, len(trace.get_evolution(date.today())))



class BitemporalPropertyTest(unittest.TestCase):

	def tearDown(self):
		TimeFrame.set_reference(None)

	def test_now(self):
		bp = BitemporalProperty()
		bp.assign("value")
		self.assertEquals("value", bp.now())

	def test_on(self):
		bp = BitemporalProperty()
		bp.assign("value")
		self.assertEquals("value", bp.on(date(2100, 1, 1)))

	def test_get(self):
		TimeFrame.set_reference(datetime.now())
		bp = BitemporalProperty()
		bp.assign("value")
		bt = bp.get()
		self.assertTrue(bt)
		self.assertEquals("value", bt.value)
		self.assertEquals(Interval(TimeFrame.reference_date(), date.max), bt.validity)
		self.assertEquals(Interval(TimeFrame.reference_datetime(), datetime.max), bt.recording)

	def test_get_history(self):
		bp = BitemporalProperty()
		bp.assign("value")
		self.assertEquals(1, len(bp.get_history()))

	def test_get_evolution(self):
		bp = BitemporalProperty()
		bp.assign("value")
		self.assertEquals(1, len(bp.get_evolution()))

	def test_assign(self):
		bp = BitemporalProperty()
		self.assertEquals(0, len(bp.trace.data))
		bp.assign("value")
		self.assertEquals(1, len(bp.trace.data))
	
	def test_end(self):
		bp = BitemporalProperty()
		bp.assign("value")
		self.assertTrue(bp.has_value())
		bp.end()
		self.assertFalse(bp.has_value())	

	def test_has_value(self):
		bp = BitemporalProperty()
		bp.assign("value")
		self.assertTrue(bp.has_value())
		self.assertTrue(bp.has_value(date(2100, 1, 1)))



class RawBitemporal:

	def __init__(self, validity):
		self.validity = validity
		self.recording = Interval(TimeFrame.reference_datetime(), datetime.max)

	def copyWith(self, validity):
		return RawBitemporal(validity)

	def end(self):
		self.recording = Interval(self.recording.start, TimeFrame.reference_datetime())

	def value(self):
		return "foo"

class RawBitemporalTest(unittest.TestCase):

	def test_raw_bitemporal_usage(self):
		bt = RawBitemporal(Interval(date(2000, 1, 1), date.max))
		trace = BitemporalTrace()
		trace.add(bt)
		self.assertEquals(1, len(trace.get(date.today(), datetime.now())))
		trace.add(RawBitemporal(Interval(date(2005, 1, 1), date.max)))
		self.assertEquals(2, len(trace.get_history(datetime.now())))
		self.assertEquals(2, len(trace.get_evolution(date.today())))


"""
The example scenario described on Wikipedia:
http://en.wikipedia.org/wiki/Temporal_database
"""
class Person:

	def __init__(self, name):
		self.name = name
		self.address = BitemporalProperty()
		self.alive = BitemporalProperty()

	def __str__(self):
		return self.name

class Address:

	def __init__(self, line):
		self.line = line

	def __eq__(self, other):
		return isinstance(other, Address) and self.line == other.line

	def __str__(self):
		return self.line

class ScenarioTest(unittest.TestCase):

	def tearDown(self):
		TimeFrame.set_reference(None)

	def test_scenario(self):
		# 3/4/1975 John Doe is born
		# nothing happens
		
		# 4/4/1975 John's father registers the baby
		TimeFrame.set_reference(date(1975, 4, 4))
		john_doe = Person("John Doe")
		john_doe.alive.assign(True, Interval(date(1975, 4, 3), date.max))
		john_doe.address.assign(Address("Smallville"), Interval(date(1975, 4, 3), date.max))

		# 26/8/1994 John moves to Bigtown, but forgets to register
		# nothing happens

		# 27/12/1994 John registers his move
		TimeFrame.set_reference(date(1994, 12, 27))
		john_doe.address.assign(Address("Bigtown"), Interval(date(1994, 8, 26), date.max))

		# 1/4/2001 John is killed in an accident, reported by the coroner that same day
		TimeFrame.set_reference(date(2001, 4, 1))
		john_doe.alive.assign(False)

		# tests
		TimeFrame.set_reference(date(2007, 1, 1))

		self.assertFalse(john_doe.alive.has_value(date(1975, 1, 1)))
		self.assertTrue(john_doe.alive.on(date(1975, 4, 3)))
		self.assertFalse(john_doe.alive.has_value(date(1975, 4, 3), datetime(1975, 4, 3)))
		self.assertFalse(john_doe.alive.now())

		self.assertEquals(Address("Smallville"), john_doe.address.on(date(1975, 4, 3)))
		self.assertEquals(Address("Bigtown"), john_doe.address.on(date(1994, 8, 26)))
		self.assertEquals(Address("Smallville"), john_doe.address.on(date(1994, 8, 26), datetime(1994, 8, 26)))
		self.assertEquals(Address("Bigtown"), john_doe.address.on(date(1994, 8, 26), datetime(1994, 12, 27)))
		self.assertEquals(Address("Bigtown"), john_doe.address.now())





if __name__ == "__main__":
	unittest.main()
