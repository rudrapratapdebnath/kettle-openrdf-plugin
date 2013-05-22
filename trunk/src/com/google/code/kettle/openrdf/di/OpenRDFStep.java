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

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Instances of this class do the actual row processing when the transformation runs.
 * Each thread of execution is represented by an instance of this class.
 * It is given instances of the data and meta classes, when executed.
 * 
 * @author Andre Oosthuizen
 *
 */
public class OpenRDFStep extends BaseStep implements StepInterface {

	public OpenRDFStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It should initialize required for step execution. 
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a database,
	 * as well as obtaining resources, like file handles.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		OpenRDFStepMeta meta = (OpenRDFStepMeta) smi;
		OpenRDFStepData data = (OpenRDFStepData) sdi;
		try {
			data.connect(meta.getRepositoryURL());
		} catch (RepositoryException e) {
			logError("Unable to initialise openRDF step ", e);
			return false;
		}
		return super.init(meta, data);
	}	

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on 
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		OpenRDFStepMeta meta = (OpenRDFStepMeta) smi;
		OpenRDFStepData data = (OpenRDFStepData) sdi;
		//This step generates rows
		try {
			String sqparql = environmentSubstitute(meta.getSparql());
			data.runQuery(sqparql);
			TupleQueryResult tupleQueryResult = data.getTupleQueryResult();
			if (tupleQueryResult.hasNext()) {
				List<String> fields = tupleQueryResult.getBindingNames();
				while (tupleQueryResult.hasNext()) {
					BindingSet bindingSet = tupleQueryResult.next();
					Object[] outputRow = RowDataUtil.allocateRowData(fields.size());
					RowMetaInterface outputRowMeta = new RowMeta();
					for (int i=0; i<fields.size(); i++) {
						String field = fields.get(i);
						outputRow[i] = bindingSet.getValue(field).stringValue();
						outputRowMeta.addValueMeta(i, new ValueMeta(field, ValueMeta.TYPE_STRING));
					}
					putRow(outputRowMeta, outputRow);
				}
			}
		} catch (MalformedQueryException | QueryEvaluationException | RepositoryException e) {
			throw new KettleException(e);
		}
		//Indicate step is finished and processRow() should not be called again
		setOutputDone();
		return false;
	}

	/**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		OpenRDFStepMeta meta = (OpenRDFStepMeta) smi;
		OpenRDFStepData data = (OpenRDFStepData) sdi;
		data.disconnect();
		super.dispose(meta, data);
	}
	
}
