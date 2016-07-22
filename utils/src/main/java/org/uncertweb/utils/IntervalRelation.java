package org.uncertweb.utils;

import java.util.Set;

import org.joda.time.ReadableInterval;

/**
 * Interval relations used by James F. Allen
 * @author Christian Autermann
 *
 */
public enum IntervalRelation {
	PRECEDES		(1 <<  0),
	PRECEDES_BY		(1 <<  1),
	CONTAINS		(1 <<  2),
	DURING			(1 <<  3),
	OVERLAPS		(1 <<  4),
	OVERLAPPED_BY	(1 <<  5),
	MEETS			(1 <<  6),
	MET_BY			(1 <<  7),
	STARTS			(1 <<  8),
	STARTED_BY		(1 <<  9),
	FINISHES		(1 << 10),
	FINISHED_BY		(1 << 11),
	EQUALS			(1 << 12);

	private final int rel;

	public int getMod() {
		return this.rel;
	}

	private IntervalRelation(int rel) {
		this.rel = rel;
	}

	private boolean is(int rel) {
		return (rel & getMod()) != 0;
	}

	public static boolean isPrecedes(int rel) {
		return PRECEDES.is(rel);
	}

	public static boolean isPrecedes(ReadableInterval i1, ReadableInterval i2) {
		return PRECEDES.is(i1, i2);
	}

	public static boolean isPrecededBy(int rel) {
		return PRECEDES_BY.is(rel);
	}

	public static boolean isPrecededBy(ReadableInterval i1, ReadableInterval i2) {
		return PRECEDES_BY.is(i1, i2);
	}

	public static boolean isDuring(int rel) {
		return DURING.is(rel);
	}

	public static boolean isDuring(ReadableInterval i1, ReadableInterval i2) {
		return DURING.is(i1, i2);
	}

	public static boolean isContains(int rel) {
		return CONTAINS.is(rel);
	}

	public static boolean isContains(ReadableInterval i1, ReadableInterval i2) {
		return CONTAINS.is(i1, i2);
	}

	public static boolean isOverlaps(int rel) {
		return OVERLAPS.is(rel);
	}

	public static boolean isOverlaps(ReadableInterval i1, ReadableInterval i2) {
		return OVERLAPS.is(i1, i2);
	}

	public static boolean isOverlappedBy(int rel) {
		return OVERLAPPED_BY.is(rel);
	}

	public static boolean isOverlappedBy(ReadableInterval i1,
			ReadableInterval i2) {
		return OVERLAPPED_BY.is(i1, i2);
	}

	public static boolean isMeets(int rel) {
		return MEETS.is(rel);
	}

	public static boolean isMeets(ReadableInterval i1, ReadableInterval i2) {
		return MEETS.is(i1, i2);
	}

	public static boolean isMetBy(int rel) {
		return MET_BY.is(rel);
	}

	public static boolean isMetBy(ReadableInterval i1, ReadableInterval i2) {
		return MET_BY.is(i1, i2);
	}

	public static boolean isStarts(int rel) {
		return STARTS.is(rel);
	}

	public static boolean isStarts(ReadableInterval i1, ReadableInterval i2) {
		return STARTS.is(i1, i2);
	}

	public static boolean isStartedBy(int rel) {
		return STARTED_BY.is(rel);
	}

	public static boolean isStartedBy(ReadableInterval i1, ReadableInterval i2) {
		return STARTED_BY.is(i1, i2);
	}

	public static boolean isFinishes(int rel) {
		return FINISHES.is(rel);
	}

	public static boolean isFinishes(ReadableInterval i1, ReadableInterval i2) {
		return FINISHES.is(i1, i2);
	}

	public static boolean isFinishedBy(int rel) {
		return FINISHED_BY.is(rel);
	}

	public static boolean isFinishedBy(ReadableInterval i1, ReadableInterval i2) {
		return FINISHED_BY.is(i1, i2);
	}

	public static boolean isEquals(int rel) {
		return EQUALS.is(rel);
	}

	public static boolean isEquals(ReadableInterval i1, ReadableInterval i2) {
		return EQUALS.is(i1, i2);
	}

	public static int inverse(int rel) {
		return rel ^ 0x7FF;
	}

	public static Set<IntervalRelation> getRelations(int rel) {
		Set<IntervalRelation> rels = UwCollectionUtils.set();
		for (IntervalRelation r : values()) {
            if (r.is(rel)) {
                rels.add(r);
            }
        }
		return rels;
	}

	public static Set<IntervalRelation> getRelations(ReadableInterval i1, ReadableInterval i2) {
		return getRelations(getMod(i1, i2));
	}

	public static int getMod(ReadableInterval i1, ReadableInterval i2) {
		return getMod(i1.getStartMillis(), i1.getEndMillis(), i2.getStartMillis(), i2.getEndMillis());
	}

	public static int getMod(long s1, long e1, long s2, long e2) {
		int mod = 0;
		for (IntervalRelation r : values()) {
			mod |= r.is(s1, e1, s2, e2) ? r.getMod() : 0;
		}
		return mod;
	}

	public boolean is(ReadableInterval i1, ReadableInterval i2) {
		return is(i1.getStartMillis(), i1.getEndMillis(), i2.getStartMillis(), i2.getEndMillis());
	}

	public boolean is(long s1, long e1, long s2, long e2) {
		if (s1 > e1 || s2 > e2) {
            return false;
        }
		switch (this) {
		case EQUALS: return s1 == s2 && e1 == e2;
		case PRECEDES: return e1 < s2;
		case MEETS: return e1 == s2;
		case STARTS: return s1 == s2 && e1 < e2;
		case DURING: return s2 < s1 && e1 < e2;
		case FINISHES: return s2 < s1 && e1 == e2;
		case OVERLAPS: return s1 < s2 && e1 < e2;
		case FINISHED_BY: return FINISHES.is(s2, e2, s1, e1);
		case PRECEDES_BY: return PRECEDES.is(s2, e2, s1, e1);
		case MET_BY: return MEETS.is(s2, e2, s1, e1);
		case STARTED_BY: return STARTS.is(s2, e2, s1, e1);
		case CONTAINS: return DURING.is(s2, e2, s1, e1);
		case OVERLAPPED_BY: return OVERLAPS.is(s2, e2, s1, e1);
		default:
			return false;
		}
	}
}