/*******************************************************************************
 * Copyright (c) 2016, 2018 Farrukh Ijaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package io.graphenee.jbpm.embedded;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.hibernate.dialect.Dialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

import io.graphenee.core.GrapheneeCoreConfiguration;
import io.graphenee.core.util.DataSourceUtil;

@Configuration
@ConditionalOnProperty(prefix = "graphenee", name = "modules.enabled", matchIfMissing = false)
@EnableJpaRepositories(entityManagerFactoryRef ="jbpmEntityManagerFactory",transactionManagerRef = "jbpmTransactionManager" ,basePackages = { "io.graphenee.jbpm.embedded.repository" })
@EntityScan({ GrapheneeCoreConfiguration.ENTITY_SCAN_BASE_PACKAGE, "io.graphenee.jbpm.embedded.model" })
@ComponentScan("io.graphenee.jbpm.embedded")
public class GrapheneeJbpmConfiguration {

	@Autowired(required = false)
	GrapheneeJbpmProperties grapheneeJbpmProperties;

	@Value("${flyway.enabled:false}")
	boolean flywayEnabled;

	@PostConstruct
	public void init() {
		if (flywayEnabled) {
			Flyway flyway = new Flyway();
			DataSource dsFlyway = grapheneeJbpmProperties().getDataSource();
			flyway.setDataSource(dsFlyway);
			String dbVendor = DataSourceUtil.determineDbVendor(dsFlyway);
			flyway.setLocations("classpath:db/jbpm/migration/" + dbVendor);
			flyway.setTable("jbpm_schema_version");
			flyway.setBaselineOnMigrate(true);
			flyway.setBaselineVersionAsString("0");
			flyway.migrate();
		}
	}

	private GrapheneeJbpmProperties grapheneeJbpmProperties() {
		if (grapheneeJbpmProperties == null) {
			synchronized (GrapheneeJbpmConfiguration.class) {
				if (grapheneeJbpmProperties == null) {
					grapheneeJbpmProperties = new GrapheneeJbpmProperties();
				}
			}
		}
		return grapheneeJbpmProperties;
	}

	@Bean("jbpmTransactionManager")
	public JpaTransactionManager jbpmTransactionManager(@Qualifier("jbpmEntityManagerFactory") EntityManagerFactory jbpmEntityManagerFactory) {
		JpaTransactionManager tm = new JpaTransactionManager(jbpmEntityManagerFactory);
		return tm;
	}

	@Bean("jbpmEntityManagerFactory")
	public EntityManagerFactory jbpmEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		DataSource ds = grapheneeJbpmProperties().getDataSource();
		em.setDataSource(ds);
		em.setPackagesToScan("org.jbpm", "org.drools","io.graphenee.jbpm.embedded.model");
		em.setPersistenceUnitName("org.jbpm.persistence.jpa");
		em.setPersistenceProviderClass(HibernatePersistenceProvider.class);
		em.setJpaDialect(new HibernateJpaDialect());
		em.setMappingResources("META-INF/JBPMorm.xml", "META-INF/Taskorm.xml", "META-INF/TaskAuditorm.xml");
		em.getJpaPropertyMap().put("hibernate.max_fetch_depth", "3");
		// em.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "none");
		em.getJpaPropertyMap().put("hibernate.show_sql", "false");
		Dialect dialect = DataSourceUtil.determineDialect(ds);
		em.getJpaPropertyMap().put("hibernate.dialect", dialect);
		em.getJpaPropertyMap().put("hibernate.id.new_generator_mappings", "false");
		em.getJpaPropertyMap().put("hibernate.transaction.jta.platform", "org.hibernate.service.jta.platform.internal.BitronixJtaPlatform");
		em.afterPropertiesSet();
		return em.getObject();
	}

}
