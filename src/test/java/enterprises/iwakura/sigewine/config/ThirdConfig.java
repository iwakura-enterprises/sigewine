package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.beans.SecondConfigBean;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThirdConfig {

    private final SecondConfigBean secondConfigBean;

    @Bean(name = "thirdConfigBean")
    public String someThirdBeanConfig() {
        return "";
    }
}
