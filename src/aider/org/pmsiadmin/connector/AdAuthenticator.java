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
public class AdAuthenticator {
	
	private String domain;
	private String ldapHost;
	private String searchBase;
  
	public AdAuthenticator(String domain, String host, String dn) {
		this.domain = domain;
		this.ldapHost = host;
		this.searchBase = dn;
	}

	public Map<String, Object> authenticate(String user, String pass) throws NamingException {
		String returnedAtts[] ={ "ObjectGUID", "sn", "givenName", "mail" };
		String searchFilter = "(&(objectClass=user)(title=MEDECIN)(sAMAccountName=" + user + "))";
    
		//Create the search controls
		SearchControls searchCtls = new SearchControls();
		searchCtls.setReturningAttributes(returnedAtts);
    
		//Specify the search scope
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapHost);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, user + "@" + domain);
		env.put(Context.SECURITY_CREDENTIALS, pass);
    
		LdapContext ctxGC = null;
        
		ctxGC = new InitialLdapContext(env, null);
		//Search objects in GC using filters
		NamingEnumeration<SearchResult> answer = ctxGC.search(searchBase, searchFilter, searchCtls);
		
		// Only get the first element retrieved
		while (answer.hasMoreElements()) {
			SearchResult sr = (SearchResult) answer.next();
			Attributes attrs = sr.getAttributes();
			Map<String, Object> amap = null;
			if (attrs != null) {
				amap = new HashMap<String, Object>();
				NamingEnumeration<?> ne = attrs.getAll();
				while (ne.hasMore()) {
					Attribute attr = (Attribute) ne.next();
					amap.put(attr.getID(), attr.get());
				}
				ne.close();
			}
			return amap;
		}
		return null;
	}
}
