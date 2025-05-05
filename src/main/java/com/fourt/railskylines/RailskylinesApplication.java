package com.fourt.railskylines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class RailskylinesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RailskylinesApplication.class, args);
	}

}
