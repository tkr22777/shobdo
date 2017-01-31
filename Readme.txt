A Bangla Dictionary Webservice.
Micro-services can be found at the application directory.
Currect list of services:
1. dict-app:
	Public docker image can be found at 'stupefied/shobdo'
	To run dict-app locally:
		a. Using docker-compose:
			1. Install docker and docker-compose
			2. Go to application/dict-app directory
			3. Run 'docker-compose up'
		b. Using docker:
			1. Install docker 
			2. Run dict-app, mongo and redis containers individually from scripts directory
	This dict-app is build using:
		a. Play Java Framework
		b. MongoDB
		c. Redis (might eventually replace using Play cache it as most data is static and small)

