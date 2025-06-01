package enterprises.iwakura.sigewine.core.extension;

import enterprises.iwakura.sigewine.core.Sigewine;
import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class SigewineConstellation {

    protected final int priority;

    public abstract List<Class<RomaritimeBean>> getBeanAnnotations();

    public abstract void processBeans(Sigewine sigewine);

}
