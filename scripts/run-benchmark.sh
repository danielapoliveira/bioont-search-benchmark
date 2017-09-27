#!/bin/bash
PORT=1111
USER=dba
PASSWORD=dba

mkdir ontologies
wget -P ontologies -i userinput/uris.txt

for i in ontologies/*.owl;
do
 filename="${i##*/}"
 path="${i%/*}/" 
 echo $filename
 echo $path
 echo -e  "$filename" > ${path}${filename}.graph
done

isql $PORT $USER $PASSWORD <<EOF
DELETE FROM DB.DBA.load_list;
ld_dir('ontologies','*.owl', NULL);
rdf_loader_run(log_enable=>3);
wait_for_children;
checkpoint;
EXIT;
EOF

java -jar target/bioont-1.0-SNAPSHOT-shaded.jar
