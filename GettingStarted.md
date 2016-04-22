# Introduction #
This document guides you through the process of creating a PDI transformation with 2 steps:
  * The openRDF Input step which generates data by executing a SPARQL query on a repository
  * The Excel Output step which will create an excel worksheet with the transformed data


# Create a PDI Transformation #
Start PDI Spoon (${PDI\_HOME}/spoon.sh or ${PDI\_HOME}/spoon.bat).

Drag/Drop the openRDF Input step from the Big Data category and the Excel Output step from the Output category onto the canvas.

Create a hop between these steps. You should now have a screen looking like this:

![http://kettle-openrdf-plugin.googlecode.com/svn/wiki/images/Spoon-003.png](http://kettle-openrdf-plugin.googlecode.com/svn/wiki/images/Spoon-003.png)

# Configure Steps #
Double click on the openRDF Input step (or right click, Edit step from the context menu) to bring up the setup dialog:

![http://kettle-openrdf-plugin.googlecode.com/svn/wiki/images/Spoon-004.png](http://kettle-openrdf-plugin.googlecode.com/svn/wiki/images/Spoon-004.png)

  * The default repository URL connects to the openRDF/Sesame SYSTEM repository on localhost. Click on the Test Connection button to verify the repository is valid
  * The default SPARQL query returns the IDs of all repositories. Click on the Preview button to verify that the query produces output.

Now double-click on the Excel Output step. The only parameter you need to provide is the location to output the Excel worksheet

Save the PDI transformation. Give it any name you like and store it on your file system as a .ktr file or in a PDI repository.

# Run the PDI transformation #
You can run the transformation directly or in debug mode. Finally, open the excel worksheet that was produced. Now you have a working example of a transformation that executes a SPARQL query and turns the result into an excel worksheet.