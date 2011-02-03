###############################
# UncertWeb Profiles Java API
###############################

####################################
##Project Structure
The project contains the following folders:

(i) om-api: contains the Java Implementation of the O&M profile
(ii) gml-api: contains the Java Implementation for the GML profile
(iii) lib: contains the XmlBeans generated Java Lib used for Xml Encoding/Parsing in the Java implementation + the UncertML Java API
(iv) Util: contains the OGC schemas used in the profiles schema (subfolder 'schemata') as well as the profiles schema (subfolder 'profiles')
 

####################################
##Required Software

Following Software needs to be installed:

-Eclipse (www.eclipse.org) or similar IDE
-Maven http://maven.apache.org/

Optional:

-M2Eclipse http://maven.apache.org/eclipse-plugin.html

####################################
##Setting up the Java project

The following steps have to be done to set up the Java project:

-Navigate to lib folder and then execute the two batch files contained there.
-afterwards, you can add this project as a Java Project in Eclipse
-if you have installed M2Eclipse (Maven Plugin for Eclipse, http://maven.apache.org/eclipse-plugin.html), you can then right click the project,
choose "Maven" and then "Enable Dependency Management". All the libraries the project uses are then added to your Classpath. 

####################################
##Enable schema validation; creating your own examples

In order to validate your documents, you have to use an XmlCatalog. Therefore,
adjust the file 'CustomCatalog.xml' in the folder './Util/Profiles':

-adjust the values of the 'rewritePrefix' attribute in the two 'rewriteSystem' elements to the path of your  local project

How to use XmlCatalog in:

Eclipse: http://www.eclipse.org/webtools/community/tutorials/XMLCatalog/XMLCatalogTutorial.html
XmlSpy: http://books.google.de/books?id=qqzXrOy_ffwC&pg=PA194&lpg=PA194&dq=XmlCatalog+XmlSpy&source=bl&ots=7DaZKiOE19&sig=Ibml2zCGxHg0cUgA3umspBDdXAQ&hl=de&ei=z289TcfhJYL3sgbPyYH0Bg&sa=X&oi=book_result&ct=result&resnum=10&ved=0CHEQ6AEwCQ#v=onepage&q&f=false
Oxygen: http://www.oxygenxml.com/validation.html#xml_catalog

###################################
## Questions? Issues?

You can contact staschc@uni-muenster.de if you have further questions or comments.