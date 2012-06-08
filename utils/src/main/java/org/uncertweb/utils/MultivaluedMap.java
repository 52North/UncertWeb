package org.uncertweb.utils;

import java.util.List;
import java.util.Map;

public interface MultivaluedMap<K, V> extends Map<K, List<V>> {
	void putSingle(K key, V value);

	void add(K key, V value);

	void add(K key, Iterable<? extends V> values);

	<U extends Iterable<? extends V>> void addAll(Map<K, U> col);
	
	V getFirst(K key);

}