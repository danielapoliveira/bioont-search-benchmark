# bioont-search-benchmark
This is a Maven project and contains source code in Java and ground truth data for a biomedical ontology search.

To run this project you will need to download and install the following:
* A Linux machine (these directions were written to work with Linux and the code has only been tested in this OS).
* [Virtuoso Jena Provider](https://github.com/srdc/virt-jena):
    1. Clone the virt-jena repository inside the benchmark directory.
    2. Inside the virt-jena new directory do `mvn clean install`
* [Virtuoso](https://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/)
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

If you wish to use the code with a customised set of ontologies you will need to create new [ontology configuration files](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/ontology_properties_files) and repeat the Solr steps starting from (iii). You will also need to add the acronym for those ontologies in [userinput/acronyms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/acronyms.txt) and the URL for their download in [userinput/uris.txt](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/uris.txt).

To change the query terms used in the benchmark edit the file [userinput/test_terms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/test_terms.txt) and introduce one query term per line.

Finally, go to the benchmark directory and build with `mvn clean package`.
    
Then run the code from the root of the bioont repository with:

    $ java -jar benchmark/target/bioont-1.0-SNAPSHOT-shaded.jar

The results will be saved in different directories of userinput.

## Bulk Loading Ontologies

The previous method has the disadvantage of not being able to load large ontologies into the local Virtuoso so an alternate method is provided in the directory [scripts](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/scripts). The script downloads the ontologies and bulk loads them via iSQL to the Virtuoso server. 

The pre-requisites are the same: you should install a Solr server and Virtuoso and they have to be running. Follow the remaining steps, but modify the [Configuration](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/config.properties) so that the variable `ontologies.load` is `false` and, in the root of the project, do:

    $ mvn clean
    $ mvn package
    
Then edit the [run-benchmark.sh](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/script/run-benchmark.sh) script and change the first three parameters to correspond to your Virtuoso server port, user and password.
 
 Finally, run the benchmark script, in the root of the project, with:

    $ script/run-benchmark.sh





