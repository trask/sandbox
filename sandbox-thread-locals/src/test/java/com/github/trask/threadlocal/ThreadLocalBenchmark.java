package com.github.trask.threadlocal;

import java.util.List;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.google.common.collect.Lists;

public class ThreadLocalBenchmark {

	public static void main(String[] args) {
		createDummyThreads();
		Runner.main(new String[] { Benchmark1.class.getName() });
	}

	private static void createDummyThreads() {
		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread() {
				public void run() {
					try {
						Thread.sleep(100000);
					} catch (InterruptedException e) {}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}

	public static class Benchmark1 extends SimpleBenchmark {
		private static final int NUM_DISTRACTIONS = 0;
		private final List<ThreadLocal<String>> distractions = Lists
				.newArrayList();
		private ThreadLocal<String> tl;

		public Benchmark1() {
			for (int i = 0; i < NUM_DISTRACTIONS; i++) {
				distractions.add(new ThreadLocal<String>());
			}
			tl = new ThreadLocal<String>();
			tl.get();
			tl.set("val");
		}

		public void timeNanoTime(int reps) {
			for (int i = 0; i < 10 * reps; i++) {
				tl.get();
			}
		}
	}

	public static class Benchmark2 extends SimpleBenchmark {
		private final ThreadLocal<String> tl = new MyThreadLocal<String>();

		public void timeNanoTime(int reps) {
			for (int i = 0; i < 10 * reps; i++) {
				tl.get();
			}
		}
	}
}
