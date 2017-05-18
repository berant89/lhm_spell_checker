package util;

import java.util.Map;

/**
 * Implements the Entry interface from the Map class
 * @author berant89
 *
 * @param <K> The type of the Key
 * @param <V> The type of the Value
 */
public final class DLEntry<K, V> implements Map.Entry<K, V>{

	private K fKey;
	private V fValue;
	
	/**
	 * The constructor
	 * @param key The key to associate the value
	 * @param value The value associated to the key
	 */
	public DLEntry(K key, V value) {
		super();
		fKey = key;
		fValue = value;
	}
	
	@Override
	public K getKey() {
		return fKey;
	}

	@Override
	public V getValue() {
		return fValue;
	}

	@Override
	public V setValue(V value) {
		fValue = value;
		return fValue;
	}

}
