package com.soledad.inventariopapeleria;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventarioPapeleriaApplication {

    public static void main(String[] args) {
        // RN-39: todas las fechas se registran en America/Bogota
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        SpringApplication.run(InventarioPapeleriaApplication.class, args);
    }

}