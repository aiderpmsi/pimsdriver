package com.github.aiderpmsi.pimsdriver.dto.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseRsfB {

	private static DecimalFormat df =
			new DecimalFormat("+#,##0.00;-#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));

	private static SimpleDateFormat sdf =
			new SimpleDateFormat("dd/MM/yyyy");
	
	public Long pmel_id;
	
	public Long pmel_line;
	
	public Date datedebutsejour;
	
	public Date datefinsejour;
	
	public String codeacte;
	
	public Integer quantite;
	
	public String numghs;
	
	public BigDecimal montanttotaldepense;

	public Long getPmel_id() {
		return pmel_id;
	}

	public void setPmel_id(Long pmel_id) {
		this.pmel_id = pmel_id;
	}

	public Long getPmel_line() {
		return pmel_line;
	}

	public void setPmel_line(Long pmel_line) {
		this.pmel_line = pmel_line;
	}

	public Date getDatedebutsejour() {
		return datedebutsejour;
	}

	public void setDatedebutsejour(Date datedebutsejour) {
		this.datedebutsejour = datedebutsejour;
	}

	public String getFormatteddatedebutsejour() {
		return sdf.format(datedebutsejour);
	}

	public void setFormatteddatedebutsejour(String formatteddatedebutsejour) throws ParseException {
		this.datedebutsejour = sdf.parse(formatteddatedebutsejour);
	}

	public Date getDatefinsejour() {
		return datefinsejour;
	}

	public void setDatefinsejour(Date datefinsejour) {
		this.datefinsejour = datefinsejour;
	}

	public String getFormatteddatefinsejour() {
		return sdf.format(datefinsejour);
	}

	public void setFormatteddatefinsejour(String formatteddatefinsejour) throws ParseException {
		this.datefinsejour = sdf.parse(formatteddatefinsejour);
	}

	public String getCodeacte() {
		return codeacte;
	}

	public void setCodeacte(String codeacte) {
		this.codeacte = codeacte;
	}

	public Integer getQuantite() {
		return quantite;
	}

	public void setQuantite(Integer quantite) {
		this.quantite = quantite;
	}

	public String getNumghs() {
		return numghs;
	}

	public void setNumghs(String numghs) {
		this.numghs = numghs;
	}

	public BigDecimal getMontanttotaldepense() {
		return montanttotaldepense;
	}

	public void setMontanttotaldepense(BigDecimal montanttotaldepense) {
		this.montanttotaldepense = montanttotaldepense;
	}

	public String getFormattedmontanttotaldepense() {
		return df.format(montanttotaldepense);
	}

	public void setFormattedmontanttotaldepense(String formattedmontanttotaldepense) throws ParseException {
		this.montanttotaldepense = (BigDecimal) df.parse(formattedmontanttotaldepense);
	}
}
