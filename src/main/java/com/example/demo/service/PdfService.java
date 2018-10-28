package com.example.demo.service;

import com.example.demo.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfService {

	public Map<String, Object> getContent() throws IOException {

		// 从数据库中获取数据， 出于演示目的， 这里数据不从数据库获取， 而是直接写死
		
		Map<String, Object> variables = new HashMap<String, Object>(3);

		List<User> userList = new ArrayList<User>();

		User tom = new User("张三", 19, 1);
		User amy = new User("Amy", 28, 0);
		User leo = new User("Leo", 23, 1);

		userList.add(tom);
		userList.add(amy);
		userList.add(leo);

		variables.put("title", "用户列表");
		variables.put("userList", userList);
		
		return variables;
	}
	
}
