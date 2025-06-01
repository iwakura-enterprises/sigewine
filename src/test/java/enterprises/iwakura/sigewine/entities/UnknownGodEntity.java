package enterprises.iwakura.sigewine.entities;

import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;

@RomaritimeBean
public class UnknownGodEntity implements BaseEntity {

    @Override
    public String name() {
        return "Unknown God";
    }
}
