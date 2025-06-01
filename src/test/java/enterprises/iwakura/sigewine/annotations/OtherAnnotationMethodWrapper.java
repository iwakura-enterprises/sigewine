package enterprises.iwakura.sigewine.annotations;

import enterprises.iwakura.sigewine.aop.MethodWrapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class OtherAnnotationMethodWrapper extends MethodWrapper<OtherAnnotation> {

    public static boolean ran = false;

    public OtherAnnotationMethodWrapper() {
        super(OtherAnnotation.class);
    }

    @Override
    protected void beforeInvocation(Object target, Method method, Object[] args, OtherAnnotation annotation, Object proxy) {
        ran = true;
    }

    @Override
    protected void afterInvocation(Object target, Method method, Object[] args, OtherAnnotation annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy) {

    }
}
