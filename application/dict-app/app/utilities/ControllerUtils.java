package utilities;

import play.Logger;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.internalServerError;

public class ControllerUtils {

    private static Logger.ALogger log = Logger.of(ControllerUtils.class);

    public static Result executeEndpoint(String transactionId,
                                         String parentRequestId,
                                         String endpoint,
                                         Supplier<Result> supplier) {

        try {
            return supplier.get();
        } catch (Exception ex) {
            return handleException(transactionId, parentRequestId, endpoint, ex);
        }
    }

    public static Result handleException(String transactionId,
                                         String parentRequestId,
                                         String endpoint,
                                         Throwable throwable) {

        String message = String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint:", transactionId,
                parentRequestId, endpoint, throwable.getMessage());

        if( throwable instanceof IllegalArgumentException) {

            log.info(message, throwable);
            return badRequest(throwable.getMessage());

        } else {

            log.error(message, throwable);
            return internalServerError(throwable.getMessage());
        }
    }
}
