package aider.org.pmsiadmin.connector;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * Classe permettant l'abstraction de la connection à ldap 
 * @author delabre
 *
 */
public class LdapAuthenticator {
	
	private LdapConfiguration config;
  
	public LdapAuthenticator(LdapConfiguration config) {
		this.config = config;
	}

	public Map<String, String> authenticate(String user, String pass) throws NamingException {
		String returnedAtts[] = {
				config.getLdapMappingUniqueUserId(),
				config.getLdapMappingSurname(),
				config.getLdapMappingGivenName(),
				config.getLdapMappingMail()
				};
		String searchFilter = "(&(objectClass=" + config.getLdapObjectClass() + ")" +
				config.getLdapAdditionalFilters() + "(" +
				config.getLdapUserlogin() + "=" + user + "))";
    
		//Create the search controls
		SearchControls searchCtls = new SearchControls();
		searchCtls.setReturningAttributes(returnedAtts);
    
		//Specify the search scope
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, config.getLdapHost());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, config.getProcessedAuthIdentifier(user));
		env.put(Context.SECURITY_CREDENTIALS, pass);
    
		LdapContext ctxGC = null;
        
		ctxGC = new InitialLdapContext(env, null);
		//Search objects in GC using filters
		NamingEnumeration<SearchResult> answer = ctxGC.search(
				config.getLdapDn(), searchFilter, searchCtls);
		
		// Only get the first element retrieved
		while (answer.hasMoreElements()) {
			SearchResult sr = (SearchResult) answer.next();
			Attributes attrs = sr.getAttributes();
			Map<String, String> amap = null;
			if (attrs != null) {
				amap = new HashMap<String, String>();
				NamingEnumeration<?> ne = attrs.getAll();
				while (ne.hasMore()) {
					Attribute attr = (Attribute) ne.next();
					amap.put(attr.getID(), (String) attr.get());
				}
				ne.close();
			}
			return amap;
		}
		return null;
	}
}
