package com.aem.sumanta.nlp;

import java.io.InputStream;
import java.math.BigInteger;

public class ContentItem {
	
	String mimeType;
	InputStream stream;
	String language;
	BigInteger contentLenght;
	String text;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public InputStream getStream() {
		return stream;
	}
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public BigInteger getContentLenght() {
		return contentLenght;
	}
	public void setContentLenght(BigInteger contentLenght) {
		this.contentLenght = contentLenght;
	}
	

}
