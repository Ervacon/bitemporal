# (c) Copyright Ervacon 2010.
# All Rights Reserved.

"""
A bitemporality framework for Python 2.5.

Introduction:

The code in this project provides a simple and elegant approach to dealing with bitemporal issues
in rich domain models. While the user (application) level API is fluent and straight forward, it
also provides you with full access to temporal information when required.

At this moment, this code is not part of a real project, and as such it is not activly worked on or
developed. Still, it provides a good starting point for those that need to tackle temporal issues
in their applications.

If you find any bugs or have suggestions or remarks related to this code, feel free to send an e-mail
to: bitemporal@ervacon.com.

Release info:

The latest version of this code can always be obtained from the Ervacon Subversion repository located at
the following URL: https://svn.ervacon.com/public/projects/bitemporal/

The code is released under a BSD style license (see license.txt).

Where to start?

A good starting point are the included unit tests which demonstrate usage of the code.

If you want to learn more about temporal databases and related topics, check out the following resources:
http://en.wikipedia.org/wiki/Temporal_database
http://www.martinfowler.com/eaaDev/timeNarrative.html
"""

__author__  = "Erwin Vervaet"


from datetime import date
from datetime import time
from datetime import datetime


class TimeFrame:
	"""Provides time frame functionality.
	"""

	@classmethod
	def set_reference(cls, value):
		"""Set the reference datetime of the time frame.

		Only accepts date and datetime arguments. A date argument will automatically be converted to a datetime.
		"""
		if type(value) == date:
			cls.reference = datetime.combine(value, time())
		else:
			cls.reference = value

	@classmethod
	def clear_reference(cls):
		"""Clear the reference datetime of the time frame.

		Subsequent calls to the time frame will return the current datetime.
		"""
		cls.reference = None

	# internal
	@classmethod
	def _get_reference(cls):
		"""Returns the reference datetime of the time frame.

		Will always return a datetime object. If no reference datetime was previously set, the current datetime
		will be returned.
		"""
		try:
			return cls.reference or datetime.now()
		except AttributeError:
			return datetime.now()

	@classmethod
	def reference_date(cls):
		"""Returns the reference datetime as a date object.
		"""
		return cls._get_reference().date()

	@classmethod
	def reference_datetime(cls):
		"""Returns the reference datetime object.
		"""
		return cls._get_reference()



class Interval:
	"""A half open interval: [start, end[.
	"""

	def __init__(self, start, end):
		"""Create a new interval.

		The lower bound of the interval (the start) is included in the interval, the upper bound (the end) is not.
		"""
		assert type(start) == type(end)
		self.start = start
		self.end = end

	def overlaps(self, other):
		"""Does this interval overlap with given interval?
		"""
		return self.start < other.end and other.start < self.end

	def __contains__(self, x):
		return self.start <= x < self.end

	def __str__(self):
		return "[" + str(self.start) + ", " + str(self.end) + "["

	def __eq__(self, other):
		return isinstance(other, Interval) and self.start == other.start and self.end == other.end



class BitemporalWrapper:
	"""Decorates an value object with bitemporal properties: a validity and recording interval.

	The validity interval indicates when a particular value is valid, while the recording interval tracks
	when the value is known. Tracking information bitemporally allows you to aswer questions along
	the lines of "On january second 1999, what did we think the valid value was for september first 1980?".

	A bitemporally wrapped object can be tracked in a BitemporalTrace.
	Due to the nature of bitemporality, the wrapped value should be immutable. The value itself will never change,
	instead new values will be added to a BitemporalTrace to represent changes in the value.
	A BitemporalWrapper itself is not immutable, its recording interval can be ended.
	"""
	
	def __init__(self, value, validity = None):
		"""Wrap given value with bitemporal properties.

		If not specified, the validity range will be from the reference date till the end of time.
		The recording interval will be from the reference datatime till the end of time.
		"""
		self.value = value
		self.validity = validity or Interval(TimeFrame.reference_date(), date.max)
		self.recording = Interval(TimeFrame.reference_datetime(), datetime.max)

	def copyWith(self, validity):
		"""Return a new BitemporalWrapper wrapping the same value as this BitemporalWrapper, but
		using given validity interval. The recording interval will be from the reference datatime
		till the end of time.
		"""
		return BitemporalWrapper(self.value, validity)
	
	def end(self):
		"""End the recording range of this wrapper on the reference datetime (now).
		"""
		self.recording = Interval(self.recording.start, TimeFrame.reference_datetime())

	def __str__(self):
		return str(self.validity) + " ~ " + str(self.recording) + " ~ " + str(self.value)



class BitemporalTrace:
	"""A trace of bitemporal data.

	A bitemporal trace manages a list of bitemporal trace data. It allows consultation and modification of
	the trace data while maintaining bitemporal invariants.
	The objects managed by a BitemporalTrace should have bitemporal properties: a .validity and .recording
	property, both of type Interval, a .value property providing the value, an end() method ending
	the recording interval of the bitemporal and a copyWith() method to copy the object with a new validity
	interval. Typically all of this is provided by wrapping a value object in a BitemporalWrapper.
	"""

	def __init__(self, data = []):
		"""Create a new bitemporal trace, potentially using a given list of trace data, which will
		be copied.
		"""
		self.data = data[:];

	def get(self, valid_on, known_on):
		"""Returns the bitemporals valid on given date as known on given datetime.
		"""
		return filter(lambda bt: valid_on in bt.validity and known_on in bt.recording, self.data)

	def get_history(self, known_on):
		"""Returns the history list as known on given datetime.

		The history informs you about how the valid value changed over time.
		"""
		return filter(lambda bt: known_on in bt.recording, self.data)

	def get_evolution(self, valid_on):
		"""Returns the evolution list of given validity date.

		The evolution informs you about how knowledge about the value valid at a certain date evolved.
		"""
		return filter(lambda bt: valid_on in bt.validity, self.data)

	def add(self, bt):
		"""Add given bitemporal to this trace, manipulating the existing trace data to maintain bitemporal
		invariants.
		"""
		self._sanity_check()

		to_end = []
		to_add = []

		for possible_overlap in self.get_history(TimeFrame.reference_datetime()):
			if bt.validity.overlaps(possible_overlap.validity):
				to_end.append(possible_overlap)

		for valid_on_start_of_bt in self.get(bt.validity.start, TimeFrame.reference_datetime()):
			if bt.validity.start > valid_on_start_of_bt.validity.start:
				new_validity = Interval(valid_on_start_of_bt.validity.start, bt.validity.start)
				to_add.append(valid_on_start_of_bt.copyWith(new_validity))

		if bt.validity.end < date.max:
			for valid_on_end_of_bt in self.get(bt.validity.end, TimeFrame.reference_datetime()):
				assert valid_on_end_of_bt.validity.end > bt.validity.end
				if valid_on_end_of_bt.validity.start < bt.validity.end :
					new_validity = Interval(bt.validity.end, valid_on_end_of_bt.validity.end)
					to_add.append(valid_on_end_of_bt.copyWith(new_validity))

		for needs_to_end in to_end:
			needs_to_end.end()
		for needs_to_be_added in to_add:
			self.data.append(needs_to_be_added)

		# copy the bitemporal to make sure we have a fresh recording interval
		self.data.append(bt.copyWith(bt.validity))

	def _sanity_check(self):
		for bt in self.data:
			if bt.recording.start > TimeFrame.reference_datetime():
				raise Exception("Cannot manipulate bitemporal trace; trace contains data from the future")

	def __str__(self):
		return str([str(bt) for bt in self.data])



class BitemporalProperty:
	"""A bitemporal property of a class.

	The BitemporalProperty class provides a high-level API expressed in terms of actual values,
	layered on top of low-level contructs such as a BitemporalTrace and a BitemporalWrapper.
	"""

	def __init__(self, trace = None):
		"""Create a new bitemporal property backed by given trace data.
		"""
		self.trace = trace or BitemporalTrace()

	def now(self):
		"""Returns the value valid and known now.
		"""
		bt = self.get()
		if bt:
			return bt.value
		else:
			return None

	def on(self, valid_on, known_on = None):
		"""Returns the value valid on specified date and known on specified datetime.
		"""
		bt = self.get(valid_on, known_on or TimeFrame.reference_datetime())
		if bt:
			return bt.value
		else:
			return None

	def get(self, valid_on = None, known_on = None):
		"""Returns the bitemporal valid on specified date and known on specified datetime.
		"""
		bts = self.trace.get(valid_on or TimeFrame.reference_date(), known_on or TimeFrame.reference_datetime())
		if bts:
			return bts[0]
		else:
			return None

	def get_history(self, known_on = None):
		"""Returns the history of this property as known on specified datetime.
		"""
		return self.trace.get_history(known_on or TimeFrame.reference_datetime())

	def get_evolution(self, valid_on = None):
		"""Returns the evolution of this property for given validity date.
		"""
		return self.trace.get_evolution(valid_on or TimeFrame.reference_date())

	def assign(self, value, validity = None):
		"""Assign a new value, with specified validity, to this property
		"""
		self.trace.add(BitemporalWrapper(value, validity or Interval(TimeFrame.reference_date(), date.max)))

	def end(self, valid_on = None):
		"""End the value valid on specified date, as known now. This will essentially forget that value.
		"""
		for bt in self.trace.get(valid_on or TimeFrame.reference_date(), TimeFrame.reference_datetime()):
			bt.end()

	def has_value(self, valid_on = None, known_on = None):
		"""Returns whether or not a valid and known value is available for specified validity date
		and recording datetime.
		"""
		return len(self.trace.get(valid_on or TimeFrame.reference_date(), known_on or TimeFrame.reference_datetime())) > 0

	def __str__(self):
		return str(self.now())

