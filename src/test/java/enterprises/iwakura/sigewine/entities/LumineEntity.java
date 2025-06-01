package enterprises.iwakura.sigewine.entities;

import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;

@RomaritimeBean
public class LumineEntity implements BaseEntity {

    @Override
    public String name() {
        return "Lumine";
    }
}
