package com.github.trask.threadlocal;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class ThreadLocalBenchmark2 extends SimpleBenchmark {

	public static void main(String[] args) {
		Runner.main(new String[] { ThreadLocalBenchmark2.class.getName() });
	}

	private static final ThreadLocal<Integer> BUFFER = new MyThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 1;
		}
	};

	public void timeThreadLocal_get(int reps) {
		int x = 0;
		for (int rep = 0; rep < reps; ++rep) {
			x += BUFFER.get();
			BUFFER.set(x);
		}
		System.out.println(x);
	}
}
