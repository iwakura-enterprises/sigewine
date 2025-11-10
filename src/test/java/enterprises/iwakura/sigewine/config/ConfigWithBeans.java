package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.beans.BeanizedBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import enterprises.iwakura.sigewine.services.BaseDatabaseService;

@Bean
public class ConfigWithBeans {

    private final LoggingConfiguration loggingConfiguration;
    private final BaseDatabaseService service;

    public ConfigWithBeans(@Bean(name = "errorLogging") LoggingConfiguration loggingConfiguration, @Bean BaseDatabaseService service) {
        this.loggingConfiguration = loggingConfiguration;
        this.service = service;
    }

    @Bean
    public BeanizedBean beanizedBean() {
        return new BeanizedBean(loggingConfiguration.getLogLevel());
    }
}
