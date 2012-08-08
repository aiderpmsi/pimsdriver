package aider.org.pmsiadmin.model.ldap;

import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;

import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.connector.LdapAuthenticator;

/**
 * Transforme un couple mot de passe + uilisateur en session
 * @author delabre
 *
 */
public class DtoSession {

	private LdapAuthenticator adAuthenticator = null;
	
	private Configuration configuration;
	
	/**
	 * Interdit la construction d'un {@link DtoSession} sans connecteur AD
	 */
	@SuppressWarnings("unused")
	private DtoSession() {}
	
	public DtoSession(LdapAuthenticator adAuthenticator,
			Configuration configuration) throws SQLException {
		this.adAuthenticator = adAuthenticator;
		this.configuration = configuration;
	}
	
	public Session getSession(String user, String name) throws NamingException {
		
		Map<String, String> adUser = adAuthenticator.authenticate(user, name);
		String ldapInfo;
		
		if (adUser != null) {
			Session session = new Session();

			session.setUserLogin(user);
			
			ldapInfo = adUser.get(configuration.getLdapMappingUniqueUserId());
			session.setUniqueUserId(ldapInfo);
			
			ldapInfo = adUser.get(configuration.getLdapMappingGivenName());
			session.setGivenName(ldapInfo == null ? "" : ldapInfo);
			
			ldapInfo = adUser.get(configuration.getLdapMappingSurname());
			session.setSurname(ldapInfo == null ? "" : ldapInfo);
			
			ldapInfo = adUser.get(configuration.getLdapMappingMail());
			session.setSurname(ldapInfo == null ? "" : ldapInfo);

			return session;
		} else
			return null;
	}
}
