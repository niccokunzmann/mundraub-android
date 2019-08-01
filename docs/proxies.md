# Proxies

Old Android phones are not able to use the new SSL certificates and are therefor not able
to retrieve up-to-date data from Na-Ovoce and other servers.

This is why proxy servers serve HTTP content.

## Na-Ovoce.cz

Example nginx configuration:

```
server {
	server_name na-ovoce.quelltext.eu;

	listen   80;

#	access_log off;
	
	# see this for the proxy: https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/
	# why this is here https://github.com/niccokunzmann/mundraub-android/pull/113#discussion_r217230294
	location /api/ {
		# use variables and a resolver, see https://stackoverflow.com/a/32846603
		resolver 8.8.8.8 valid=30s;
		set $upstream_na_ovoce test;
		proxy_pass https://na-ovoce.cz/api/;
	}
}
```

## Fruitmap.org

Example nginx configuration:

```
server {
	server_name fruitmap.quelltext.eu;

	listen   80;

#	access_log off;
	
	# see this for the proxy: https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/
	# why this is here https://github.com/niccokunzmann/mundraub-android/pull/113#discussion_r217230294
	location /api/ {
		proxy_ssl_server_name on; # from https://stackoverflow.com/a/25330027
		proxy_ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
		proxy_set_header Host www.fruitmap.org;
		proxy_set_header User-Agent $http_user_agent;
		proxy_pass https://www.fruitmap.org/;
	}
}
```
