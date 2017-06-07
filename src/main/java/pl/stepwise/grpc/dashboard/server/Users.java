package pl.stepwise.grpc.dashboard.server;

import java.util.ArrayList;

import pl.stepwise.grpc.dashboard.messages.Messages;

public class Users extends ArrayList<Messages.User> {

    private static Users users;

    private static int seq = 0;

    public static Users getInstance() {
        if (users == null) {
            users = new Users();
        }
        return users;
    }

    private Users() {
        this.add(Messages.User.newBuilder()
                .setId(1)
                .setLogin("rgorzkowski")
                .setFirstName("Rafal")
                .setLastName("Gorzkowski")
                .build());

        this.add(Messages.User.newBuilder()
                .setId(2)
                .setLogin("jkowalski")
                .setFirstName("Jan")
                .setLastName("Kowalki")
                .build());

        this.add(Messages.User.newBuilder()
                .setId(3)
                .setLogin("anowak")
                .setFirstName("Anna")
                .setLastName("Nowak")
                .build());
    }

    @Override
    public boolean add(Messages.User User) {
        Messages.User copy = Messages.User.newBuilder(User)
                .setId(++seq)
                .build();
        return super.add(copy);
    }
}
