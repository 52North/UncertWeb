#!/usr/bin/env python

import os
import sys
import string
import osgeo.osr

def main(argv = None):
	if argv is None:
		argv = sys.argv
	if len(argv) != 2:
		print('Usage: proj2wkt.py [Proj4 Projection Text]')
		return 1
	else:
		try:
			srs = osgeo.osr.SpatialReference()
			srs.ImportFromProj4(sys.argv[1])
			print(srs.ExportToWkt())
			return 0
		except:
			return 1

if __name__ == '__main__':
	sys.exit(main())
