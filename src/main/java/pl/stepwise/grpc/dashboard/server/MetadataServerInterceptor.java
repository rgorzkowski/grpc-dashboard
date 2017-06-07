package pl.stepwise.grpc.dashboard.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * Created by rafal on 6/5/17.
 */
public class MetadataServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> next) {
        if (serverCall.getMethodDescriptor().getFullMethodName()
                .startsWith("UserService")) {
            for (String key : metadata.keys()) {
                System.out.println(key + ":" + metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)));
            }
        }
        return next.startCall(serverCall, metadata);
    }
}
