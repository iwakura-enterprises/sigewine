package enterprises.iwakura.sigewine.entities;

import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class AetherEntity implements BaseEntity {

    @Override
    public String name() {
        return "Aether";
    }
}
