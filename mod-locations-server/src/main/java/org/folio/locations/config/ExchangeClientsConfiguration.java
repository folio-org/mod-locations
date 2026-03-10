package org.folio.locations.config;

import org.folio.locations.client.ConsortiumTenantsClient;
import org.folio.locations.client.UserTenantsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ExchangeClientsConfiguration {

  @Bean
  public ConsortiumTenantsClient consortiumTenantsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(ConsortiumTenantsClient.class);
  }

  @Bean
  public UserTenantsClient userTenantsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(UserTenantsClient.class);
  }
}
