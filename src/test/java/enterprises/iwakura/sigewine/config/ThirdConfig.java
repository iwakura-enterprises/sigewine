package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.beans.SecondConfigBean;
import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThirdConfig {

    private final SecondConfigBean secondConfigBean;

    @RomaritimeBean(name = "thirdConfigBean")
    public String someThirdBeanConfig() {
        return "";
    }
}
