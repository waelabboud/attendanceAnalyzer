package com.attendance.analyzer.attendanceAnalyzer;

public class AttendanceInterval {

	String userId = null;

	String startDay = null;
	String startMonth = null;
	String startYear = null;

	String endMonth = null;
	String endDay = null;
	String endYear = null;

	public AttendanceInterval(String userId, String startMonth, String startDay, String startYear, String endMonth,
			String endDay, String endYear) {
		super();
		this.userId = userId;
		this.startMonth = startMonth;
		this.startDay = startDay;
		this.startYear = startYear;
		this.endMonth = endMonth;
		this.endDay = endDay;
		this.endYear = endYear;
	}
	
	public String getStartDate() {
		return  startDay + "/"+startMonth + "/" + startYear;
	}
	
	public String getEndDate() {
		return  endDay + "/"+endMonth + "/" + endYear;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}

	public String getStartDay() {
		return startDay;
	}

	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}

	public String getStartYear() {
		return startYear;
	}

	public void setStartYear(String startYear) {
		this.startYear = startYear;
	}

	public String getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(String endMonth) {
		this.endMonth = endMonth;
	}

	public String getEndDay() {
		return endDay;
	}

	public void setEndDay(String endDay) {
		this.endDay = endDay;
	}

	public String getEndYear() {
		return endYear;
	}

	public void setEndYear(String endYear) {
		this.endYear = endYear;
	}

}
