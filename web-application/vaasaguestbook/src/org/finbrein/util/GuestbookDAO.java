/**
 * GuestbookDAO.java
 */
package org.finbrein.util;

import java.io.Serializable;

/**
 * @author Michael Aro
 * 
 */
public class GuestbookDAO implements Serializable {

	private String id;
	private String image;
	private String name;
	private String message;
	private String datetime;
	
	public GuestbookDAO(String image, String name, String message, String datetime) {
		this.image = image;
		this.name = name;
		this.message = message;
		this.datetime = datetime;
	}
	
	public GuestbookDAO(String id, String image, String name, String message, String datetime) {
		this.id = id;
		this.image = image;
		this.name = name;
		this.message = message;
		this.datetime = datetime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	
	



}
