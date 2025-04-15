package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.beans.BeanizedBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;

@RomaritimeBean
public class ConfigWithBeans {

    private final LoggingConfiguration loggingConfiguration;

    public ConfigWithBeans(@RomaritimeBean(name = "errorLogging") LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    @RomaritimeBean
    public BeanizedBean beanizedBean() {
        return new BeanizedBean(loggingConfiguration.getLogLevel());
    }
}
