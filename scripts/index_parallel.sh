#!/bin/bash

loadprofiles() {
  p=$1
  p=${p##*-}
  p=${p%%.properties}
  echo $p
  if java -Xmx6g -jar -Dspring.profiles.active=$p OLS/ols-apps/ols-solr-app/target/ols-solr-app.jar 2>&1 | tee loaded/output-$p.log; then
    echo $p >> properties_loaded.txt
  else
    echo $p >> properties_error.txt
  fi
}

export -f loadprofiles

parallel -j3 loadprofiles <<<"$(ls OLS/ols-apps/ols-solr-app/src/main/resources/*.properties)"
