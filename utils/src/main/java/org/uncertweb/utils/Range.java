package org.uncertweb.utils;

public class Range {
	protected int from;
	protected int to;

	public Range(int limit) {
		this(limit, limit);
	}

	public Range(int start, int end) {
		from = start;
		to = end;
	}

	public boolean contains(int value) {
		return from <= value && to >= value;
	}
}