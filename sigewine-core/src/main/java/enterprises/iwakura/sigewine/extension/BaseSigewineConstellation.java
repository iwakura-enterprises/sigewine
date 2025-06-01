package enterprises.iwakura.sigewine.extension;

import enterprises.iwakura.sigewine.BeanDefinition;
import enterprises.iwakura.sigewine.Sigewine;
import enterprises.iwakura.sigewine.annotations.RomaritimeBean;

import java.util.List;
import java.util.Map;

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
