package pl.stepwise.grpc.dashboard.server;

import java.util.ArrayList;

import pl.stepwise.grpc.dashboard.messages.Messages;

public class Employees extends ArrayList<Messages.Employee> {

    private static Employees employees;

    public static Employees getInstance() {
        if (employees == null) {
            employees = new Employees();
        }
        return employees;
    }

    private Employees() {
        this.add(Messages.Employee.newBuilder()
                .setId(1)
                .setLogin("rgorzkowski")
                .setFirstName("Rafal")
                .setLastName("Gorzkowski")
                .build());

        this.add(Messages.Employee.newBuilder()
                .setId(2)
                .setLogin("jkowalski")
                .setFirstName("Jan")
                .setLastName("Kowalki")
                .build());

        this.add(Messages.Employee.newBuilder()
                .setId(3)
                .setLogin("anowak")
                .setFirstName("Anna")
                .setLastName("Nowak")
                .build());
    }

}
