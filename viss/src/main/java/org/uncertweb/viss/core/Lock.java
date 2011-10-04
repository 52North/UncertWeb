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
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.uncertweb.viss.core.resource.IResource;
import org.uncertweb.viss.core.util.Utils;

public class Lock {
	private static Lock instance;

	public static synchronized Lock getInstance() {
		return (instance == null) ? instance = new Lock() : instance;
	}

	private Lock() {}

	private Map<UUID, Integer> useCounts = Utils.map();
	private ReentrantLock useCountsLock = new ReentrantLock();

	public void usingResources(boolean allowed) {

	}

	public boolean usingResource(IResource resource, boolean use) {
		useCountsLock.lock();
		try {
			if (use) {
				Integer count = useCounts.get(resource.getUUID());
				if (count != null && count.intValue() < 0) {
					return false;
				}
				count = Integer.valueOf((count == null) ? 1 : count.intValue() + 1);
				useCounts.put(resource.getUUID(), count);
				return true;
			} else {
				Integer count = useCounts.get(resource.getUUID());
				if (count == null || count.intValue() < 1) {
					return false;
				}
				count = Integer.valueOf(count.intValue() - 1);
				useCounts.put(resource.getUUID(), count);
				return true;
			}
		} finally {
			useCountsLock.unlock();
		}
	}

	public void deleteResources(IResource resource) {
		useCountsLock.lock();
		try {
			useCounts.remove(resource.getUUID());
		} finally {
			useCountsLock.unlock();
		}

	}

	public boolean deletingResource(IResource resource) {
		useCountsLock.lock();
		try {
			Integer count = useCounts.get(resource.getUUID());
			if (count == null || count.intValue() == 0) {
				count = Integer.valueOf(-1);
				useCounts.put(resource.getUUID(), count);
				return true;
			} else {
				return false;
			}
		} finally {
			useCountsLock.unlock();
		}
	}
}
