package enterprises.iwakura.sigewine.entities;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.services.PrimordialService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
@Getter
public class AetherEntity implements BaseEntity {

    private final PrimordialService primordialService;

    @Override
    public String name() {
        return "Aether";
    }
}
