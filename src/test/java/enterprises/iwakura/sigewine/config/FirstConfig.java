package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.beans.FirstConfigBean;
import enterprises.iwakura.sigewine.core.annotations.Bean;

public class FirstConfig {

    @Bean
    public FirstConfigBean firstConfigBean() {
        return new FirstConfigBean();
    }

}
