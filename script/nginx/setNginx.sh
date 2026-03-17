apt install nginx -y
cp ../cert/cert.pem /etc/ssl/certs/
cp ../cert/key.pem /etc/ssl/private/
cp kxreport.conf /etc/nginx/sites-available/
ln -s /etc/nginx/sites-available/kxreport.conf /etc/nginx/sites-enabled/
systemctl daemon-reload
systemctl reload nginx
iptables -I INPUT -p tcp --dport 80 -j ACCEPT
iptables -I INPUT -p tcp --dport 443 -j ACCEPT
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
iptables -I INPUT -p tcp --dport 8443 -j ACCEPT
netfilter-persistent save