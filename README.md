# The Pollution Traffic Weather Project

##About

The Pollution Traffic Weather Project (visit our homepage and try the service at [http://fuseki.fone.no:3030/landing.html](http://fuseki.fone.no:3030/landing.html)) is an academic non-profit project based on data with open licensing (CC BY 3.0) from The Norwegian Public Roads Administration, The Norwegian Meteorological Institute, and The Norwegian Institute for Air Research. We have used the Apache Jena framework for Java to lift data about weather, air pollution and traffic in Bergen, Norway from CSV/xls tables to RDF graphs. Around these RDF graphs we have created a website and SPARQL endpoint.


## Getting Started

To get a copy of our dataset up and running you need to download and install the Apache Jena Fuseki server from the [official site](https://jena.apache.org/download/) 
We used version 2.6.0.
Once installed and up and running you need to use the tdbloader to restore our dataset (“ptwp_2017-05-19_22-07-41.nq.gz” or “inf_ptw_2017-05-19_22-05-43.nq.gz”, further information in the DATASETS section) onto your Fuseki server. The tdbloader application is found in the Apache Jena package that you can download from the link above. Version 3.3.0 at the time of writing.

If you wish to use our template for the webpage, you can use either the built-in webserver in the fuseki(just dump the files from the Webpage folder into the webapp folder of your Fuseki server and you are good to go. The included sgvizler.js file is modified from the original to remove some settings - you can download the latest version from the [sgvizler homepage](http://mgskjaeveland.github.io/sgvizler/). 

If using our template, please remember to update the SPARQL-endpoint URL in all the .html files to fit your Fuseki server URL.

Change the URL to fit your needs: 
```
.defaultEndpointURL("http://YOURSERVER.COM:3030/DATASET/SERVICE")
```

###Datasets

ptwp_2017-05-19_22-07-41.nq.gz - this is our main dataset. It contains all our data separated into 4 graphs as follows:

1. default: 147 triples (Ontology)

2. data:TrafficData: 4039956 triples

3. data:PollutionData: 1338946 triples

4. data:WeatherData: 444548 triples

inf_ptw_2017-05-19_22-05-43.nq.gz - this is a sample database with inference engine turned on, containing data from only measuring point for each type of data limited to 1 month (May of 2016). In total 149015 triples. Due to the heavy load inference/reasoning creates on the servers, we had to limit this to 1 month only for demonstration purposes.

### Usage Example
Here is an example of the results of a SPARQL query visualized in sgvizler:

PREFIXes are already defined on the endpoint, so they are not included here.
```
SELECT ?time ?cars
WHERE { GRAPH <http://fuseki.fone.no:3030/ptwp/data/TrafficData> { 
?subject a :TrafficMeasurement .
               	?subject :dateTime ?time .
               	?subject :numOfVehicles ?cars .
}
}LIMIT 100000
```

![sgvizler_example.png](https://bitbucket.org/repo/nqRqLa/images/504347541-sgvizler_example.png)

### Prerequisites

If you want to use this dataset in a live production environment, make sure you have sufficiently powerful hardware. Also, if running in a live environment you should consider using Apache2 as a host for you webpages instead of the built-in webserver in the Fuseki application. Also make sure to have a properly configured .htaccess file to restrict write access to the public, or use the included Apache Shiro configuration file (shiro.ini) if you wish to use Apache Shiro for security.


## Built With

* [Apache Jena framework](https://jena.apache.org/download/index.cgi)

* [Apache Fuseki server](https://jena.apache.org/documentation/serving_data/) for hosting our dataset and website 

* [Sgvizler library](https://mgskjaeveland.github.io/sgvizler/), for visualizing our semantic data

* [Eclipse Neon](https://eclipse.org/neon/), for Java development

* [Protege](http://protege.stanford.edu/) for ontology development

## Authors

Candidates 104, 109, and 121

## License

This project is licensed under the CC BY 3.0 License - see [CreativeCommons 3.0 Attribution Unported](https://creativecommons.org/licenses/by/3.0/legalcode) for more details and the full license.