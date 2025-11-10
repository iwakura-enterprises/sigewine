package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.Getter;

public abstract class SelfInjectedBaseClass {

    @Getter
    @Bean
    private SelfInjectedBaseClass selfInjectedBaseClass;

}
