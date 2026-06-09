package com.example.ai.demo;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	CommandLineRunner notionMcpToolLogger(
			@Qualifier("notionMcpToolCallbacks") ToolCallbackProvider notionMcpToolCallbacks) {

		return args -> {
			try {
				int toolCount = notionMcpToolCallbacks.getToolCallbacks().length;
				System.out.printf("[MCP Notion] tools loaded: %d%n", toolCount);
			}
			catch (Exception ex) {
				System.out.printf("[MCP Notion] tools unavailable: %s%n", ex.getMessage());
			}
		};
	}

}
