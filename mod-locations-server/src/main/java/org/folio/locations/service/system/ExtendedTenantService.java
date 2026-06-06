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
  private final DataLoadService dataLoadService;

  public ExtendedTenantService(JdbcTemplate jdbcTemplate,
                               FolioExecutionContext executionContext,
                               FolioSpringLiquibase folioSpringLiquibase,
                               KafkaAdminService kafkaAdminService,
                               DataLoadService dataLoadService) {
    super(jdbcTemplate, executionContext, folioSpringLiquibase);
    this.kafkaAdminService = kafkaAdminService;
    this.executionContext = executionContext;
    this.dataLoadService = dataLoadService;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    kafkaAdminService.createTopics(executionContext.getTenantId());
    kafkaAdminService.restartEventListeners();
  }

  @Override
  public void loadReferenceData() {
    super.loadReferenceData();
    dataLoadService.loadReferenceData();
  }

  @Override
  public void loadSampleData() {
    super.loadSampleData();
    dataLoadService.loadSampleData();
  }
}
