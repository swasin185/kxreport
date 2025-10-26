openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
  -keyout key.pem -out cert.pem
sudo cp *.pem /etc/tomcat10