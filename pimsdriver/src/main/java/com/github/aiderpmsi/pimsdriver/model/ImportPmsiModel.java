package com.github.aiderpmsi.pimsdriver.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ImportPmsiModel {

	/**
	 * Pmsi Month. Must be between 1 and 12
	 */
	@Min(1)
	@Max(12)
	private Integer monthValue;

	/**
	 * Pmsi Year. Must be non null
	 */
	@NotNull
	private Integer yearValue;

	/**
	 * Finess Value. Must be non null
	 */
	@NotNull
	@Size(min = 1)
	private String finessValue;

	public Integer getMonthValue() {
		return monthValue;
	}

	public void setMonthValue(Integer monthValue) {
		this.monthValue = monthValue;
	}

	public Integer getYearValue() {
		return yearValue;
	}

	public void setYearValue(Integer yearValue) {
		this.yearValue = yearValue;
	}

	public String getFinessValue() {
		return finessValue;
	}

	public void setFinessValue(String finessValue) {
		this.finessValue = finessValue;
	}

	/**
	 * Creates the Form with default values : - month = current month - year =
	 * current year
	 */
	public void setDefaultValues() {
		// Gets the current Calendar (Gregorian calendar)
		Calendar cal = GregorianCalendar.getInstance();
		
		// Sets the current month and current year
		setMonthValue(cal.get(Calendar.MONTH) + 1);
		setYearValue(cal.get(Calendar.YEAR));
		
		// SETS FINESS TO VOID
		setFinessValue("");
	}

}
