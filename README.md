# bioont-search-benchmark
This is a Maven project and contains source code in Java and ground truth data for a biomedical ontology search.

To run this project you will need to download and install the following:
* [Virtuoso Jena Provider](https://github.com/srdc/virt-jena)
* [Solr](http://lucene.apache.org/solr/)
    * The use of OLS-SOLR spring boot application is advised for optimal compatibility (https://github.com/EBISPOT/OLS/tree/master/ols-apps/ols-solr-app).
* [Virtuoso](https://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/)

You will need a version of Solr and Virtuoso running at same time. Then open the file [userinput/config.properties](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/config.properties) and change the parameters. Note that you will need to register in [BioPortal](https://bioportal.bioontology.org/) to obtain an [API key](https://bioportal.bioontology.org/help#Getting_an_API_key).

If you wish to use the code with a customised set of ontologies you will need to create new [ontology configuration files](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/ontology_properties_files) and add the acronym for those ontologies in [userinput/acronyms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput/acronyms.txt). 

To change the query terms used in the benchmark edit the file [userinput/test_terms.txt](https://github.com/danielapoliveira/bioont-search-benchmark/tree/master/userinput/test_terms.txt) and introduce one query term per line.

Finally, to run the benchmark code use the following commands:

    $ mvn clean
    $ mvn package
    $ java -jar target/bioont-1.0-SNAPSHOT-shaded.jar

The results will be saved in different directories of userinput.

## Bulk Loading Ontologies

The previous method has the disadvantage of not being able to load large ontologies into the local Virtuoso so an alternate method is provided in the directory [script](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/script). The script downloads the ontologies and bulk loads them via iSQL to the Virtuoso server. 

This method works with a bash script which means it's only available in Linux machines.

The pre-requisites are the same: you should install a Solr server and Virtuoso and they have to be running. Follow the remaining steps, but before running the benchmark, edit [run-benchmark.sh](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/script/run-benchmark.sh) and change the first three parameters to correspond to your Virtuoso server port, user and password.
To run the benchmark script and code, in the same directory as [userinput](https://github.com/danielapoliveira/bioont-search-benchmark/blob/master/userinput), run:

    $ script/run-benchmark.sh





