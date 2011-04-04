package de.ifgi.airbase.feeder.util;

/**
 * Simple class to encapsulate a pair of values.
 * 
 * @author Christian Autermann
 * 
 * @param <U>
 *            the first element
 * @param <V>
 *            the second element
 */
public class Tuple<U, V> {
	private U elem1 = null;
	private V elem2 = null;

	/**
	 * Creates a new {@code Tuple} with {@code null} values.
	 */
	public Tuple() {
	    //
	}

	/**
	 * Creates a new {@code Tuple} with specified values.
	 * 
	 * @param elem1
	 *            the elem1
	 * @param elem2
	 *            the elem2
	 */
	public Tuple(U elem1, V elem2) {
		this.setElem1(elem1);
		this.setElem2(elem2);
	}

	/**
	 * @param elem1
	 *            the elem1 to set
	 */
	public void setElem1(U elem1) {
		this.elem1 = elem1;
	}

	/**
	 * @return the elem1
	 */
	public U getElem1() {
		return this.elem1;
	}

	/**
	 * @param elem2
	 *            the elem2 to set
	 */
	public void setElem2(V elem2) {
		this.elem2 = elem2;
	}

	/**
	 * @return the elem2
	 */
	public V getElem2() {
		return this.elem2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getElem1() == null) ? 0 : getElem1().hashCode());
		result = prime * result
				+ ((getElem2() == null) ? 0 : getElem2().hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null)
			return this.hashCode() == obj.hashCode();
		return false;
	}

}
