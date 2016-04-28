#!/usr/bin/env python
import sys
import fileinput

mode = 0
counter = 0

for line in fileinput.input():
	if line.startswith("[INFO] Reactor Summary:"):
		print
		print
		mode = 1
	if mode == 1:
		sys.stdout.write(line)
	else:
		if line.startswith("[INFO] Building") and "jar:" not in line and "zip:" not in line:
			print
			sys.stdout.write(line.replace("\n", " ").replace("\r", ""))
			sys.stdout.flush()
			counter = 0
		else:
			counter = (counter + 1) % 10
			if counter == 0:
				sys.stdout.write(".")
	sys.stdout.flush()
