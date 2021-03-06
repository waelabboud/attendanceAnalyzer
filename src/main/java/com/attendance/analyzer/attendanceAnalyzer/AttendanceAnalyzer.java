package com.attendance.analyzer.attendanceAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.attendance.analyzer.excel.ExcelExtractor;
import com.attendance.analyzer.util.DateUtils;

public class AttendanceAnalyzer {

	private final String USER_AGENT = "Mozilla/5.0";
	private boolean isDiagnose = false;

	public static void main(String[] args) throws Exception {
		AttendanceAnalyzer analyzer = new AttendanceAnalyzer();
		analyzer.analyze(args);
	}

	private void analyze(String[] args) throws Exception {

		System.out.println("started analyzing timestamps...");
		
		AttendanceInterval ai = extractInterval(args);
		String url = constructURL(ai);

		

		InputStream response = getHTTPResponse(url);

		Map<String, ArrayList<String>> attendancePerDay = readAttendance(response);

		if (isDiagnose) {
			// just output the attendance per day map and exit
			printAttendancePerDay(attendancePerDay);
		} else {

			Map<String, Double> hoursPerDay = analyzeAttendancePerDay(attendancePerDay);

			String reportLocation = new ExcelExtractor().writeToExcel(hoursPerDay, ai.userId);

			System.out.println("An Excel report is generated here:" + reportLocation);
		}
		System.out.println("Done.");
	}

	private void printAttendancePerDay(Map<String, ArrayList<String>> attendancePerDay) {
	   attendancePerDay.forEach((k,v)->System.out.println("Day : " + k + " Entries : " + v)); 
	}

	private String constructURL(AttendanceInterval ai) {

		String url = "http://10.100.0.201/if.cgi?redirect2=setting.htm&failure2=fail.htm&type2=txt_data"
				.concat("&UID22=").concat(ai.getUserId()).concat("&TID22=&select5=0&Fkey=255&num=0")
				.concat("&start_month=").concat(ai.getStartMonth()).concat("&start_date=").concat(ai.getStartDay())
				.concat("&start_year=").concat(ai.getStartYear()).concat("&end_month=").concat(ai.getEndMonth())
				.concat("&end_date=").concat(ai.getEndDay()).concat("&end_year=").concat(ai.getEndYear())
				.concat("&sel_all=1&Export=EXPORT");
		return url;
	}

	private AttendanceInterval extractInterval(String[] args) {

		isDiagnose = false; // reset
		
		if(null == args  || args.length == 0 ) {
			printUsage();
			System.exit(0);
		}

		String userId = args[0];
		String startMonth = args[1];
		String startDay = null;
		String startYear = null;
		String endMonth = null;
		String endDay = null;
		String endYear = null;

		if (startMonth == null) {
			printUsage();
		} else if (startMonth.equals("currentMonth")) {

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1); // calculate up to yesterday
			Date yesterday = cal.getTime();

			startDay = "01";
			startMonth = new SimpleDateFormat("MM").format(Calendar.getInstance().getTime());
			startYear = new SimpleDateFormat("yy").format(Calendar.getInstance().getTime());
			endMonth = startMonth;
			endDay = new SimpleDateFormat("dd").format(yesterday);
			;
			endYear = startYear;
		} else {
			startDay = args[2];
			startYear = args[3];
			endMonth = args[4];
			endDay = args[5];
			endYear = args[6];

			
			if ( args.length > 6 && args[7].equals("-diagnose")) {
				isDiagnose = true;
			}
		}

		// 99 5 1 19 5 6 19

		AttendanceInterval ai = new AttendanceInterval(userId, startMonth, startDay, startYear, endMonth, endDay,
				endYear);

		return ai;
	}

	private void printUsage() {
		System.out.println("Incorrect arguments entered");
		System.out.println("Usage: Analyzer id currentMonth");
		System.out.println("Example: Analyzer 99 currentMonth");
		System.out.println("Or: Analyzer id startMonth startDay startYear endMonth endDay endYear");
		System.out.println("Example: Analyzer 99 5 1 19 5 6 19");
		System.out.println("Note: you can also show all entries for a specific time interval by adding -diagnose");
		System.out.println("For example: Analyzer 99 5 1 19 5 30 19 -diagnose");
	}

	private InputStream getHTTPResponse(String url) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpUriRequest request = new HttpGet(url);

		// add request header
		request.addHeader("User-Agent", USER_AGENT);

		HttpResponse response = client.execute(request);
		System.out.println("Response:" + response);
		return response.getEntity().getContent();
	}

	/**
	 * Extracts the attendance info from the http response.
	 * 
	 * @param response the http response
	 * @return a tree map of total worked hours / day in reverse order
	 * @throws IOException
	 */
	private Map<String, ArrayList<String>> readAttendance(InputStream inputStream) {

		TreeMap<String, ArrayList<String>> attendancePerDay = new TreeMap<>(Collections.reverseOrder());

		Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines();

		lines.forEach(line -> {
			String[] record = line.split(",");
			String day = record[4];
			String timeStamp = record[5];
			ArrayList<String> timeStamps = null;
			if (!attendancePerDay.containsKey(day)) {
				// new day recorded
				timeStamps = new ArrayList<>();
			} else {
				timeStamps = attendancePerDay.get(day);
			}
			timeStamps.add(timeStamp);
			attendancePerDay.put(day, timeStamps);
		});

		return attendancePerDay;
	}

	private Map<String, Double> analyzeAttendancePerDay(Map<String, ArrayList<String>> attendancePerDay) {

		TreeMap<String, Double> hoursPerDay = new TreeMap<>();
		attendancePerDay.forEach((k, v) -> {

			Double totalDurationPerDay = 0.0;
			if (v.size() % 2 == 0) {
				// even - good

				for (int i = 1; i < v.size(); i = i + 2) {
					Double duration = DateUtils.calculateDuration(v.get(i), v.get(i - 1));
					totalDurationPerDay = totalDurationPerDay + duration;
				}
			} else {
				// odd - use first timestamp as IN and last timestamp as OUT - ignore all
				// timestamps in between
				totalDurationPerDay = DateUtils.calculateDuration(v.get(v.size() - 1), v.get(0));
			}

			hoursPerDay.put(k, round(totalDurationPerDay));

		});

		return hoursPerDay;
	}

	private double round(double d) {
		return Math.round(d * 100) / 100.0;
	}

}