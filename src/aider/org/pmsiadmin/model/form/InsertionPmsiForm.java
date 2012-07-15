package aider.org.pmsiadmin.model.form;

import javax.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import aider.org.pmsiadmin.validator.MultipartNotEmpty;
import aider.org.pmsiamin.model.ldap.Session;

public class InsertionPmsiForm {

	@NotNull(message = "Vous n'êtes pas identifié")
	private Session session = null;
		
    @NotNull
    @MultipartNotEmpty
    private MultipartFile file = null;
    
    public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setFile(MultipartFile file) {
    	this.file = file;
    }
    
    public MultipartFile getFile() {
    	return file;
    }
}
