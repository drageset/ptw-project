The Pollution Traffic Weather Project ([visit our homepage!](http://fuseki.fone.no:3030/landing.html)) is an accademic non-proffit project based on data with open licencing ([CC BY 3.0](https://creativecommons.org/licenses/by/3.0/)) from [The Norwegian Public Roads Administration](http://www.vegvesen.no/), [The Norwegian Meteorological Institute](https://www.met.no/), and [The Norwegian Institute for Air Research](http://www.nilu.no/). 

We have used the [Apache Jena framework](https://jena.apache.org/) for Java to lift data about weather, air pollution and traffic in Bergen, Norway from CSV/xls tables to RDF graphs. Around these RDF graphs we have created a website and SPARQL endpoint. As the project was not initially intended to be open sourced, most of the code comments are as of 9th of May 2017 in Norwegian.

Our goal is to make data about air quality accessible and explorable on the internet, mainly making the data easier to comprehend and reflect on for the general populace, but also letting data scientists benefit from the extra power provided by semantic technologies (RDF* and OWL). 

We have parsed and lifted data about measurements of air polutants like NO2, data about the amount of cars passing certain points in the city per hour, and weather measurements like percipitation and wind direction/speed.

With as few modifications and excemptions to the original data as possible, the data is now stored in RDF graphs accessible for query through the [SPARQL endpoint at this site](http://fuseki.fone.no:3030/query.html). For visualising our data ([click here to see some cool graphs](http://fuseki.fone.no:3030/explore.html)) we have chosen the sgvizler visualisation tool, which can be found [here on GitHub](https://mgskjaeveland.github.io/sgvizler/).

We developed our own ontology to describe the data in RDF (that we named "http://www.ptwproject.org/ontology#", which is not a domain we own). We have been using the [Semantic Sensor Network ontology](https://www.w3.org/2005/Incubator/ssn/ssnx/ssn), and the [OWL time ontology](https://www.w3.org/TR/owl-time/).

NB: So far our data consists of a test set, and is restricted to 2016
