package com.github.aiderpmsi.pimsdriver.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class SecurityContextImpl implements SecurityContext {

	private final ExternalUser user;
	
	public SecurityContextImpl(ExternalUser user) {
		this.user = user;
	}

	@Override
	public Principal getUserPrincipal() {
		return user;
	}

	@Override
	public boolean isUserInRole(String role) {
		if (user.getRoles().contains(role))
			return true;
		else
			return false;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public String getAuthenticationScheme() {
		return SecurityContext.BASIC_AUTH;
	}

}
