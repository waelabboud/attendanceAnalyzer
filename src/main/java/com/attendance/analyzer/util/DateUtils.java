package com.attendance.analyzer.util;

public class DateUtils {

	public static Double calculateDuration(String durationA, String durationB) {
		Integer a = getSeconds(durationA);
		Integer b = getSeconds(durationB);

		return (b - a) / 3600.0;
	}

	/*
	 * Transforms a timestamp to seconds.
	 * hh:mm:ss to seconds
	 */
	public static Integer getSeconds(String dur) {
		String[] data = dur.replaceAll("\"", "").split(":");

		int hours = Integer.parseInt(data[0]);
		int minutes = Integer.parseInt(data[1]);
		int seconds = Integer.parseInt(data[2]);

		int time = seconds + 60 * minutes + 3600 * hours;
		return time;
	}
}
