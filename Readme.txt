A Bangla Dictionary Webservice.
Micro-services can be found at the application directory.
Currect list of services:
	a. dict-app:
		How to run dic-app locally:
			1. Create the centor-java docker image from the docker directory of the dict-app
			2. Run docker containers:
				a. Using docker-compose:
					1. Requires you to have docker and docker-composed installed
					2. Run: $docker-compose up
				b. Using only docker:
					1. Requires you to have docker installed
					2. Run each of the dict-app, mongo and redis containers individually from the scrips directory

		This dict-app is build using:
			a. Play Java Framework
			b. MongoDB
			c. Redis (might eventually replace using Play cache it as most data is static and small)

