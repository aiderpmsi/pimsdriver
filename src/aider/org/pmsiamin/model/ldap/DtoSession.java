package aider.org.pmsiamin.model.ldap;

import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;

import aider.org.pmsiadmin.connector.LdapAuthenticator;

/**
 * Transforme un couple mot de passe + uilisateur en session
 * @author delabre
 *
 */
public class DtoSession {

	private LdapAuthenticator adAuthenticator = null;
	
	/**
	 * Interdit la construction d'un {@link DtoSession} sans connecteur AD
	 */
	@SuppressWarnings("unused")
	private DtoSession() {}
	
	public DtoSession(LdapAuthenticator adAuthenticator) throws SQLException {
		this.adAuthenticator = adAuthenticator;
	}
	
	public Session getSession(String user, String name) throws NamingException {
		
		Map<String, Object> adUser = adAuthenticator.authenticate(user, name);
		
		if (adUser != null) {
			Session session = new Session();

			session.setUser(user);
			session.setUserGUID((String) adUser.get("ObjectGUID"));
			session.setGivenName(adUser.get("givenName") == null ? "" : (String) adUser.get("givenName"));
			session.setSn(adUser.get("sn") == null ? "" : (String) adUser.get("sn"));
			session.setMail(adUser.get("mail") == null ? "" : (String) adUser.get("mail"));

			return session;
		} else
			return null;
	}
}
