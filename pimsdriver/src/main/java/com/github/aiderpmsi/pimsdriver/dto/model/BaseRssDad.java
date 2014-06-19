package com.github.aiderpmsi.pimsdriver.dto.model;

public class BaseRssDad {

	public Long pmel_id;
	
	public Long pmel_position;
	
	public Long pmel_line;
	
	public String dad;

	public String getDad() {
		return dad;
	}

	public void setDa(String dad) {
		this.dad = dad;
	}

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
	
}
