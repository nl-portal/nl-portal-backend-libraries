# How to generate self-signed client SSL certificates

```bash
openssl genrsa 2048 > ca-key.pem
openssl req -new -x509 -nodes -days 36500 -key ca-key.pem -subj '/CN=test.local/O=Test/C=NL' > ca-cert.pem
openssl req -newkey rsa:2048 -days 36500 -nodes -keyout client-key.pem -subj '/CN=test.local/O=Test/C=NL' > client-req.pem
openssl x509 -req -in client-req.pem -days 36500 -CA ca-cert.pem -CAkey ca-key.pem -set_serial 01 > client-cert.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -in client-key.pem -out client-key-pkcs8.pem
rm ca-cert.pem ca-key.pem client-req.pem
```
