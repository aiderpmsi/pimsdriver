package com.github.aiderpmsi.pimsdriver.dto.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseRssMain {

	private static SimpleDateFormat sdf =
			new SimpleDateFormat("dd/MM/yyyy");
	
	public Long pmel_id;
	
	public Long pmel_position;
	
	public Long pmel_line;
	
	public String numrss;
	
	public String numlocalsejour;
	
	public String numrum;
	
	public String numunitemedicale;
	
	public String ghm;
	
	public String ghmcorrige;

	public String dp;
	
	public String dr;
	
	public Integer nbseances;
	
	public Date dateentree;
	
	public Date datesortie;

	public Long getPmel_id() {
		return pmel_id;
	}

	public void setPmel_id(Long pmel_id) {
		this.pmel_id = pmel_id;
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

	public String getNumrss() {
		return numrss;
	}

	public void setNumrss(String numrss) {
		this.numrss = numrss;
	}

	public String getNumlocalsejour() {
		return numlocalsejour;
	}

	public void setNumlocalsejour(String numlocalsejour) {
		this.numlocalsejour = numlocalsejour;
	}

	public String getNumrum() {
		return numrum;
	}

	public void setNumrum(String numrum) {
		this.numrum = numrum;
	}

	public String getNumunitemedicale() {
		return numunitemedicale;
	}

	public void setNumunitemedicale(String numunitemedicale) {
		this.numunitemedicale = numunitemedicale;
	}

	public String getGhm() {
		return ghm;
	}

	public void setGhm(String ghm) {
		this.ghm = ghm;
	}

	public String getGhmcorrige() {
		return ghmcorrige;
	}

	public void setGhmcorrige(String ghmcorrige) {
		this.ghmcorrige = ghmcorrige;
	}

	public String getDp() {
		return dp;
	}

	public void setDp(String dp) {
		this.dp = dp;
	}

	public String getDr() {
		return dr;
	}

	public void setDr(String dr) {
		this.dr = dr;
	}

	public Integer getNbseances() {
		return nbseances;
	}

	public void setNbseances(Integer nbseances) {
		this.nbseances = nbseances;
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

}
