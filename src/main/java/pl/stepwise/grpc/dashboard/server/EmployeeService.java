package pl.stepwise.grpc.dashboard.server;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import pl.stepwise.grpc.dashboard.messages.EmployeeServiceGrpc;
import pl.stepwise.grpc.dashboard.messages.Messages;
import pl.stepwise.grpc.dashboard.messages.Messages.Employee;
import pl.stepwise.grpc.dashboard.messages.Messages.EmployeeRequest;
import pl.stepwise.grpc.dashboard.messages.Messages.EmployeeResponse;
import pl.stepwise.grpc.dashboard.messages.Messages.UploadPhotoRequest;
import com.google.protobuf.ByteString;

/**
 * Created by rafal on 6/5/17.
 */
public class EmployeeService extends EmployeeServiceGrpc.EmployeeServiceImplBase {

    // unary call
    @Override
    public void getByLogin(Messages.GetByLoginRequest request,
            StreamObserver<EmployeeResponse> responseObserver) {
        for (Employee employee : Employees.getInstance()) {
            if (employee.getLogin().equals(request.getLogin())) {
                EmployeeResponse response = EmployeeResponse.newBuilder()
                        .setEmployee(employee)
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
//                new Exception("Employee not found with login: " + request.getLogin())
        );
    }

    //server side streaming
    @Override
    public void getAll(Messages.GetAllRequest request, StreamObserver<EmployeeResponse> responseObserver) {
        for (Employee employee : Employees.getInstance()) {
            EmployeeResponse response = EmployeeResponse.newBuilder()
                    .setEmployee(employee)
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
    public StreamObserver<EmployeeRequest> saveAll(StreamObserver<EmployeeResponse> responseObserver) {
        return new StreamObserver<EmployeeRequest>() {
            @Override
            public void onNext(EmployeeRequest value) {
                Employees.getInstance().add(value.getEmployee());
                responseObserver.onNext(
                        EmployeeResponse.newBuilder()
                                .setEmployee(value.getEmployee())
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
                for (Employee e : Employees.getInstance()) {
                    System.out.println(e);
                }
                responseObserver.onCompleted();
            }
        };
    }
}
