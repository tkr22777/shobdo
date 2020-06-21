Notice:
=======

This service is now in dev mode. Things may be broken. 
TODO: fix everything and update this readme.
Okay cool!

If you a dev, follow along.
If you want to build using command line:
- Install sbt
- Install docker
- Spin up the mongo docker from scripts directory 
- Run: sbt clean; sbt compile; sbt test;

If you want to import to IntelliJ and develop on IntelliJ:
- Use java 8 when importing to IntelliJ. 
- Install routes file plugin on IntelliJ.
- Allow annotation processing on IntelliJ.
- Spin up the mongo docker from scripts directory 
- All tests should now pass!

The Shobdo webservice:
==============================

This file will be packaged with your application when using `activator dist`.

Controllers:
============

- WordController.java:

  Shows how word CRUDL API requests are handled.
  
Application Logic:
==================

Caches:
=======

- WordCache.java:

  Uses redis to words and search results
  
Data Access Objects (DAOs):
===========================

- WordDao.java:
  Interface to data layer for word object's CRUDL and search operations

Objects:
========

- Filters.java:

  Creates the list of HTTP filters used by your application.

- ExampleFilter.java

  A simple filter that adds a header to every response.