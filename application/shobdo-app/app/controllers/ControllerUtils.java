package controllers;

import exceptions.EntityDoesNotExist;
import play.mvc.Result;
import play.mvc.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

/* package private */ class ControllerUtils {

    private static Logger log = LoggerFactory.getLogger(ControllerUtils.class);

    /* package private */ static Result executeEndpoint(final String transactionId,
                                         final String parentRequestId,
                                         final String endpoint,
                                         final Map<String, String> parameters,
                                         final Supplier<Result> supplier) {

        log.debug(String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s]",
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

        log.error(String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s]",
            transactionId, parentRequestId, endpoint, parameters), throwable);

        if (throwable instanceof EntityDoesNotExist) {
            return Results.notFound(throwable.getMessage());
        } else if (throwable instanceof IllegalArgumentException) {
            return Results.badRequest(throwable.getMessage());
        } else {
            return Results.internalServerError(throwable.getMessage());
        }
    }
}
