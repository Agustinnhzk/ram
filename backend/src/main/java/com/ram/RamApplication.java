package com.ram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class RamApplication {

    public static void main(String[] args) {
        // Todo el backend trabaja en UTC: la base guarda fechas con zona (TIMESTAMPTZ)
        // y cada frontend las muestra en la hora local de quien mira.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(RamApplication.class, args);
    }

}