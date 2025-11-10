package enterprises.iwakura.sigewine_circular_dependency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import enterprises.iwakura.sigewine.core.Sigewine;

public class SigewineCircularDependencyTest {

    @Test
    public void run() {
        Sigewine sigewine = new Sigewine();
        Assertions.assertThrows(IllegalStateException.class, () -> {
            sigewine.scan(SigewineCircularDependencyTest.class);
        });
    }
}
