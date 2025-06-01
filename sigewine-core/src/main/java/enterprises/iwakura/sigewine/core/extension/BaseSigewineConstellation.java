package enterprises.iwakura.sigewine.core.extension;

import enterprises.iwakura.sigewine.core.Sigewine;
import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;

import java.util.List;

public class BaseSigewineConstellation extends SigewineConstellation {

    public static final int PRIORITY = 0;

    public BaseSigewineConstellation() {
        super(PRIORITY);
    }

    @Override
    public List<Class<RomaritimeBean>> getBeanAnnotations() {
        return List.of(RomaritimeBean.class);
    }

    @Override
    public void processBeans(Sigewine sigewine) {

    }
}
