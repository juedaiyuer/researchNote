/*
    GNU LESSER GENERAL PUBLIC LICENSE
    Copyright (C) 2006 The Lobo Project

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Contact info: xamjadmin@users.sourceforge.net
*/
package org.lobobrowser.util;

import java.util.*;

/**
 * A cache with least-recently-used policy.
 * Note that this class is not thread safe by itself.
 */
public class LRUCache implements java.io.Serializable {
    private static final long serialVersionUID = 940427225784212823L;	
	private final int approxMaxSize;
	private final Map cacheMap = new HashMap();
	
	/**
	 * Ascending timestamp order. First is least recently used.
	 */
	private final TreeSet timedSet = new TreeSet();
	private int currentSize = 0;
	
	public LRUCache(int approxMaxSize) {
		this.approxMaxSize = approxMaxSize;
	}
	
	public void put(Object key, Object value, int approxSize) {
		if(approxSize > this.approxMaxSize) {
			throw new IllegalArgumentException("Max size is " + this.approxMaxSize);
		}
		OrderedValue ordVal = (OrderedValue) this.cacheMap.get(key);
		if(ordVal != null) {
			this.currentSize += (approxSize - ordVal.approximateSize);
			this.timedSet.remove(ordVal);
			ordVal.approximateSize = approxSize;
			ordVal.value = value;
			ordVal.touch();
			this.timedSet.add(ordVal);
		}
		else {
			ordVal = new OrderedValue(value, approxSize);
			this.cacheMap.put(key, ordVal);
			this.timedSet.add(ordVal);
			this.currentSize += approxSize;
		}
		while(this.currentSize > this.approxMaxSize) {
			this.removeLRU();
		}
	}

	private void removeLRU() {
		OrderedValue ordVal = (OrderedValue) this.timedSet.first();
		if(ordVal != null) {
			if(this.timedSet.remove(ordVal)) {
				this.currentSize -= ordVal.approximateSize;
			}
			else {
				throw new IllegalStateException("Could not remove existing tree node.");
			}
		}
		else {
			throw new IllegalStateException("Cannot remove LRU since the cache is empty.");
		}
	}
	
	public Object get(Object key) {
		OrderedValue ordVal = (OrderedValue) this.cacheMap.get(key);
		if(ordVal != null) {
			this.timedSet.remove(ordVal);
			ordVal.touch();
			this.timedSet.add(ordVal);
			return ordVal.value;
		}
		else {
			return null;
		}
	}
	
	private class OrderedValue implements Comparable, java.io.Serializable {
	    private static final long serialVersionUID = 340227625744215821L;	
		private long timestamp;
		private int approximateSize;
		private Object value;

		private OrderedValue(Object value, int approxSize) {
			this.value = value;
			this.approximateSize = approxSize;
			this.touch();
		}
		
		private final void touch() {
			this.timestamp = System.currentTimeMillis();
		}

		public int compareTo(Object arg0) {
			if(this == arg0) {
				return 0;
			}
			OrderedValue other = (OrderedValue) arg0;
			long diff = this.timestamp - other.timestamp;
			if(diff != 0) {
				return (int) diff;
			}
			int hc1 = System.identityHashCode(this);
			int hc2 = System.identityHashCode(other);
			if(hc1 == hc2) {
				hc1 = System.identityHashCode(this.value);
				hc2 = System.identityHashCode(other.value);
			}
			return hc1 - hc2;
		}
	}
}
