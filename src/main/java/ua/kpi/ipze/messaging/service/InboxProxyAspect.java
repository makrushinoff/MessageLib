package ua.kpi.ipze.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.ipze.messaging.api.Message;
import ua.kpi.ipze.messaging.api.StableMessagingConfigurer;
import ua.kpi.ipze.messaging.dao.InboxDao;
import ua.kpi.ipze.messaging.model.Inbox;
import ua.kpi.ipze.messaging.state.InboxStateHolder;

import java.time.LocalDateTime;

@Aspect
public class InboxProxyAspect {

    private static final Logger log = LoggerFactory.getLogger(InboxProxyAspect.class);

    private final ObjectMapper objectMapper = StableMessagingConfigurer.objectMapper();
    private final InboxDao inboxDao = InboxDao.getInstance();

    @Around("execution(* ua.kpi.ipze.messaging.api.MessageReceiver+.receiveMessage(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Proxying {}#{} to store messages", joinPoint.getTarget(), ((MethodSignature)joinPoint.getSignature()).getMethod().getName());
        Inbox inbox = new Inbox();
        Message message;
        try {
            message = objectMapper.readValue((String) joinPoint.getArgs()[0], Message.class);
        } catch (Exception e) {
            log.debug("Proceed with {}#{} original method", joinPoint.getTarget(), ((MethodSignature)joinPoint.getSignature()).getMethod().getName());
            return joinPoint.proceed();
        }
        boolean messageExists = inboxDao.existsById(message.getId());
        if (messageExists) {
            log.debug("Receiver {}#{} got duplicated message. Skipping", joinPoint.getTarget(), ((MethodSignature)joinPoint.getSignature()).getMethod().getName());
            return null;
        }
        inbox.setId(message.getId());
        inbox.setReceivedDateTime(LocalDateTime.now());
        inbox.setQueue(InboxStateHolder.getQueueNameForReceiver(joinPoint.getTarget().toString()));
        inbox.setMessage(message.getValue());
        inboxDao.create(inbox);
        log.debug("Proxied and stored message from receiver {}#{}", joinPoint.getTarget(), ((MethodSignature)joinPoint.getSignature()).getMethod().getName());
        return null;
    }

    public static InboxProxyAspect aspectOf() {
        return new InboxProxyAspect();
    }

}
