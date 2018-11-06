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

		User tom = new User("基金A", "类型A", "NO123456");

		//variables.put("title", "hello, 张三");
		variables.put("user", tom);
		
		return variables;
	}
	
}
