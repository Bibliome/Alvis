
# The Alvis suite

The Alvis suite is a Text Mining and knowledge exploration suite, in use in scientific laboratories, which is
​
- modular: the Alvis suite is composed of:
​
     - _AlvisAE_: an annotation editor [git repo](https://github.com/bibliome/Alvis/) ![AlvisAE screenshot]()
​
     - _AlvisNLP_: a text-mining workflow engine [git repo](https://github.com/bibliome/AlvisNLP/) ![AlvisNLP diagram]()
​
     - _AlvisIR_: a search engine interface enabling to explore text-mining results [git repo](https://github.com/bibliome/AlvisIR/) ![AlvisIR screenshot]()
​
- easily extendable
- designed for entity and relationships extraction
​
With Alvis, one can:
​
- train an information extraction model (supervised learning based on manual annotation)
​
- extract entities and relationships
​
- query a corpus of documents and get results enhanced by text-mining pre-processing (parent-child inference, ontology-based facets, proof of matching)
​
It is built in Java and Google Web Toolkit, with a focus on user experience, both for the command-line tools and web interfaces.
​
Alvis is developped by the [Bibliome team](http://maiage.jouy.inra.fr/?q=fr/bibliome/) at the French National Institute for Agriculture Research (INRA).
​
## Stage of development
​
The Alvis suite is in beta stage but stable enough to be used in production.
​
Planned developments come within the H2020 [OpenMinTeD](https://openminted.eu) project.
​
The Alvis suite will be integrated to the more exhaustive OpenMinTeD plateform (see below). On the roadmap is the tighter integration of the AlvisNLP text-mining super-module with the AlvisAE as well as packaging of the Alvis suite as components of the OpenMinTeD plateform. 

