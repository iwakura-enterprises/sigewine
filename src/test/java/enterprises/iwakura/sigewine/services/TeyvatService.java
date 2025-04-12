package enterprises.iwakura.sigewine.services;

import enterprises.iwakura.sigewine.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.beans.LoggingConfiguration;
import enterprises.iwakura.sigewine.entities.BaseEntity;
import enterprises.iwakura.sigewine.utils.collections.TypedArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RomaritimeBean
@RequiredArgsConstructor
public class TeyvatService {

    private final PrimordialService primordialService;
    private final SecondWhoCameService secondWhoCameService;
    private final LoggingConfiguration loggingConfiguration; // Will be the info one

    @RomaritimeBean
    private final List<BaseEntity> entities = new TypedArrayList<>(BaseEntity.class);

}
