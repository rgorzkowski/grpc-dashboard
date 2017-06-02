Ask for a certificate file for a servers, as well as the private key:

`openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes`

Certificate cert.pem is what you're going to expose publicly, and the key.pem is going 
to be kept private to your server.

*Put localhost in Common Name.*

