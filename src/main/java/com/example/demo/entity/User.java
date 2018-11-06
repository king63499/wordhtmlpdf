package com.example.demo.entity;

public class User {

	private String name;
	private String type;
	private String no;


	public User(String name, String type, String no) {
		super();
		this.name = name;
		this.type = type;
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}
}
