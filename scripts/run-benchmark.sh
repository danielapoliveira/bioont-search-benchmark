#!/bin/bash
PORT=1151
USER=dba
PASSWORD=dba
VIRT_DB=$PWD/virt_database

ONTO_DB=VIRT_DB/tmp_ontologies
mkdir -p $ONTO_DB
wget -P $ONTO_DB -i userinput/uris.txt

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
ld_dir('$ONTO_DB','*.owl', NULL);
rdf_loader_run(log_enable=>3);
wait_for_children;
checkpoint;
EXIT;
EOF
