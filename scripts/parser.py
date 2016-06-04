#!/usr/bin/python
import sys

dict_file = open("../data/DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt","r")
LINES_TO_TEST = 20
DEBUG = False
INFO = False

def readline(dict_file):
	line = dict_file.readline()
	if INFO: sys.stdout.write("#" + str(i) + ":" +  line)
	return line

def getWordEndPos(line):
	return line.index('[')

def getEngEndPos(line):
	return line.index(']')

def getWord(line):
	WORD_END_POS = getWordEndPos(line)
	if DEBUG:
		print "Word end position:%d" % (WORD_END_POS)
	word = line[:WORD_END_POS].strip()
	return word
	#return word.split(', ')

def getEng(line):
	WORD_END_POS = getWordEndPos(line)
	ENG_END_POS = getEngEndPos(line)
	eng = line[WORD_END_POS+1:ENG_END_POS].strip()
	return eng
	#return eng.split(', ')

def theRest(line):
	ENG_END_POS = getEngEndPos(line)
	the_rest = line[ENG_END_POS + 1:].strip()
	print "The Rest\t\t:'%s'" %(the_rest)

for i in range(0,LINES_TO_TEST):
	line = readline(dict_file)
	word = getWord(line)
	print "Bangla Word\t\t#%d:\t'%s'" %(i,word)
	eng = getEng(line)
	print "English Pronunciation\t#%d:\t'%s'" %(i,eng)
	theRest(line)
