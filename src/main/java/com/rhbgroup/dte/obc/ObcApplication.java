package com.rhbgroup.dte.obc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ObcApplication {

  public static void main(String[] args) {
    SpringApplication.run(ObcApplication.class, args);
  }
}
