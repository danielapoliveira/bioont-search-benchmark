#|/bin/bash

PYTHON=python2

for i in OLS/ols-apps/ols-solr-app/src/main/resources/*.properties;
do
 p=$($PYTHON scripts/split.py $i) 
 echo $p
 (java -Xmx6g -jar -Dspring.profiles.active=$p OLS/ols-apps/ols-solr-app/target/ols-solr-app.jar && $PYTHON scripts/appendToFile.py $p false) ||
 $PYTHON appendToFile.py $p true
done
