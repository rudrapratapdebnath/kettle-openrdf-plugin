/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2013 Andre Oosthuizen (South Africa)
 */
package com.google.code.kettle.openrdf.di;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Maintain step settings
 * Validate step settings
 * Serialize step settings
 * Provide access to step classes
 * Perform row layout changes
 * 
 * @author Andre Oosthuizen
 * 
 */
public class OpenRDFStepMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 * The PKG member is used when looking up internationalized strings. The properties file with localized keys is expected to reside in {the package of the class specified}/messages/messages_{locale}.properties
	 */
	private static Class<?> PKG = OpenRDFStepMeta.class; // for i18n purposes

	private String repositoryURL;
	private String sparql;

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public OpenRDFStepMeta() {
		super();
	}

	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step. A standard implementation passing the arguments to the constructor of the step dialog is recommended.
	 * 
	 * @param shell
	 *            an SWT Shell
	 * @param meta
	 *            description of the step
	 * @param transMeta
	 *            description of the the transformation
	 * @param name
	 *            the name of the step
	 * @return new instance of a dialog for this step
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new OpenRDFStepDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. A standard implementation passing the arguments to the constructor of the step class is recommended.
	 * 
	 * @param stepMeta
	 *            description of the step
	 * @param stepDataInterface
	 *            instance of a step data class
	 * @param cnr
	 *            copy number
	 * @param transMeta
	 *            description of the transformation
	 * @param disp
	 *            runtime implementation of the transformation
	 * @return the new instance of a step implementation
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new OpenRDFStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new OpenRDFStepData();
	}

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration to sensible defaults. The values set here will be used by Spoon when a new step is created.
	 */
	public void setDefault() {
		repositoryURL = "http://localhost:8080/openrdf-sesame/repositories/SYSTEM";
		sparql = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX sys:<http://www.openrdf.org/config/repository#>\nSELECT ?repositoryID\nWHERE {\n      ?repository sys:repositoryID ?repositoryID .\n}\nORDER BY ASC(?repositoryID)";
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this step meta object. Be sure to create proper deep copies if the step configuration is stored in modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating a deep copy.
	 * 
	 * @return a deep copy of this
	 */
	public Object clone() {
		Object retval = super.clone();
		return retval;
	}

	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected return value is an XML fragment consisting of one or more XML tags.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {
		StringBuffer xml = new StringBuffer();
		xml.append(XMLHandler.addTagValue("repositoryURL", repositoryURL));
		xml.append(XMLHandler.addTagValue("sparql", sparql));
		return xml.toString();
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the XML node passed in.
	 * 
	 * @param stepnode
	 *            the XML node containing the configuration
	 * @param databases
	 *            the databases available in the transformation
	 * @param counters
	 *            the counters available in the transformation
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		try {
			setRepositoryURL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "repositoryURL")));
			setSparql(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "sparql")));
		} catch (Exception e) {
			throw new KettleXMLException("OpenRDF plugin unable to read step info from XML node", e);
		}

	}

	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to a repository. The repository implementation provides the necessary methods to save the step attributes.
	 * 
	 * @param rep
	 *            the repository to save to
	 * @param id_transformation
	 *            the id to use for the transformation when saving
	 * @param id_step
	 *            the id to use for the step when saving
	 */
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "repositoryURL", repositoryURL); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "sparql", sparql); //$NON-NLS-1$
		} catch (Exception e) {
			throw new KettleException("Unable to save step into repository: " + id_step, e);
		}
	}

	/**
	 * This method is called by PDI when a step needs to read its configuration from a repository. The repository implementation provides the necessary methods to read the step attributes.
	 * 
	 * @param rep
	 *            the repository to read from
	 * @param id_step
	 *            the id of the step being read
	 * @param databases
	 *            the databases available in the transformation
	 * @param counters
	 *            the counters available in the transformation
	 */
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {
			repositoryURL = rep.getStepAttributeString(id_step, "repositoryURL"); //$NON-NLS-1$
			sparql = rep.getStepAttributeString(id_step, "sparql"); //$NON-NLS-1$
		} catch (Exception e) {
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering the step. 
	 * This method must apply any changes the step makes to the row stream. Usually a step adds fields to the row-stream.
	 * 
	 * @param r
	 *            the row structure coming in to the step
	 * @param origin
	 *            the name of the step making the changes
	 * @param info
	 *            row structures of any info steps coming in
	 * @param nextStep
	 *            the description of a step this step is passing rows to
	 * @param space
	 *            the variable space for resolving variables
	 */
	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {
		OpenRDFStepData data = new OpenRDFStepData();
		try {
			data.connect(getRepositoryURL());
			data.runQuery(getSparql());
			List<String> fields = data.getTupleQueryResult().getBindingNames();
			if (fields != null) {
				for (String field : fields) {
					ValueMetaInterface valueMeta = new ValueMeta();
					valueMeta.setName(field);
					valueMeta.setType(ValueMeta.TYPE_STRING);
					valueMeta.setTrimType(ValueMeta.TRIM_TYPE_BOTH);
					valueMeta.setOrigin(origin);
					r.addValueMeta(valueMeta);
				}
			}
		} catch (RepositoryException | QueryEvaluationException | MalformedQueryException e) {
			logError("Unable to get openRDF step fields", e);
		} finally {
			data.disconnect();
		}
	}

	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok. The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include: - verify that all mandatory configuration is given - verify that the step receives any input, unless it's a row generating step - verify that the step does not receive any input if it does not take them into account - verify that the step finds fields it relies on in the row-stream
	 * 
	 * @param remarks
	 *            the list of remarks to append to
	 * @param transmeta
	 *            the description of the transformation
	 * @param stepMeta
	 *            the description of the step
	 * @param prev
	 *            the structure of the incoming row-stream
	 * @param input
	 *            names of steps sending input to the step
	 * @param output
	 *            names of steps this step is sending output to
	 * @param info
	 *            fields coming in from info steps
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		CheckResult cr;
		// No input steps allowed to lead into this step (for now at least).
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "OpenRDF.CheckResult.ReceivingRows.ERROR"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "OpenRDF.CheckResult.ReceivingRows.OK"), stepMeta);
			remarks.add(cr);
		}
	}
	
	public String getRepositoryURL() {
		return repositoryURL;
	}

	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}
	
	public String getSparql() {
		return sparql;
	}

	public void setSparql(String sparql) {
		this.sparql = sparql;
	}
	
}
