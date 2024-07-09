package uk.gov.companieshouse.pscstatementdataapi.logging;

import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Component
@Aspect
public class MessageLoggingAspect {

    private static final String LOG_MESSAGE_RECEIVED = "Processing delta";
    private static final String LOG_MESSAGE_PROCESSED = "Processed delta";
    private static final String EXCEPTION_MESSAGE = "%s exception thrown: %s";

    private final Logger logger;

    public MessageLoggingAspect(Logger logger) {
        this.logger = logger;
    }

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object manageStructuredLogging(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            Message<?> message = (Message<?>) joinPoint.getArgs()[0];
            DataMapHolder.initialise(extractContextId(message.getPayload())
                    .orElse(UUID.randomUUID().toString()));

            String topic = (String) joinPoint.getArgs()[1];
            String partition = (String) joinPoint.getArgs()[2];
            String offset = (String) joinPoint.getArgs()[3];
            DataMapHolder.get()
                    .topic(topic)
                    .partition(Integer.valueOf(partition))
                    .offset(Long.valueOf(offset));

            logger.debug(LOG_MESSAGE_RECEIVED, DataMapHolder.getLogMap());

            Object result = joinPoint.proceed();

            logger.debug(LOG_MESSAGE_PROCESSED, DataMapHolder.getLogMap());

            return result;
        } catch (Exception ex) {
            logger.debug(String.format(EXCEPTION_MESSAGE, ex.getClass().getSimpleName(),
                    ex.getMessage()), DataMapHolder.getLogMap());
            throw ex;
        } finally {
            DataMapHolder.clear();
        }
    }

    private Optional<String> extractContextId(Object payload) {
        if (payload instanceof ChsDelta) {
            return Optional.of(((ChsDelta)payload).getContextId());
        } else if (payload instanceof ResourceChangedData) {
            return Optional.of(((ResourceChangedData)payload).getContextId());
        }
        return Optional.empty();
    }
}
