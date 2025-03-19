package com.fourt.RailSkylines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
public class RailSkylinesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RailSkylinesApplication.class, args);
	}

}
