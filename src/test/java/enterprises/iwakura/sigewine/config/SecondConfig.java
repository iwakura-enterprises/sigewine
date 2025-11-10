package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.beans.FirstConfigBean;
import enterprises.iwakura.sigewine.beans.SecondConfigBean;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecondConfig {

    private final FirstConfigBean firstConfigBean;

    @Bean
    public SecondConfigBean secondConfigBean() {
        return new SecondConfigBean();
    }
}
