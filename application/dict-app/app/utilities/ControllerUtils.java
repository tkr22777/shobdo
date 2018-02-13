package utilities;

import Exceptions.EntityDoesNotExist;
import play.Logger;
import play.mvc.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.notFound;

public class ControllerUtils {

    private static Logger.ALogger log = Logger.of(ControllerUtils.class);

    public static Result executeEndpoint(String transactionId,
                                         String parentRequestId,
                                         String endpoint,
                                         Map<String, String> parameters,
                                         Supplier<Result> supplier) {

        try {
            String message = String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s]",
                    transactionId, parentRequestId, endpoint, parameters);
            log.info(message);
            return supplier.get();
        } catch (Exception ex) {
            return handleException(transactionId, parentRequestId, endpoint, parameters, ex);
        }
    }

    private static Result handleException(String transactionId,
                                         String parentRequestId,
                                         String endpoint,
                                         Map<String, String> parameters,
                                         Throwable throwable) {

        String message = String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s][Exception Message=%s]", transactionId,
                parentRequestId, endpoint, parameters, throwable.getMessage());

        if( throwable instanceof EntityDoesNotExist) {

            log.info(message, throwable);
            return notFound(throwable.getMessage());

        } else if ( throwable instanceof IllegalArgumentException) {

            log.info(message, throwable);
            return badRequest(throwable.getMessage());

        } else {

            log.error(message, throwable);
            return internalServerError(throwable.getMessage());
        }
    }
}
