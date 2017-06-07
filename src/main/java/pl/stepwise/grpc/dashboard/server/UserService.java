package pl.stepwise.grpc.dashboard.server;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pl.stepwise.grpc.dashboard.messages.UserServiceGrpc;
import pl.stepwise.grpc.dashboard.messages.Messages;
import pl.stepwise.grpc.dashboard.messages.Messages.User;
import pl.stepwise.grpc.dashboard.messages.Messages.UserRequest;
import pl.stepwise.grpc.dashboard.messages.Messages.UserResponse;
import pl.stepwise.grpc.dashboard.messages.Messages.UploadPhotoRequest;
import com.google.protobuf.ByteString;

/**
 * Created by rafal on 6/5/17.
 */
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    // unary call
    @Override
    public void getByLogin(Messages.GetByLoginRequest request,
            StreamObserver<UserResponse> responseObserver) {
        for (User User : Users.getInstance()) {
            if (User.getLogin().equals(request.getLogin())) {
                UserResponse response = UserResponse.newBuilder()
                        .setUser(User)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted(); // unary call
                return;
            }
        }

        Metadata md = new Metadata();
        md.put(Metadata.Key.of("missingLogin", Metadata.ASCII_STRING_MARSHALLER), request.getLogin());
        responseObserver.onError(
                new StatusRuntimeException(Status.NOT_FOUND, md)
//                new Exception("User not found with login: " + request.getLogin())
        );
    }

    //server side streaming
    @Override
    public void getAll(Messages.GetAllRequest request, StreamObserver<UserResponse> responseObserver) {
        for (User User : Users.getInstance()) {
            UserResponse response = UserResponse.newBuilder()
                    .setUser(User)
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    //client side streaming
    @Override
    public StreamObserver<UploadPhotoRequest> uploadPhoto(
            StreamObserver<Messages.UploadPhotoResponse> responseObserver) {
        return new StreamObserver<UploadPhotoRequest>() {

            private ByteString photo;

            @Override
            public void onNext(UploadPhotoRequest value) {
                if (photo == null) {
                    photo = value.getData();
                } else {
                    photo = photo.concat(value.getData());
                }
                System.out.println("Message with " + value.getData().size() + " bytes has been received.");
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Total bytes received -> " + photo.size());
                responseObserver.onNext(
                        Messages.UploadPhotoResponse.newBuilder()
                                .setStatus("OK")
                                .setBody(photo)
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    // bidirectional streaming
    @Override
    public StreamObserver<UserRequest> saveAll(StreamObserver<UserResponse> responseObserver) {
        return new StreamObserver<UserRequest>() {
            @Override
            public void onNext(UserRequest value) {
                Users.getInstance().add(value.getUser());
                responseObserver.onNext(
                        UserResponse.newBuilder()
                                .setUser(value.getUser())
                                .build()
                );
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("On completed method has been called.");
                for (User e : Users.getInstance()) {
                    System.out.println(e);
                }
                responseObserver.onCompleted();
            }
        };
    }
}
