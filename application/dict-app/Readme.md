The Shobdo backend webservice:
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