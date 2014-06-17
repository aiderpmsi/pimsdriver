package com.github.aiderpmsi.pimsdriver.dto.model;

import java.math.BigDecimal;
import java.sql.Date;

public class BaseRsfA {

	public Long pmel_id;
	
	public Long pmel_root;
	
	public Long pmel_position;
	
	public Long ligne;
	
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

	public Long getLigne() {
		return ligne;
	}

	public void setLigne(Long ligne) {
		this.ligne = ligne;
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

	public Date getDateentree() {
		return dateentree;
	}

	public void setDateentree(Date dateentree) {
		this.dateentree = dateentree;
	}

	public Date getDatesortie() {
		return datesortie;
	}

	public void setDatesortie(Date datesortie) {
		this.datesortie = datesortie;
	}

	public BigDecimal getTotalfacturehonoraire() {
		return totalfacturehonoraire;
	}

	public void setTotalfacturehonoraire(BigDecimal totalfacturehonoraire) {
		this.totalfacturehonoraire = totalfacturehonoraire;
	}

	public BigDecimal getTotalfactureph() {
		return totalfactureph;
	}

	public void setTotalfactureph(BigDecimal totalfactureph) {
		this.totalfactureph = totalfactureph;
	}

	public String getEtatliquidation() {
		return etatliquidation;
	}

	public void setEtatliquidation(String etatliquidation) {
		this.etatliquidation = etatliquidation;
	}

}
