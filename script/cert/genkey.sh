# openssl req -x509 -nodes -days 3650 -newkey rsa:2048 -keyout key.pem -out cert.pem
# Copy certificates
sudo mkdir /etc/cert
sudo cp cert.pem /etc/cert/
sudo cp key.pem /etc/cert/
sudo chgrp tomcat /etc/cert/*.pem
sudo chmod 640 /etc/cert/*.pem
sudo cp server.xml /var/lib/tomcat10/conf