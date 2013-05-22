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

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * The data class is used for storing data unique to a thread of execution, when the plugin runs.
 * This is where database connections, file handles, caches and other things needed during execution are stored.
 * 
 * @author Andre Oosthuizen
 *
 */
public class OpenRDFStepData extends BaseStepData implements StepDataInterface {

	private Repository repository;
	private RepositoryConnection connection;
	private TupleQueryResult tupleQueryResult;

	public OpenRDFStepData() {

	}

	public void connect(String repositoryURL) throws RepositoryException {
		try {
			this.repository = new HTTPRepository(repositoryURL);
			this.repository.initialize();
			this.connection = this.repository.getConnection();
		} catch (RuntimeException e) {
			throw new RepositoryException(e);
		}
	}
	
	public void disconnect() {
		try {
			this.tupleQueryResult.close();
			this.connection.close();
			this.repository.shutDown();
		} catch (Throwable ignore) {
			
		}
	}
	
	public void runQuery(String sparql) throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
		this.tupleQueryResult = tupleQuery.evaluate();
	}
	
	public TupleQueryResult getTupleQueryResult() {
		return tupleQueryResult;
	}

}
