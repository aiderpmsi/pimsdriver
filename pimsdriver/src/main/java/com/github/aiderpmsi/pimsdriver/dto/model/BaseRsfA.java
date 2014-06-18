package com.github.aiderpmsi.pimsdriver.dto.model;

import java.math.BigDecimal;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BaseRsfA {
	
	private static DecimalFormat df =
			new DecimalFormat("+#,##0.00;-#,##0.00", new DecimalFormatSymbols(Locale.FRANCE));

	private static SimpleDateFormat sdf =
			new SimpleDateFormat("dd/MM/yyyy");
	
	public Long pmel_id;
	
	public Long pmel_root;
	
	public Long pmel_position;
	
	public Long pmel_line;
	
	public String numfacture;
	
	public String numrss;
	
	public String codess;
	
	public String sexe;
	
	public Date datenaissance;
	
	public Date dateentree;
	
	public Date datesortie;
	
	public BigDecimal totalfacturehonoraire;
	
	public BigDecimal totalfactureph;
	
	public String etatliquidation;

	public Long getPmel_id() {
		return pmel_id;
	}

	public void setPmel_id(Long pmel_id) {
		this.pmel_id = pmel_id;
	}

	public Long getPmel_root() {
		return pmel_root;
	}

	public void setPmel_root(Long pmel_root) {
		this.pmel_root = pmel_root;
	}

	public Long getPmel_position() {
		return pmel_position;
	}

	public void setPmel_position(Long pmel_position) {
		this.pmel_position = pmel_position;
	}

	public Long getPmel_line() {
		return pmel_line;
	}

	public void setPmel_line(Long pmel_line) {
		this.pmel_line = pmel_line;
	}

	public String getNumfacture() {
		return numfacture;
	}

	public void setNumfacture(String numfacture) {
		this.numfacture = numfacture;
	}

	public String getNumrss() {
		return numrss;
	}

	public void setNumrss(String numrss) {
		this.numrss = numrss;
	}

	public String getCodess() {
		return codess;
	}

	public void setCodess(String codess) {
		this.codess = codess;
	}

	public String getSexe() {
		return sexe;
	}

	public void setSexe(String sexe) {
		this.sexe = sexe;
	}

	public Date getDatenaissance() {
		return datenaissance;
	}

	public void setDatenaissance(Date datenaissance) {
		this.datenaissance = datenaissance;
	}

	public String getFormatteddatenaissance() {
		return sdf.format(datenaissance);
	}

	public void setFormatteddatenaissance(String formatteddatenaissance) throws ParseException {
		this.datenaissance = sdf.parse(formatteddatenaissance);
	}

	public Date getDateentree() {
		return dateentree;
	}

	public void setDateentree(Date dateentree) {
		this.dateentree = dateentree;
	}

	public String getFormatteddateentree() {
		return sdf.format(dateentree);
	}

	public void setFormatteddateentree(String formatteddateentree) throws ParseException {
		this.dateentree = sdf.parse(formatteddateentree);
	}

	public Date getDatesortie() {
		return datesortie;
	}

	public void setDatesortie(Date datesortie) {
		this.datesortie = datesortie;
	}

	public String getFormatteddatesortie() {
		return sdf.format(datesortie);
	}

	public void setFormatteddatesortie(String formatteddatesortie) throws ParseException {
		this.datesortie = sdf.parse(formatteddatesortie);
	}

	public BigDecimal getTotalfacturehonoraire() {
		return totalfacturehonoraire;
	}

	public void setTotalfacturehonoraire(BigDecimal totalfacturehonoraire) {
		this.totalfacturehonoraire = totalfacturehonoraire;
	}

	public String getFormattedtotalfacturehonoraire() {
		return df.format(totalfacturehonoraire);
	}

	public void setFormattedtotalfacturehonoraire(String formattedtotalfacturehonoraire) throws ParseException {
		this.totalfacturehonoraire = (BigDecimal) df.parse(formattedtotalfacturehonoraire);
	}

	public BigDecimal getTotalfactureph() {
		return totalfactureph;
	}

	public void setTotalfactureph(BigDecimal totalfactureph) {
		this.totalfactureph = totalfactureph;
	}

	public String getFormattedtotalfactureph() {
		return df.format(totalfactureph);
	}

	public void setFormattedtotalfactureph(String formattedtotalfactureph) throws ParseException {
		this.totalfactureph = (BigDecimal) df.parse(formattedtotalfactureph);
	}

	public String getEtatliquidation() {
		return etatliquidation;
	}

	public void setEtatliquidation(String etatliquidation) {
		this.etatliquidation = etatliquidation;
	}

}
