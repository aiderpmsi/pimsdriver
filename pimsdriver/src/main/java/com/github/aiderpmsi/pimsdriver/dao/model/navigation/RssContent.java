package com.github.aiderpmsi.pimsdriver.dao.model.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RssContent {
	private HashMap<String, String> rssmain;
	private List<HashMap<String, String>> rssacte = new ArrayList<>();
	private List<HashMap<String, String>> rssda = new ArrayList<>();
	private List<HashMap<String, String>> rssdad = new ArrayList<>();
	public HashMap<String, String> getRssmain() {
		return rssmain;
	}
	public void setRssmain(HashMap<String, String> rssmain) {
		this.rssmain = rssmain;
	}
	public List<HashMap<String, String>> getRssacte() {
		return rssacte;
	}
	public void setRssacte(List<HashMap<String, String>> rssacte) {
		this.rssacte = rssacte;
	}
	public List<HashMap<String, String>> getRssda() {
		return rssda;
	}
	public void setRssda(List<HashMap<String, String>> rssda) {
		this.rssda = rssda;
	}
	public List<HashMap<String, String>> getRssdad() {
		return rssdad;
	}
	public void setRssdad(List<HashMap<String, String>> rssdad) {
		this.rssdad = rssdad;
	}
}