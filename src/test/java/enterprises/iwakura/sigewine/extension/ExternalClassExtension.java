package enterprises.iwakura.sigewine.extension;

import java.util.List;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.entities.BaseEntity;
import lombok.RequiredArgsConstructor;

@Bean // Not needed!
@RequiredArgsConstructor
public class ExternalClassExtension {

    private final List<BaseEntity> entities;

    @Bean
    public ExternalClass externalClass() {
        return new ExternalClass(entities);
    }

    @Bean
    public OtherExternalClass otherExternalClass() {
        return new OtherExternalClass();
    }
}
