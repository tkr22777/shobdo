#!/usr/bin/python
import sys, re

DEBUG = False
INFO = True
TypeTable = {}
i = 0

def getWordEndPos(line):
	return line.index('[')

def getEngEndPos(line):
	return line.index(']')

def getWord(line):
	WORD_END_POS = getWordEndPos(line)
	if DEBUG:
		print("Word end position:%d" % (WORD_END_POS))
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
	if DEBUG: print("The(Rest\t\t:'%s'" %(the_rest))
	return the_rest

def findType(a_definition):

	count = len(the_rest)
	try:
		count = the_rest.index('. ') + 1;
	except Exception:
		if DEBUG: print("Found Error For DotSpace(. )")
		count = len(the_rest)

	substring = the_rest[:count]

	if substring in table:
		table[substring] = table[substring] + 1
		'''
		if table[substring] is 5:
			print "-->Type:%s" % substring
			print line
		'''
	else:
		#print the_rest
		table[substring] = 0

regex='<b>[^0-9]*</b>'
regex_complied = re.compile(regex)
string = 'Hey Jude<b>1</b> <b>T</b> <b>32</b>'

def getDifferntDefinitions(definitions):
	return regex_complied.split(definitions)


def main():

	j = 0

	file_loc = "../data/DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt"
	file_loc = "/Users/tahsin/Dropbox/Work/shobdo/resources/DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt"

	dict_file = open(file_loc, "r")

	LINES_TO_TEST = 22000

	lines = []

	for i in range(LINES_TO_TEST):
		lines.append(dict_file.readline())

	#lines.sort()

	print(["*"] * 20)

	line = lines[1]

	charMap = {}

	lineLen = {}

	for line in lines:
		lineRange = int(len(line) / 10)
		if lineRange not in lineLen:
			lineLen[lineRange] = [line]
		else:
			lineLen[lineRange].append(line)

		for char in line:
			if char not in charMap:
				charMap[char] = 1
			else:
				charMap[char] = charMap[char] + 1

	for key in lineLen:
		if key == 90:
			print(key)
			lines = lineLen[key]
			for line in lines:
				print(line)

	"""
		print("Word #%d:" %(i) + " line:" + line)
		the_rest = theRest(line)

		if len(getDifferntDefinitions(the_rest)) < 2:

			j = j + 1

			word = getWord(line)
			if INFO: print("Bangla Word\t\t#%d:\t'%s'" %(i,word))

			eng = getEng(line)
			if INFO: print("English Pronunciation\t#%d:\t'%s'" %(i,eng))

			if INFO: print("Transaltion\t\t#%d:\t'%s'" %(i,the_rest))
			print
	"""
				
if __name__ == "__main__": main()
