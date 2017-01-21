00_centos_java:
	- builds a centos_java image with centos and java 8
	- this is sufficient to run the play app 
01_activator_build
	- builds centos_java_activator image with centos_java image and actovator (the play builder)
	- required for production deployment build, could be useful for a CI (Continuous Integration) tool too
	- we have two step builds because the first image rebuilds and takes a lot of time when we build with no-cache=true

