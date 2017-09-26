#!/bin/bash
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

isql 1152 dba dba <<EOF
DELETE FROM DB.DBA.load_list;
ld_dir('ontologies','*.owl', NULL);
rdf_loader_run(log_enable=>3);
wait_for_children;
checkpoint;
EXIT;
EOF

java -jar script/bioont.jar
