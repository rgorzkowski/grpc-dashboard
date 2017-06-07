grpc Examples
==============================================

**Installation**

Ask for a certificate file for a servers, as well as the private key:

`openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes`

Certificate cert.pem is what you're going to expose publicly, and the key.pem is going 
to be kept private to your server.

*Put localhost in Common Name.*

**Examples**

To build the examples, run in this directory:

```
$ ./gradlew installDist
```

This creates the scripts `user-service-server` and `user-service-client` in the
`build/install/examples/bin/` directory that run the examples. Each
example requires the server to be running before starting the client.

For example, to try the User service example first run:

```
$ ./build/install/examples/bin/user-service-server
```

And in a different terminal window run:

```
$ ./build/install/examples/bin/user-service-client
```

That's it!
