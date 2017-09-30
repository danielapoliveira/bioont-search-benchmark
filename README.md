# bioont-search-benchmark
This is a Maven project that contains source code in Java and ground truth data for a biomedical ontology benchmark.

To run this project you will need the following:
* A Linux machine.
* [Virtuoso Jena Provider](https://github.com/srdc/virt-jena):
    1. Clone the virt-jena repository inside the benchmark directory.
    2. Inside the new virt-jena directory do `mvn clean install`
* [Virtuoso](https://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/)
    1. Create a directory in the root of the bioont repository to store of Virtuoso database, e.g. `virt_database`
    2. Change the `virtuoso.ini` parameters according to your machine requirements and put the file in your Virtuoso database directory.
    3. Start the Virtuoso server in your database directory.
    4. Edit the [scripts/bulk_load.sh](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/scripts/bulk_load.sh) script and change the first four parameters to correspond to your Virtuoso server port, user, password and the directory of the Virtuoso database (e.g `VIRT_DB=$PWD/virt_database`).
    5. In the root directory of the repository, bulk load the ontologies into Virtuoso with `scripts/bulk_load.sh`. 
    
    * To restart the Virtuoso store, stop virtuoso and delete everything inside `virt_database` except for the `virtuoso.ini` file. Restart virtuoso and run [scripts/bulk_load.sh](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/scripts/bulk_load.sh) again.
* [Solr](http://lucene.apache.org/solr/) - the use of OLS-SOLR spring boot application is advised for optimal compatibility (https://github.com/EBISPOT/OLS/tree/master/ols-apps/ols-solr-app). Follow these steps:
    1. Clone/download the [OLS git repository](https://github.com/EBISPOT/OLS) into the bioont repository.
    2. Delete the contents of the [resources directory](https://github.com/EBISPOT/OLS/tree/master/ols-apps/ols-solr-app/src/main/resources).
    3. Copy all contents of the [userinput/ontology_property_files](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/ontology_properties_files) directory into the [resources directory](https://github.com/EBISPOT/OLS/tree/master/ols-apps/ols-solr-app/src/main/resources).
    4. Build OLS by running `mvn clean package` in the root of the OLS repositorty.
    5. Download and extract Solr (only [version 5.2.1](http://archive.apache.org/dist/lucene/solr/5.2.1/) was tested) to the root of the bioont repository.
    6. Create a directory to store the Solr indexes in the root of the bioont repository, e.g. `solr_index`
    7. Start solr with:
   
    `$ solr-5.2.1/bin/solr -Dsolr.solr.home=$PWD/OLS/ols-solr/src/main/solr-5-config -Dsolr.data.dir=$PWD/solr_index`
    
    8. Build the Solr indexes from the root of the bioont repository with:
    
    `$ scripts/index.sh`  
    
    * To restart the Solr indexes, stop Solr, delete everything inside the `solr_index` directory and run step (vii) again.

    
## Running the benchmark
Keep Virtuoso and Solr running. Open the file [userinput/config.properties](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/config.properties) and change the necessary parameters. Note that you will need to register in [BioPortal](https://bioportal.bioontology.org/) to obtain an [API key](https://bioportal.bioontology.org/help#Getting_an_API_key).

To run the benchmark do the following:

1. In the [benchmark](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/benchmark) directory build the project with `mvn clean package`.
2. Run the benchmark with `java -jar benchmark/target/bioont-1.0-SNAPSHOT-shaded.jar`
3. View the results in the [userinput/ranking_results](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/ranking_results) and [userinput/evaluation](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/evaluation) folders.

* To restart the benchmark, delete the [userinput/ranking_models](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/ranking_models) and run step (2) again.

# Customising input data
If you wish to use the benchmark with a different set of ontologies you will need to create new [ontology configuration files](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/ontology_properties_files) with the exact some structure and repeat the Solr steps starting from (iii). You will also need to add the acronym for those new ontologies in [userinput/acronyms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/acronyms.txt) and the URL for their download in [userinput/uris.txt](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/uris.txt).

To change the query terms used in the benchmark edit the file [userinput/test_terms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/test_terms.txt) and introduce one query term per line.
    

 






