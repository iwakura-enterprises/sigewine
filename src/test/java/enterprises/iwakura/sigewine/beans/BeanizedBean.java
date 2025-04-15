package enterprises.iwakura.sigewine.beans;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;

@RequiredArgsConstructor
public class BeanizedBean {

    public final Level logLevel;
    public int value = 413;

}
