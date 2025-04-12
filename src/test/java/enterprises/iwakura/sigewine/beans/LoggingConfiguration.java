package enterprises.iwakura.sigewine.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;

/**
 * Some logging configuration.
 */
@Getter
@RequiredArgsConstructor
public class LoggingConfiguration {

    private final Level logLevel;

}
