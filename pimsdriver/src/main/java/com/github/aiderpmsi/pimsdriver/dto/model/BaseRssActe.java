package com.github.aiderpmsi.pimsdriver.dto.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseRssActe {

	private static SimpleDateFormat sdf =
			new SimpleDateFormat("dd/MM/yyyy");
	
	public Long pmel_id;
	
	public Long pmel_position;
	
	public Long pmel_line;
	
	public Date daterealisation;
	
	public String codeccam;

	public String phase;
	
	public String activite;
	
	public Integer nbacte;

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

	public Date getDaterealisation() {
		return daterealisation;
	}

	public void setDaterealisation(Date daterealisation) {
		this.daterealisation = daterealisation;
	}

	public String getFormatteddaterealisation() {
		return sdf.format(daterealisation);
	}

	public void setFormatteddaterealisation(String formatteddaterealisation) throws ParseException {
		this.daterealisation = sdf.parse(formatteddaterealisation);
	}

	public String getCodeccam() {
		return codeccam;
	}

	public void setCodeccam(String codeccam) {
		this.codeccam = codeccam;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getActivite() {
		return activite;
	}

	public void setActivite(String activite) {
		this.activite = activite;
	}

	public Integer getNbacte() {
		return nbacte;
	}

	public void setNbacte(Integer nbacte) {
		this.nbacte = nbacte;
	}
	

}
