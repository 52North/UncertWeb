###############################
# UncertWeb GML Java API
###############################

####################################
##Project Structure
The project contains the following folders:

(i) src: contains the Java classes of the implementation of the O&M profile

 

####################################
##Required Software

Following Software needs to be installed:

-Eclipse (www.eclipse.org) or similar IDE
-Maven http://maven.apache.org/

Optional:

-M2Eclipse http://maven.apache.org/eclipse-plugin.html


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

###################################
## Revision History
0.0.8 - updated dependency to GML API to 0.0.5
0.0.9 - updated dependency to GML API to 0.0.6 (due to schema change in OM profile) and added CategoryObservationCollection