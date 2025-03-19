package pt.ulisboa.tecnico.tuplespaces.frontend;

import io.grpc.*;

public class DelayInterceptor implements ServerInterceptor {
    public static final Metadata.Key<String> DELAY1 = Metadata.Key.of("delay1", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> DELAY2 = Metadata.Key.of("delay2", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> DELAY3 = Metadata.Key.of("delay3", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> CTX_DELAY1 = Context.key("delay1");
    public static final Context.Key<String> CTX_DELAY2 = Context.key("delay2");
    public static final Context.Key<String> CTX_DELAY3 = Context.key("delay3");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    ) {
        // Extract delay values from the headers and store them in the context
        Context context = Context.current()
            .withValue(CTX_DELAY1, headers.get(DELAY1))
            .withValue(CTX_DELAY2, headers.get(DELAY2))
            .withValue(CTX_DELAY3, headers.get(DELAY3));

        return Contexts.interceptCall(context, call, headers, next);
    }
}