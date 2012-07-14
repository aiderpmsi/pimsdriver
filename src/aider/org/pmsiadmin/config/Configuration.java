package aider.org.pmsiadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Objet de configuration conservant tous le éléments saisis dans le fchier de config
 */
@Component("configuration")
public class Configuration {

	@Value("${sedna.host}")
	private String sednaHost;

	@Value("${sedna.user}")
	private String sednaUser;

	@Value("${sedna.pwd}")
	private String sednaPwd;
	
	@Value("${ldap.domain}")
	private String ldapDomain;
	
	@Value("${ldap.host}")
	private String ldapHost;
	
	@Value("${ldap.dn}")
	private String ldapDn;

	// ============== Getters et Setters =====================
	
	public String getSednaHost() {
		return sednaHost;
	}

	public void setSednaHost(String sednaHost) {
		this.sednaHost = sednaHost;
	}

	public String getSednaUser() {
		return sednaUser;
	}

	public void setSednaUser(String sednaUser) {
		this.sednaUser = sednaUser;
	}

	public String getSednaPwd() {
		return sednaPwd;
	}

	public void setSednaPwd(String sednaPwd) {
		this.sednaPwd = sednaPwd;
	}

	public String getLdapDomain() {
		return ldapDomain;
	}

	public void setLdapDomain(String ldapDomain) {
		this.ldapDomain = ldapDomain;
	}

	public String getLdapHost() {
		return ldapHost;
	}

	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}

	public String getLdapDn() {
		return ldapDn;
	}

	public void setLdapDn(String ldapDn) {
		this.ldapDn = ldapDn;
	}
	
	
}
