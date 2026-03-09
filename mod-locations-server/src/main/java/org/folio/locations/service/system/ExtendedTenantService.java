package org.folio.locations.service.system;

import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service
public class ExtendedTenantService extends TenantService {

  private final KafkaAdminService kafkaAdminService;
  private final FolioExecutionContext executionContext;

  public ExtendedTenantService(JdbcTemplate jdbcTemplate,
                               FolioExecutionContext executionContext,
                               FolioSpringLiquibase folioSpringLiquibase,
                               KafkaAdminService kafkaAdminService) {
    super(jdbcTemplate, executionContext, folioSpringLiquibase);
    this.kafkaAdminService = kafkaAdminService;
    this.executionContext = executionContext;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    kafkaAdminService.createTopics(executionContext.getTenantId());
    kafkaAdminService.restartEventListeners();
  }
}
