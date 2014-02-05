package com.github.aiderpmsi.pimsdriver.security;

import java.security.Principal;

public class ExternalUser implements Principal {

	private String name = "Test";
	
	private String role = "Test2";
	
	public String getRole() {
		return role;
	}

	@Override
	public String getName() {
		return name;
	}

}
