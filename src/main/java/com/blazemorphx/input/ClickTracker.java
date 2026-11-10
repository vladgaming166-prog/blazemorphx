package com.blazemorphx.input;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ClickTracker {
	private static final Deque<Long> left = new ArrayDeque<>();
	private static final Deque<Long> right = new ArrayDeque<>();
	private static boolean leftHeld;
	private static boolean rightHeld;

	private ClickTracker() {
	}

	public static void init() {
		left.clear();
		right.clear();
	}

	public static void tick() {
		long now = System.currentTimeMillis();
		trim(left, now);
		trim(right, now);
	}

	public static void mouseButton(int button, boolean pressed) {
		if (button == 0) {
			leftHeld = pressed;
			if (pressed) {
				left.addLast(System.currentTimeMillis());
			}
		} else if (button == 1) {
			rightHeld = pressed;
			if (pressed) {
				right.addLast(System.currentTimeMillis());
			}
		}
	}

	public static double cps() {
		return left.size() / 1.0;
	}

	public static boolean leftHeld() {
		return leftHeld;
	}

	public static boolean rightHeld() {
		return rightHeld;
	}

	private static void trim(Deque<Long> clicks, long now) {
		while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) {
			clicks.removeFirst();
		}
	}
}
