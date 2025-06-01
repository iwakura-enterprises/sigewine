package enterprises.iwakura.sigewine.annotations;

import enterprises.iwakura.sigewine.MethodWrapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class TransactionalMethodWrapper extends MethodWrapper<Transactional> {

    public static boolean ran = false;

    public TransactionalMethodWrapper() {
        super(Transactional.class);
    }

    @Override
    protected void beforeInvocation(Object target, Method method, Object[] args, Transactional annotation, Object proxy) {
        ran = true;
    }

    @Override
    protected void afterInvocation(Object target, Method method, Object[] args, Transactional annotation, Optional<Object> optionalResult, Optional<Throwable> optionalThrowable, Object proxy) {

    }
}
