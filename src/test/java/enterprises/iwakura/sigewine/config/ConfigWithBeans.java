package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.beans.BeanizedBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import enterprises.iwakura.sigewine.services.BaseDatabaseService;

@RomaritimeBean
public class ConfigWithBeans {

    private final LoggingConfiguration loggingConfiguration;
    private final BaseDatabaseService service;

    public ConfigWithBeans(@RomaritimeBean(name = "errorLogging") LoggingConfiguration loggingConfiguration, @RomaritimeBean BaseDatabaseService service) {
        this.loggingConfiguration = loggingConfiguration;
        this.service = service;
    }

    @RomaritimeBean
    public BeanizedBean beanizedBean() {
        return new BeanizedBean(loggingConfiguration.getLogLevel());
    }
}
