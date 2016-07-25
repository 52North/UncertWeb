/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24,
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.viss.core;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.bson.types.ObjectId;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.viss.core.resource.IResource;

public class Lock {
	private static Lock instance;
	private final Map<ObjectId, Integer> useCounts = UwCollectionUtils.map();
	private final ReentrantLock useCountsLock = new ReentrantLock();
    private Lock() {}

	public void usingResources(boolean allowed) {

	}

	public boolean usingResource(IResource resource, boolean use) {
		useCountsLock.lock();
		try {
			if (use) {
				Integer count = useCounts.get(resource.getId());
				if (count != null && count < 0) {
					return false;
				}
				count = (count == null) ? 1 : count + 1;
				useCounts.put(resource.getId(), count);
				return true;
			} else {
				Integer count = useCounts.get(resource.getId());
				if (count == null || count < 1) {
					return false;
				}
				count -= 1;
				useCounts.put(resource.getId(), count);
				return true;
			}
		} finally {
			useCountsLock.unlock();
		}
	}

	public void deleteResources(IResource resource) {
		useCountsLock.lock();
		try {
			useCounts.remove(resource.getId());
		} finally {
			useCountsLock.unlock();
		}

	}

	public boolean deletingResource(IResource resource) {
		useCountsLock.lock();
		try {
			Integer count = useCounts.get(resource.getId());
			if (count == null || count == 0) {
				count = -1;
				useCounts.put(resource.getId(), count);
				return true;
			} else {
				return false;
			}
		} finally {
			useCountsLock.unlock();
		}
	}
    public static synchronized Lock getInstance() {
        return (instance == null) ? instance = new Lock() : instance;
    }
}
