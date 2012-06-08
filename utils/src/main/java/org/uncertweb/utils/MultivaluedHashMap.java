package org.uncertweb.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MultivaluedHashMap<K, V> extends HashMap<K, List<V>> implements
		MultivaluedMap<K, V> {
	private static final long serialVersionUID = 1964378219421775389L;

	@Override
	public void putSingle(K key, V value) {
		List<V> l = getList(key);
		l.clear();
		if (value != null)
			l.add(value);
	}

	@Override
	public void add(K key, Iterable<? extends V> values) {
		List<V> l = getList(key);
		if (values != null)
			for (V v : values)
				l.add(v);

	}

	@Override
	public void add(K key, V value) {
		List<V> l = getList(key);
		if (value != null)
			l.add(value);

	}

	@Override
	public V getFirst(K key) {
		List<V> values = get(key);
		if (values != null && values.size() > 0)
			return values.get(0);
		else
			return null;
	}

	private List<V> getList(K key) {
		List<V> l = get(key);
		if (l == null) {
			l = new LinkedList<V>();
			put(key, l);
		}
		return l;
	}
	
	public static <X,Y> MultivaluedHashMap<X,Y> create(){
		return new MultivaluedHashMap<X,Y>();
	}

}