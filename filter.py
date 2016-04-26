#!/usr/bin/env python
import sys
import fileinput

mode = 0

for line in fileinput.input():
	if line.startswith("[INFO] Reactor Summary:"):
		print
		print
		mode = 1
	if mode == 1:
		sys.stdout.write(line)
	else:
		if line.startswith("[INFO] Building") and "jar:" not in line and "zip:" not in line:
			sys.stdout.write(line)
	sys.stdout.flush()
		
		


#oll = 0

#for line in fileinput.input():
#	if oll > 0:
#		sys.stdout.write(oll * "\b")
#		sys.stdout.write(oll * " ")
#		sys.stdout.write(oll * "\b")
#	if line.startswith("[INFO] Download"):
#        	sys.stdout.write(line.replace("\n", ""))
#		oll = len(line)
#	else:
#		sys.stdout.write(line)
#		oll = 0
#	sys.stdout.flush()
