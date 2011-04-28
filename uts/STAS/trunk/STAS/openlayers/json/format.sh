#!/bin/sh

TMP=$(mktemp)

for f in *.js; do
	python -mjson.tool < $f > $TMP
	mv $TMP $f
done

rm -f $TMP
