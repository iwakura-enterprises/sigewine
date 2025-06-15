package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import lombok.Getter;

public abstract class SelfInjectedBaseClass {

    @Getter
    @RomaritimeBean
    private SelfInjectedBaseClass selfInjectedBaseClass;

}
