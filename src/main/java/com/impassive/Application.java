package com.impassive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

/** @author impassivey */
@PropertySource("file:///Users/impassivey/conf/lock/application.properties")
@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }
}
