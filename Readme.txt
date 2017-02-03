A Bangla Dictionary Webservice.
Micro-services can be found at the application directory.
Currect list of services:
1. dict-app:
	The Public docker image 'stupefied/shobdo' can be found at docker hub.
	To run dict-app locally:
		1. Install docker and docker-compose
		2. Go to application/dict-app directory
		3. Run 'docker-compose up'
	This dict-app is build using:
		a. Play Java Framework
		b. MongoDB
		c. Redis (might eventually replace using Play cache it as most data is static and small)

