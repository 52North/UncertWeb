package org.uncertweb.utils;

import java.util.Arrays;
import java.util.Iterator;

public abstract class MultiDimensionalIterator<T> implements Iterator<T> {

	private int dim;
	private int[] size;
	private int[] index;

	public MultiDimensionalIterator() {
	}

	public MultiDimensionalIterator(int[] size) {
		setSize(size);
	}

	protected void setSize(int[] size) {
		this.size = size;
		this.dim = size.length;
		this.index = new int[size.length];
		Arrays.fill(index, 0);
	}

	public final int[] index() {
		return index;
	}

	public final int[] size() {
		return size;
	}

	public final int dim() {
		return dim;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean hasNext() {
		for (int i = 0; i < dim; ++i)
			if (index[i] >= size[i])
				return false;
		return true;
	}

	@Override
	public final T next() {
		T t = value(index);
		inc();
		return t;
	}

	protected final void inc() {
		for (int i = dim - 1; i >= 0; --i) {
			if (index[i] < size[i] - 1) {
				++index[i];
				break;
			} else if (i == 0) // don't start from the beginning
				index = size;
			else
				index[i] = 0;
		}
	}

	protected abstract T value(int[] index);
}