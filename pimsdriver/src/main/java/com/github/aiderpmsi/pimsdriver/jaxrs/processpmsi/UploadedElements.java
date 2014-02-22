package com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="uploaded")
@XmlSeeAlso({UploadedElement.class})
public class UploadedElements {

	private List<UploadedElement> element;

	private Boolean lastChunk;
	
	private Boolean onlyPending;
	
	private List<String> order;
	
	private List<Boolean> orderdir;
	
	private Integer askedFirst;
	
	private Integer askedRows;
	
	@XmlElementWrapper(name="elements")
	public List<UploadedElement> getElement() {
		return element;
	}

	public void setElement(List<UploadedElement> element) {
		this.element = element;
	}

	public Boolean getLastChunk() {
		return lastChunk;
	}

	public void setLastChunk(Boolean lastChunk) {
		this.lastChunk = lastChunk;
	}

	public Boolean getOnlyPending() {
		return onlyPending;
	}

	public void setOnlyPending(Boolean onlyPending) {
		this.onlyPending = onlyPending;
	}

	@XmlElementWrapper(name="order")
	public List<String> getOrder() {
		return order;
	}

	public void setOrder(List<String> order) {
		this.order = order;
	}

	@XmlElementWrapper(name="orderdirection")
	public List<Boolean> getOrderdir() {
		return orderdir;
	}

	public void setOrderdir(List<Boolean> orderdir) {
		this.orderdir = orderdir;
	}

	public Integer getAskedFirst() {
		return askedFirst;
	}

	public void setAskedFirst(Integer askedFirst) {
		this.askedFirst = askedFirst;
	}

	public Integer getAskedRows() {
		return askedRows;
	}

	public void setAskedRows(Integer askedRows) {
		this.askedRows = askedRows;
	}

	
}
