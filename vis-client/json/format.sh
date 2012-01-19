#!/bin/sh

TMP=`mktemp`

format() {
	python -mjson.tool < "$1" > "$TMP"
	mv "$TMP" "$1"
}
if [ -z "$1" ]; then
	for f in *.js; do
		format "$f"
	done
else 
	format "$1"
fi

rm -f "$TMP"
