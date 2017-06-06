package pl.stepwise.grpc.dashboard.server;

import io.grpc.stub.StreamObserver;
import pl.stepwise.grpc.dashboard.messages.EmployeeServiceGrpc;
import pl.stepwise.grpc.dashboard.messages.Messages;
import com.google.protobuf.ByteString;

/**
 * Created by rafal on 6/5/17.
 */
public class EmployeeService extends EmployeeServiceGrpc.EmployeeServiceImplBase {

    // unary call
    @Override
    public void getByLogin(Messages.GetByLoginRequest request,
            StreamObserver<Messages.EmployeeResponse> responseObserver) {
        for (Messages.Employee employee : Employees.getInstance()) {
            if (employee.getLogin().equals(request.getLogin())) {
                Messages.EmployeeResponse response = Messages.EmployeeResponse.newBuilder()
                        .setEmployee(employee)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted(); // unary call
                return;
            }
        }
        responseObserver.onError(new Exception("Employee not found with login: " + request.getLogin()));
    }

    //server side streaming
    @Override
    public void getAll(Messages.GetAllRequest request, StreamObserver<Messages.EmployeeResponse> responseObserver) {
        for (Messages.Employee employee : Employees.getInstance()) {
            Messages.EmployeeResponse response = Messages.EmployeeResponse.newBuilder()
                    .setEmployee(employee)
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    //client side streaming
    @Override
    public StreamObserver<Messages.UploadPhotoRequest> uploadPhoto(
            StreamObserver<Messages.UploadPhotoResponse> responseObserver) {
        return new StreamObserver<Messages.UploadPhotoRequest>() {

            private ByteString photo;

            @Override
            public void onNext(Messages.UploadPhotoRequest value) {
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
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }
}
