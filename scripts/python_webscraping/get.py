#!/usr/bin/python
import urllib2
#pages=435
#url = 'http://dsalsrv02.uchicago.edu/cgi-bin/romadict.pl?table=biswas-bengali&page=%s&display=utf8' % (i)
#pages=464
#url = 'http://dsalsrv02.uchicago.edu/cgi-bin/romadict.pl?table=biswas-bangala&page=%s&display=utf8' % (i)
pages=464
response_str=""
for i in range(1, pages+1):
	print "Page:%s" % (i)
	url = 'http://dsalsrv02.uchicago.edu/cgi-bin/romadict.pl?table=biswas-bangala&page=%s&display=utf8' % (i)
	print "URL: %s" % (url)
	response = urllib2.urlopen(url)
	response_str = response_str + "NEW_SIN_PAGE_BANGLA_TO_BANGLA" + response.read()

with open('DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt' , 'w') as f:
	f.write(response_str)
	f.close()
