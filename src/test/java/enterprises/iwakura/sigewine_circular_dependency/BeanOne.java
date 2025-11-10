package enterprises.iwakura.sigewine_circular_dependency;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class BeanOne {

    private final BeanTwo beanTwo;

}
