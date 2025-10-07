package com.fourt.railskylines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fourt.railskylines.config.ChatbotProperties;
import com.fourt.railskylines.config.OpenAIProperties;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({OpenAIProperties.class, ChatbotProperties.class})
public class RailskylinesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RailskylinesApplication.class, args);
	}

}
