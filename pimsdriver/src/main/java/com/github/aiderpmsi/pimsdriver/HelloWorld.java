package com.github.aiderpmsi.pimsdriver;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("helloWorld")
@SessionScoped
public class HelloWorld implements Serializable {

	private static final long serialVersionUID = -4683282621362816695L;

	private String name;

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

}
