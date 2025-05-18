package enterprises.iwakura.sigewine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.event.Level;

/**
 * Holds configuration options for {@link Sigewine}.
 */
@Getter
@Builder
public class SigewineOptions {

    /**
     * Default constructor for {@link SigewineOptions}.
     */
    public SigewineOptions() {
    }
}
