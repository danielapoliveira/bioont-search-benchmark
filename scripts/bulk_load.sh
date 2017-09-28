#!/bin/bash
PORT=1111
USER=dba
PASSWORD=dba
VIRT_DB=$PWD/virt_database

ONTO_DB=$VIRT_DB/tmp_ontologies
mkdir -p $ONTO_DB

echo
echo Downloading ontologies...
echo

wget -N -P $ONTO_DB -i userinput/uris.txt

for i in $ONTO_DB/*.owl;
do
 filename="${i##*/}"
 path="${i%/*}/" 
 echo $filename
 echo -e  "$filename" > ${path}${filename}.graph
done

echo
echo Bulk loading ontologies, please wait for the it to finish...
echo

isql $PORT $USER $PASSWORD <<EOF
DELETE FROM DB.DBA.load_list;
ld_dir('$ONTO_DB','*.owl', NULL);
rdf_loader_run(log_enable=>3);
wait_for_children;
checkpoint;
EXIT;
EOF

echo
echo DONE
echo
