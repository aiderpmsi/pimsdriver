package com.github.aiderpmsi.pimsdriver.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class PmsiUploadElementModel {

	/** Pmsi Month. Must be between 1 and 12 */
	@Min(1)	@Max(12)
	private Integer month;

	/** Pmsi Year. Must be non null */
	@NotNull
	private Integer year;

	/** Finess Value. Must be non null */
	@NotNull
	@Size(min = 1)
	private String finess;

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getFiness() {
		return finess;
	}

	public void setFiness(String finess) {
		this.finess = finess;
	}

	/**
	 * Creates the Form with default values : - month = current month - year =
	 * current year
	 */
	public void initDefaultValues() {
		// Gets the current Calendar (Gregorian calendar)
		Calendar cal = GregorianCalendar.getInstance();
		
		// Sets the current month and current year
		setMonth(cal.get(Calendar.MONTH) + 1);
		setYear(cal.get(Calendar.YEAR));
		
		// SETS FINESS TO VOID
		setFiness("");
	}

}
