package com.github.trask.threadlocal;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class ThreadLocalBenchmark3 extends SimpleBenchmark {

	public static void main(String[] args) {
		Runner.main(new String[] { ThreadLocalBenchmark3.class.getName() });
	}

	private static final ThreadLocal<IntegerHolder> BUFFER = new MyThreadLocal<IntegerHolder>() {
		@Override
		protected IntegerHolder initialValue() {
			return new IntegerHolder();
		}
	};
	
	private static final AtomicInteger VALUE = new AtomicInteger();
	
	private static volatile int val = 1;

	public void timeThreadLocal_get(int reps) {
		int x = 1;
		for (int rep = 0; rep < reps; ++rep) {
			IntegerHolder integerHolder = BUFFER.get();
			integerHolder.value += x;
		}
		System.out.println(x);
	}
	
	private static class IntegerHolder {
		public int value;
	}
}
