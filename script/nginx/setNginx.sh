sudo apt install nginx -y
sudo cp ../cert/cert.pem /etc/ssl/certs/
sudo cp ../cert/key.pem /etc/ssl/private/
sudo cp kxreport.conf /etc/nginx/sites-available/
sudo ln -s /etc/nginx/sites-available/kxreport.conf /etc/nginx/sites-enabled/
sudo systemctl reload nginx