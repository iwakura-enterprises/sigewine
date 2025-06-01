package enterprises.iwakura.sigewine.annotations;

import enterprises.iwakura.sigewine.aop.MethodWrapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class ClassWrappedMethodWrapper extends MethodWrapper<ClassWrapped> {

    public static int ranTimes = 0;

    public ClassWrappedMethodWrapper() {
        super(ClassWrapped.class);
    }

    @Override
    protected void beforeInvocation(Object target, Method method, Object[] args, ClassWrapped annotation, Object proxy) {
        ranTimes++;
    }

    @Override
    protected void afterInvocation(Object target, Method method, Object[] args, ClassWrapped annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy) {

    }
}
