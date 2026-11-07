package com.cesde.nexou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NexouApplication {

	public static void main(String[] args) {
		// Las horas de las reservas de equipos se envían en hora local: el servidor debe usar la misma zona
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(System.getenv().getOrDefault("APP_TIMEZONE", "America/Bogota")));
		SpringApplication.run(NexouApplication.class, args);
	}

}
