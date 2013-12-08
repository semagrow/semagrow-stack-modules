/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.semagrow.stack.modules.utils.federationWrapper.impl;

import eu.semagrow.stack.modules.utils.QueryTranformation;
import eu.semagrow.stack.modules.utils.ReactivityParameters;
import eu.semagrow.stack.modules.utils.endpoint.SPARQLEndpoint;
import eu.semagrow.stack.modules.utils.federationWrapper.FederationEndpointWrapperComponent;
import eu.semagrow.stack.modules.utils.queryDecomposition.DistributedExecutionPlan;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.algebra.evaluation.federation.FederatedService;
import org.openrdf.query.algebra.evaluation.federation.SPARQLFederatedService;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 *
 * @author ggianna
 */
public class FederationEndpointWrapperComponentImpl implements 
        FederationEndpointWrapperComponent {

    SPARQLEndpoint caller;
    String query;
    Iterator<DistributedExecutionPlan> possiblePlans;
    QueryTranformation transformationService;
    HTTPRepository federation;

    public FederationEndpointWrapperComponentImpl(QueryTranformation 
            transformationService, HTTPRepository federation) 
    {
        this.transformationService = transformationService;
        this.federation = federation;
    }

    
    public void executeDistributedQuery(SPARQLEndpoint caller, String query, 
            UUID queryID,
            Iterator<DistributedExecutionPlan> possiblePlans, 
            ReactivityParameters rpParams) {
        this.caller = caller;
        this.query = query;
        this.possiblePlans = possiblePlans;
        
        try {
            // Initialize connection
            federation.initialize();
            RepositoryConnection rc = federation.getConnection();
            
            // Perform query
            TupleQuery q = rc.prepareTupleQuery(QueryLanguage.SPARQL, query);
            // Set the time, taking into account the time limitations
            q.setMaxQueryTime(rpParams.getMaximumResponseTime());
            
            TupleQueryResult t = q.evaluate();
            // Shutdown
            federation.shutDown();
            
            // Render results
            caller.renderResults(queryID, t);
        } catch (RepositoryException ex) {
            Logger.getLogger(
                    FederationEndpointWrapperComponentImpl.class.getName()).log(
                            Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(
                    FederationEndpointWrapperComponentImpl.class.getName()).log(
                            Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            // TODO: Check for time limit to ask for next plan
            Logger.getLogger(FederationEndpointWrapperComponentImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setTransformationService(QueryTranformation transformer) {
        transformationService = transformer;
    }
    
}