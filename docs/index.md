[The Alvis Suite](#the-alvis-suite) [OpenMinTeD](#openminted) [Job offer (we want you!)](#job-offer)

<!--
pointeurs propres vers les répos
et démos

jolis screenshots de AlvisX 

lien loi République numérique
lien UK

logo bibliome
logo INRA
logo openminted
logo Alvis
-->


# The Alvis suite

The Alvis suite is a Text Mining and knowledge exploration suite, in use in scientific laboratories, which is

- modular: the Alvis suite is composed of:

     - _AlvisAE_: an annotation editor [git repo](https://github.com/bibliome/Alvis/) ![AlvisAE screenshot]()

     - _AlvisNLP_: a text-mining workflow engine [git repo](https://github.com/bibliome/AlvisNLP/) ![AlvisNLP diagram]()

     - _AlvisIR_: a search engine interface enabling to explore text-mining results [git repo](https://github.com/bibliome/AlvisIR/) ![AlvisIR screenshot]()

- easily extendable
- designed for entity and relationships extraction

With Alvis, one can:

- train an information extraction model (supervised learning based on manual annotation)

- extract entities and relationships

- query a corpus of documents and get results enhanced by text-mining pre-processing (parent-child inference, ontology-based facets, proof of matching)

It is built in Java and Google Web Toolkit, with a focus on user experience, both for the command-line tools and web interfaces.

Alvis is developped by the [Bibliome team](http://maiage.jouy.inra.fr/?q=fr/bibliome/) at the French National Institute for Agriculture Research (INRA).

## Stage of development

The Alvis suite is in beta stage but stable enough to be used in production.

Planned developments come within the H2020 [OpenMinTeD](https://openminted.eu) project.

The Alvis suite will be integrated to the more exhaustive OpenMinTeD plateform (see below). On the roadmap is the tighter integration of the AlvisNLP text-mining super-module with the AlvisAE as well as packaging of the Alvis suite as components of the OpenMinTeD plateform. 

## OpenMinTeD

The [OpenMinTeD](https://openminted.eu) project is a EU H2020 project that provides a text-mining plateform to non-experts to allow both easy use of text-mining methods and increased exploitation of results obtained by text mining.

Several teams from several european countries united around this project and the available expertise range from computer science (text-mining, database management, indexing) to computational biology (bioinformatics), to publishing (academic librarians), to humanities (social sciences, digital humanities).

The workflow engine that was choosen is [Galaxy](http://galaxyproject.org/). Current work under progress includes the integration of existing text-mining programs as Galaxy components, extension of the Galaxy tool sheds to meet OpenMinTeD expectations and integration of the plateforme inputs and outputs to existing domain-specific applications (bioinformatics, life sciences, social sciences).


## Text mining

Text mining is a mature discipline which empowers computer scientists to extract information from heterogeneous and unstructured resource. Current developments make use of Machine Learning algorithms to automatise the Information Extraction (IE) process, while some make use of ontologies to bridge the gap between unstructured text mining results and ontologies.


## Text-mining on scientific publications is legal in France since 2016

[Since October 2016](https://via.hypothes.is/http://www.enseignementsup-recherche.gouv.fr/cid107077/les-chercheurs-francais-pourront-desormais-pratiquer-pleinement-la-fouille-de-texte-et-de-donnees.html), when the "Loi pour une République numérique" was passed, French researchers can legally do text mining on scientific, publications. It is also the case in Great Britain [since 2014](https://scinfolex.com/2014/04/01/le-royaume-uni-sanctuarise-les-pratiques-de-data-mining-par-le-biais-dune-exception-au-droit-dauteur/) and in the United States [since 2015](https://scinfolex.com/2015/10/21/comment-laffaire-google-books-se-termine-en-victoire-pour-le-text-mining/). Text-mining has been mature for years, so the development of the OpenMinTeD plateforme is intended to make text-mining available to non-programmers and bridge the gap between what is considered possible and one researcher can actually in her daily activity achieve.

<!--


Recherche d'un dev :

- Java : il y a du code Java
- du design (légèrement mais être futé est attendu), au dev (fusion de deux suites), intégration au niveau de la plateforme OpenMinTeD (Galaxy), un petit peu de production
- objectif qualité : tests, intégration continue

Deux missions :
- intégrer AlvisAE et AlvisNLP
- intégrer la suite Alvis à la plateforme OpenMinTeD (basée sur le moteur de workflows Galaxy)

Proximité (discussions, séminaires, éventuellement contribution à) : Text Mining, Workflows, Machine Learning, promotion de l'Open Access en sciences
-->

## Job offer

An engineer position is available for an enthusiastic software developer at the Bibliome group, a bio text-mining team in the French national institute for agriculture and food research institute. The selected candidate will work on the [OpenMinTeD](https://openminted.eu) project (EC/H2020 EINFRA 654021), which aims at building a text-mining service for researchers. She will undertake the integration of the tools of the Alvis suite developed by [Bibliome](http://maiage.jouy.inra.fr/?q=fr/bibliome/), and participate in the development of the OpenMinTeD platform. The candidate will work in cooperation with Bibliome team members, as well as project partners all over Europe (Greece, UK, Germany).
 
Location: [Jouy-en-Josas](http://www.openstreetmap.org/search?query=jouy%20en%20josas%20Domaine%20de%20Vilvert), France (15km from Paris)

 
Period: 12 months starting May or June 2017 (possible extension)

 
Remuneration: The remuneration ranges from 2000 to 3000 €/month according to qualification and experience. The benefit package includes subsidy for local transport and lunch, health care, and retirement insurance.

 
Degrees and skills:

-    Master degree in computer science, or engineering degree in computer programming

 -    Excellent programming skills in Java, JEE

  -   Strong familiarity with development tools:  a standard Java IDE, git, maven

   -   Familiarity with the following technologies are a plus: docker, UIMA, XML technologies, Galaxy

  -    Good technical English communication skills

 -  Interest in text-mining and natural language processing

 -  Strong teamwork ability


Outside of the mission, the developer will have the opportunity to be close to #MachineLearning, #Ontologies, #SearchEngineOptimisation, #OpenAccess, #OpenScience



<script async defer src="https://hypothes.is/embed.js"></script>

