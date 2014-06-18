package com.github.aiderpmsi.pimsdriver.dto.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseRsfC {

	private static DecimalFormat df =
			new DecimalFormat("+#,##0.00;-#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));

	private static SimpleDateFormat sdf =
			new SimpleDateFormat("dd/MM/yyyy");
	
	public Long pmel_id;
	
	public Long pmel_line;
	
	public Date dateacte;
	
	public String codeacte;
	
	public Integer quantite;
	
	public BigDecimal montanttotalhonoraire;

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

	public Date getDateacte() {
		return dateacte;
	}

	public void setDateacte(Date dateacte) {
		this.dateacte = dateacte;
	}

	public String getFormatteddateacte() {
		return sdf.format(dateacte);
	}

	public void setFormatteddateacte(String formatteddateacte) throws ParseException {
		this.dateacte = sdf.parse(formatteddateacte);
	}

	public String getCodeacte() {
		return codeacte;
	}

	public void setCodeacte(String codeacte) {
		this.codeacte = codeacte;
	}

	public BigDecimal getMontanttotalhonoraire() {
		return montanttotalhonoraire;
	}

	public void setMontanttotalhonoraire(BigDecimal montanttotalhonoraire) {
		this.montanttotalhonoraire = montanttotalhonoraire;
	}

	public String getFormattedmontanttotalhonoraire() {
		return df.format(montanttotalhonoraire);
	}

	public void setFormattedmontanttotalhonoraire(String formattedmontanttotalhonoraire) throws ParseException {
		this.montanttotalhonoraire = (BigDecimal) df.parse(formattedmontanttotalhonoraire);
	}

}
