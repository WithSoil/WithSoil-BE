package com.outthedoor.withsoil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WithSoilApplication {

	public static void main(String[] args) {
		SpringApplication.run(WithSoilApplication.class, args);
	}

}
