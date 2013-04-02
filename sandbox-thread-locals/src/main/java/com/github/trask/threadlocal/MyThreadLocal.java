package com.github.trask.threadlocal;

public class MyThreadLocal<T> extends ThreadLocal<T> {

	public static volatile int misses;

	private volatile LastValue<T> lastValue;

	@Override
	public T get() {
		LastValue<T> lastVal = lastValue;
		Thread currentThread = Thread.currentThread();
		if (lastVal != null && currentThread == lastVal.thread) {
			return lastVal.value;
		}
		// misses++;
		T value = super.get();
		lastValue = new LastValue<T>(value, currentThread);
		return value;
	}

	@Override
	public void set(T value) {
		// super.set(value);
		// lastValue = new LastValue<T>(value, Thread.currentThread());
	}

	@Override
	public void remove() {
		super.remove();
		// force it to recalculate, since it will call initalValue()
		lastValue = null;
	}

	private static class LastValue<T> {
		private final T value;
		private final Thread thread;

		public LastValue(T value, Thread thread) {
			this.value = value;
			this.thread = thread;
		}
	}
}
