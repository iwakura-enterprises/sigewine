package enterprises.iwakura.sigewine.core;

import enterprises.iwakura.sigewine.core.extension.InjectBeanExtension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Holds configuration options for {@link Sigewine}.
 */
@Data
@Builder
@AllArgsConstructor
public class SigewineOptions {

    /**
     * The priority for {@link InjectBeanExtension}
     */
    @Builder.Default
    private int injectBeanExtensionPriority = 500;

    /**
     * Whether to register Sigewine itself as a bean.
     */
    @Builder.Default
    private boolean registerItselfAsBean = true;

    /**
     * Default constructor for {@link SigewineOptions}.
     */
    public SigewineOptions() {
    }
}
