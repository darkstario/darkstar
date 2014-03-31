The example.com.pem.key and example.com.pem.crt files in this directory were created for testing as follows:

    openssl req -x509 -newkey rsa:2048 -keyout example.com.pem.key -out example.com.pem.crt -days 3652 -nodes

This creates a (non-password-protected) private key and a self-signed certificate, valid for 10 years.

These files are intended for testing purposes only and shouldn't be used in a production system.
