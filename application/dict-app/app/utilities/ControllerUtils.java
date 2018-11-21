package utilities;

import exceptions.EntityDoesNotExist;
import play.mvc.Result;

import java.util.Map;
import java.util.function.Supplier;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.notFound;

public class ControllerUtils {

    private static LogPrint log = new LogPrint(ControllerUtils.class);

    public static Result executeEndpoint(final String transactionId,
                                         final String parentRequestId,
                                         final String endpoint,
                                         final Map<String, String> parameters,
                                         final Supplier<Result> supplier) {

        log.info(String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s]",
            transactionId, parentRequestId, endpoint, parameters));

        try {
            return supplier.get();
        } catch (Exception  ex) {
            return handleException(transactionId, parentRequestId, endpoint, parameters, ex);
        }
    }

    private static Result handleException(final String transactionId,
                                          final String parentRequestId,
                                          final String endpoint,
                                          final Map<String, String> parameters,
                                          final Throwable throwable) {

        log.info(String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s]"
            + "[Exception Message=%s]", transactionId, parentRequestId, endpoint, parameters, throwable.getMessage()));

        if (throwable instanceof EntityDoesNotExist) {
            return notFound(throwable.getMessage());
        } else if (throwable instanceof IllegalArgumentException) {
            return badRequest(throwable.getMessage());
        } else {
            return internalServerError(throwable.getMessage());
        }
    }
}
