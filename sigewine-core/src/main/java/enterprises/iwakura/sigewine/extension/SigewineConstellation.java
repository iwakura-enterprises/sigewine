package enterprises.iwakura.sigewine.extension;

import enterprises.iwakura.sigewine.BeanDefinition;
import enterprises.iwakura.sigewine.MethodWrapper;
import enterprises.iwakura.sigewine.Sigewine;
import enterprises.iwakura.sigewine.annotations.RomaritimeBean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public abstract class SigewineConstellation {

    protected final int priority;

    public abstract List<Class<RomaritimeBean>> getBeanAnnotations();

    public abstract void processBeans(Sigewine sigewine);

}
