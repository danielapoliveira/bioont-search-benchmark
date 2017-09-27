#|/bin/bash

for i in ./ols-apps/ols-solr-app/src/main/resources/*.properties;
do
 p=$(python split.py $i)
 echo $p
 (java -Xmx6g -jar -Dspring.profiles.active=$p ols/ols-apps/ols-solr-app/target/ols-solr-app.jar && python appendToFile.py $p false) ||
 python appendToFile.py $p true
done
