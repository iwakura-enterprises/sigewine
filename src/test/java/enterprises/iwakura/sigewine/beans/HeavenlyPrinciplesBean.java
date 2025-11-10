package enterprises.iwakura.sigewine.beans;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.core.utils.BeanAccessor;
import enterprises.iwakura.sigewine.services.TeyvatService;
import lombok.Getter;

@Bean
@Getter
public class HeavenlyPrinciplesBean {

    @Bean
    private HeavenlyPrinciplesRule heavenlyPrinciplesRule;

    @Bean
    private final BeanAccessor<TeyvatService> teyvatServiceAccessor = new BeanAccessor<>(TeyvatService.class);

}
