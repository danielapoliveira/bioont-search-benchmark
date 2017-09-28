# bioont-search-benchmark
This is a Maven project that contains source code in Java and ground truth data for a biomedical ontology benchmark.

To run this project you will need the following:
* A Linux machine (these directions were written and tested in Linux).
* [Virtuoso Jena Provider](https://github.com/srdc/virt-jena):
    1. Clone the virt-jena repository inside the benchmark directory.
    2. Inside the new virt-jena directory do `mvn clean install`
* [Virtuoso](https://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/)
    1. Change the virtuoso.ini parameters according to your machine requirements.
    2. Start the Virtuoso server in the same directory as the .ini file.
* [Solr](http://lucene.apache.org/solr/) - the use of OLS-SOLR spring boot application is advised for optimal compatibility (https://github.com/EBISPOT/OLS/tree/master/ols-apps/ols-solr-app). Follow these steps:
    1. Clone/download the [OLS git repository](https://github.com/EBISPOT/OLS).
    2. Delete the contents of the [resources directory](https://github.com/EBISPOT/OLS/tree/master/ols-apps/ols-solr-app/src/main/resources).
    3. Copy all contents of the [ontology_property_files](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/ontology_property_files) directory into the [resources directory](https://github.com/EBISPOT/OLS/tree/master/ols-apps/ols-solr-app/src/main/resources).
    4. Build OLS by running `mvn clean package` in the root of the OLS repositorty.
    5. Download and extract Solr (only [version 5.2.1](http://archive.apache.org/dist/lucene/solr/5.2.1/) was tested) to the root of the bioont repository.
    6. Create a directory to store the Solr indexes in the root of the bioont repository, e.g. `solr_index`
    7. Start solr with:
   
    `$ solr-5.2.1/bin/solr -Dsolr.solr.home=$PWD/OLS/ols-solr/src/main/solr-5-config -Dsolr.data.dir=$PWD/solr_index`
    
    8. Build the Solr indexes from the root of the bioont repository with:
    
    `$ scripts/index.sh`    
    
Start Virtuoso and keep Solr running. Open the file [userinput/config.properties](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/config.properties) and change the necessary parameters. Note that you will need to register in [BioPortal](https://bioportal.bioontology.org/) to obtain an [API key](https://bioportal.bioontology.org/help#Getting_an_API_key). 

Also edit the [run-benchmark.sh](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/script/run-benchmark.sh) script and change the first three parameters to correspond to your Virtuoso server port, user and password.

Then go to the benchmark directory and build with `mvn clean package`.

 Finally, run the benchmark script, in the root of the project, with:

     $ script/run-benchmark.sh

The results will be saved in different directories of userinput.

# Customising input data
If you wish to use the code with a customised set of ontologies you will need to create new [ontology configuration files](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/ontology_properties_files) and repeat the Solr steps starting from (iii). You will also need to add the acronym for those ontologies in [userinput/acronyms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/acronyms.txt) and the URL for their download in [userinput/uris.txt](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/uris.txt).

To change the query terms used in the benchmark edit the file [userinput/test_terms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/test_terms.txt) and introduce one query term per line.
    

 






