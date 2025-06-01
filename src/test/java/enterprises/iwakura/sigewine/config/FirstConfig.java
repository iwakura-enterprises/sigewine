package enterprises.iwakura.sigewine.config;

import enterprises.iwakura.sigewine.beans.FirstConfigBean;
import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;

public class FirstConfig {

    @RomaritimeBean
    public FirstConfigBean firstConfigBean() {
        return new FirstConfigBean();
    }

}
