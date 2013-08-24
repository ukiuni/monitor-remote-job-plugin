package org.ukiuni.callOtherJenkins.CallOtherJenkins.util;

public class TimeParser {
	public static long parse(String arg) throws NumberFormatException {
		arg = arg.toLowerCase();
		if (arg.endsWith("d")) {
			return getTimePart(arg) * 24 * 60 * 60 * 1000;
		}
		if (arg.endsWith("h")) {
			return getTimePart(arg) * 60 * 60 * 1000;
		}
		if (arg.endsWith("m")) {
			return getTimePart(arg) * 60 * 1000;
		}
		if (arg.endsWith("s")) {
			return getTimePart(arg) * 1000;
		}
		return Long.parseLong(arg);
	}

	private static long getTimePart(String arg) {
		return Long.parseLong(arg.substring(0, arg.length() - 1));
	}
}
