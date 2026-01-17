# openssl req -x509 -nodes -days 3650 -newkey rsa:2048 -keyout key.pem -out cert.pem
# Copy certificates
sudo cp cert.pem /etc/ssl/certs/
sudo cp key.pem /etc/ssl/private/
