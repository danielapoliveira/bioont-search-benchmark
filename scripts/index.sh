#|/bin/bash

for i in OLS/ols-apps/ols-solr-app/src/main/resources/*.properties;
do
 p=${p##*-}
 p=${p%%.properties}
 echo $p
 if java -Xmx6g -jar -Dspring.profiles.active=$p OLS/ols-apps/ols-solr-app/target/ols-solr-app.jar; then
  echo $p >> properties_loaded.txt
 else
  echo $p >> properties_error.txt
 fi
done
