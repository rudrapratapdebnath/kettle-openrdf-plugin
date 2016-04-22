# Pre-requisites #

openRDF for PDI has the following dependencies
  * [JDK 1.5](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or above.
  * [PDI version 4.x](http://sourceforge.net/projects/pentaho/files/Data%20Integration/) or later release.
  * [openRDF Sesame 2.x](http://www.openrdf.org/download.jsp) release

# Download/Unzip #

[Download the latest openRDF](https://code.google.com/p/kettle-openrdf-plugin/downloads/list) for PDI release.

Unzip the archive to ${PDI\_HOME}/plugins/steps. This should create a folder named **OpenRDFInput** under ${PDI\_HOME}/plugins/steps.

Add all the sesame-xxx.jar files in the openRDF Sesame lib folder to ${PDI\_HOME}/libext

# Verify installation #

Start or restart the PDI Spoon UI (${PDI\_HOME}/spoon.sh or ${PDI\_HOME}/spoon.bat).

Create a new PDI Transformation. You should now have a design tab with all the available steps.

Open the Big Data category and drag/drop the **openRDF Input** step onto the canvas.

Congratulations, openRDF for PDI is now installed. You can now start building your first transformation.