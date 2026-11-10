package com.blazemorphx.util;

import com.blazemorphx.BlazeMorphx;
import net.minecraft.util.Identifier;

public final class Identifiers {
	private Identifiers() {
	}

	public static Identifier of(String path) {
		return Identifier.of(BlazeMorphx.MOD_ID, path);
	}
}
