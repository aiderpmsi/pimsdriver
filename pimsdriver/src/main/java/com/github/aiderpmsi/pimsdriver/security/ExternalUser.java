package com.github.aiderpmsi.pimsdriver.security;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

public class ExternalUser implements Principal {

	private String name;
	
	private HashSet<String> roles = new HashSet<>();
	
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Set<String> getRoles() {
		return roles;
	}
	
}
