com.ervacon.bitemporal
======================

A simple and elegant approach to dealing with bitemporal issues in rich domain models.

# Introduction

The code in this project provides a simple and elegant approach to dealing with bitemporal issues
in rich domain models. While the user (application) level API is fluent and straight forward, it
also provides you with full access to temporal information when required. Furthermore, you can still
persist your core domain objects, and their bitemporal properties using a capable ORM product such
as Hibernate.

At this moment, this code is not part of a real project, and as such it is not activly worked on or
developed. Still, it provides a good starting point for those that need to tackle temporal issues
in their applications.

If you find any bugs or have suggestions or remarks related to this code, feel free to send an e-mail
to: bitemporal@ervacon.com.

# Release info

Java 5 (JDK 1.5) or later is required to build and use the code in this project. Joda Time
(http://www.joda.org/joda-time/) is used internally, and is the only external dependency.

The project is packaged as a simple Maven 2 project (http://maven.apache.org/). Simply executing 'mvn package'
in the project directory will build the project and package it as a jar file.

The latest version of this code can always be obtained from the Ervacon GitHub repository located at
the following URL: https://github.com/klr8/bitemporal

The code is released under a BSD style license (see LICENSE).

# Where to start?

The slide deck for the "Temporal Issues in a Rich Domain Model" session presented by Erwin Vervaet at
The Spring Experience 2007 is included in the 'doc/' directory. These slides document the code in this project
and provide a general introduction to dealing with temporal issues in rich domain models. The included unit
tests also demonstrate usage of the code.

If you want to learn more about temporal databases and related topics, start here:
http://en.wikipedia.org/wiki/Temporal_database
http://www.martinfowler.com/eaaDev/timeNarrative.html
