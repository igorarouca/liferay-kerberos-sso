package com.cambiahealth.portal.dbcleanup.util;

import java.util.concurrent.atomic.AtomicInteger;
public final class ThreadIndex {

	public static int get() {
		return _threadIndex.getAndIncrement();
	}

	private ThreadIndex() {
	}

	private static final AtomicInteger _threadIndex = new AtomicInteger(0);

}