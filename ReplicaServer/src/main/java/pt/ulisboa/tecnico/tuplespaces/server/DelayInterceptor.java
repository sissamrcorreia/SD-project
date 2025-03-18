package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.*;

public class DelayInterceptor implements ServerInterceptor {
    private static final Metadata.Key<String> DELAY_KEY =
            Metadata.Key.of("delay1", Metadata.ASCII_STRING_MARSHALLER); // TODO: Change to delay after FE is done

        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        String delayValue = headers.get(DELAY_KEY);
        if (delayValue != null) {
            try {
                int delaySeconds = Integer.parseInt(delayValue);
                ServerMain.debug(DelayInterceptor.class.getSimpleName(), "Delaying call for " + delaySeconds + " seconds");
                Thread.sleep(delaySeconds * 1000); // Convert to ms
            } catch (NumberFormatException e) {
                ServerMain.debug(DelayInterceptor.class.getSimpleName(), "Value for delay is not a number: " + delayValue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                call.close(Status.ABORTED.withDescription("Thread interrupted"), new Metadata());
                ServerMain.debug(DelayInterceptor.class.getSimpleName(), "Thread interrupted");
            }
        }

        // pass the call to the next handler
        return next.startCall(call, headers);
    } 
}
