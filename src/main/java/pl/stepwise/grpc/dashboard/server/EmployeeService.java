package pl.stepwise.grpc.dashboard.server;

import io.grpc.stub.StreamObserver;
import pl.stepwise.grpc.dashboard.messages.EmployeeServiceGrpc;
import pl.stepwise.grpc.dashboard.messages.Messages;

/**
 * Created by rafal on 6/5/17.
 */
public class EmployeeService extends EmployeeServiceGrpc.EmployeeServiceImplBase {

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
}
