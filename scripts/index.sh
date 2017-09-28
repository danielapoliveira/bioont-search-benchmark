#|/bin/bash

for i in OLS/ols-apps/ols-solr-app/src/main/resources/*.properties;
do
 p=$(python scripts/split.py $i) 
 echo $p
 #(java -Xmx6g -jar -Dspring.profiles.active=mp OLS/ols-apps/ols-solr-app/target/ols-solr-app.jar && python scripts/appendToFile.py mp false) ||
 #python appendToFile.py mp true
 (java -Xmx6g -jar -Dspring.profiles.active=$p OLS/ols-apps/ols-solr-app/target/ols-solr-app.jar && python scripts/appendToFile.py $p false) ||
 python appendToFile.py $p true
done
