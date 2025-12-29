package enterprises.iwakura.sigewine.extension;

import java.util.List;

import enterprises.iwakura.sigewine.entities.BaseEntity;
import lombok.Getter;

@Getter
public class ExternalClass {

    private final List<BaseEntity> entities;

    public ExternalClass(List<BaseEntity> entities) {
        this.entities = entities;
    }

    public void doSomething() {
        System.out.println("Doing something in ExternalClass");
        System.out.printf("There is a total of %d entities.%n", entities.size());
    }
}
