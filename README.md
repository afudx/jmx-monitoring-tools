# jmx-monitoring-tools

Untuk menjalankan include library yang ada di folder lib. Aplikasi membutuhkan parameter berupa path yang mengarah ke file serverlist.txt

Contoh isi serverlist.txt :
	service:jmx:remoting-jmx://127.0.0.1:10004,admin,admin#123,1,1000,/data/tmp/jmx-data-test
	format: service url,username,password,interval (detik),loop count,output folder
