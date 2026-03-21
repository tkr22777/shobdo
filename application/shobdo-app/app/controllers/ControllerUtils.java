package controllers;

import exceptions.EntityDoesNotExist;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import utilities.ShobdoLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/* package private */ class ControllerUtils {

    private static ShobdoLogger log = new ShobdoLogger(ControllerUtils.class);

    static final Map<String, Integer> ROLE_LEVEL;
    static {
        final Map<String, Integer> m = new HashMap<>();
        m.put("USER", 1);
        m.put("REVIEWER", 2);
        m.put("ADMIN", 3);
        m.put("OWNER", 4);
        ROLE_LEVEL = Collections.unmodifiableMap(m);
    }

    static boolean hasMinRole(final String required) {
        final String role = Http.Context.current().session().get("userRole");
        if (role == null) return false;
        return ROLE_LEVEL.getOrDefault(role, 0) >= ROLE_LEVEL.getOrDefault(required, Integer.MAX_VALUE);
    }

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

        if (throwable instanceof EntityDoesNotExist) {
            log.info(String.format("[endpoint=%s] not found: %s", endpoint, throwable.getMessage()));
            return Results.notFound(throwable.getMessage());
        } else if (throwable instanceof IllegalArgumentException) {
            log.error(String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s]",
                transactionId, parentRequestId, endpoint, parameters), throwable);
            return Results.badRequest(throwable.getMessage());
        } else {
            log.error(String.format("[X-TransactionId=%s][X-Parent-Request-ID=%s][endpoint=%s][Parameters:%s]",
                transactionId, parentRequestId, endpoint, parameters), throwable);
            return Results.internalServerError(throwable.getMessage());
        }
    }
}
