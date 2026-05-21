package org.folio.locations;

import org.folio.locations.config.EcsTlrProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EcsTlrProperties.class)
public class LocationsApplication {

  public static void main(String[] args) {
    SpringApplication.run(LocationsApplication.class, args);
  }
}
