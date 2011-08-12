package org.uncertweb.viss.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.util.Utils;

public class Lock {
	private static Lock instance;

	public static synchronized Lock getInstance() {
		return (instance == null) ? instance = new Lock() : instance;
	}

	private Lock() {
	}

	private Map<UUID, Integer> useCounts = Utils.map();
	private ReentrantLock useCountsLock = new ReentrantLock();

	public void usingResources(boolean allowed) {

	}

	public boolean usingResource(Resource resource, boolean use) {
		useCountsLock.lock();
		try {
			if (use) {
				Integer count = useCounts.get(resource.getUUID());
				if (count != null && count.intValue() < 0) {
					return false;
				}
				count = Integer.valueOf((count == null) ? 1
						: count.intValue() + 1);
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

	public void deleteResources(Resource resource) {
		useCountsLock.lock();
		try {
			useCounts.remove(resource.getUUID());
		} finally {
			useCountsLock.unlock();
		}

	}

	public boolean deletingResource(Resource resource) {
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
