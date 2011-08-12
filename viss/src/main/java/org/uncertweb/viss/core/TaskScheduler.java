package org.uncertweb.viss.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskScheduler<T> {

	private class WorkerThread extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					Callable<T> task = getNextTask();
					T result = task.call();
					resultLock.lock();
					try {
						results.put(task, result);
						newResult.signalAll();
					} finally {
						resultLock.unlock();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private Lock queueLock = new ReentrantLock();
	private Lock resultLock = new ReentrantLock();
	private Condition newResult = resultLock.newCondition();
	private Condition notEmpty = queueLock.newCondition();
	private Queue<Callable<T>> lowPriority = new LinkedBlockingDeque<Callable<T>>();
	private Queue<Callable<T>> highPriority = new LinkedBlockingDeque<Callable<T>>();
	private HashMap<Callable<T>, T> results = new HashMap<Callable<T>, T>();

	public TaskScheduler(int threadCount) {
		while (--threadCount >= 0) {
			new WorkerThread().start();
		}
	}

	public void submitAll(Collection<? extends Callable<T>> col) {
		for (Callable<T> c : col) {
			submitTask(c);
		}
	}

	public void submitTask(Callable<T> c) {
		queueLock.lock();
		try {
			lowPriority.add(c);
			notEmpty.signal();
		} finally {
			queueLock.unlock();
		}
	}

	public void cancelAll(Collection<Callable<T>> col) {
		for (Callable<T> c : col) {
			cancel(c);
		}
	}

	public void cancel(Callable<T> c) {
		queueLock.lock();
		try {
			if (!lowPriority.remove(c)) {
				if (!highPriority.remove(c)) {
					resultLock.lock();
					try {
						results.remove(c);
					} finally {
						resultLock.unlock();
					}
				}
			}
		} finally {
			queueLock.unlock();
		}
	}

	public T retrieveResult(Callable<T> c) {
		T result;
		resultLock.lock();
		try {
			result = results.get(c);
			if (result == null) {
				queueLock.lock();
				try {
					if (lowPriority.contains(c)) {
						lowPriority.remove(c);
						highPriority.add(c);
					}
				} finally {
					queueLock.unlock();
				}
			}
			while ((result = results.get(c)) == null) {
				try {
					newResult.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			results.remove(c);
		} finally {
			resultLock.unlock();
		}
		return result;
	}

	private Callable<T> getNextTask() {
		queueLock.lock();
		try {
			while (highPriority.isEmpty() && lowPriority.isEmpty()) {
				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			return (!highPriority.isEmpty()) ? highPriority.poll()
					: lowPriority.poll();
		} finally {
			queueLock.unlock();
		}
	}
}
