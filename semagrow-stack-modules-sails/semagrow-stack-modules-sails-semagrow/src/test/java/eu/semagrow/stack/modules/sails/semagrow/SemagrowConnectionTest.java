package eu.semagrow.stack.modules.sails.semagrow;

import eu.semagrow.stack.modules.sails.config.VOIDInferencerConfig;
import eu.semagrow.stack.modules.sails.semagrow.config.SemagrowSailConfig;
import eu.semagrow.stack.modules.sails.semagrow.config.SemagrowRepositoryConfig;
import eu.semagrow.stack.modules.api.query.SemagrowTupleQuery;
import eu.semagrow.stack.modules.vocabulary.VOID;
import info.aduna.iteration.Iterations;
import junit.framework.TestCase;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SemagrowConnectionTest extends TestCase {

    private String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX void: <http://rdfs.org/ns/void#>\n";

    public void testEvaluateInternal() throws Exception {

        String q1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                   "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                   "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "SELECT *  { ?s <http://localhost/my> ?z. " +
                "?z <http://rdf.iit.demokritos.gr/2014/my#pred2> ?y . }" ;


        String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "SELECT *  { { <htt://localhost/sub> <http://localhost/my> ?z. " +
                "?z <http://rdf.iit.demokritos.gr/2014/my#pred2> \"R\" . } UNION " +
                "{ <htt://localhost/sub> <http://localhost/my> ?z.\n" +
                "?z <http://rdf.iit.demokritos.gr/2014/my#pred2> \"R\" . } } LIMIT 10" ;

        SailImplConfig config = new SemagrowSailConfig();

        SemagrowRepositoryConfig repoConfig = new SemagrowRepositoryConfig();
        SemagrowSailRepository repo = (SemagrowSailRepository) RepositoryRegistry.getInstance().get(repoConfig.getType()).getRepository(repoConfig);
        repo.initialize();
        SemagrowSailRepositoryConnection conn = repo.getConnection();
        SemagrowTupleQuery query =  conn.prepareTupleQuery(QueryLanguage.SPARQL, q1);
        query.setIncludeInferred(true);
        query.setIncludeProvenanceData(true);

        TupleQueryResult result = query.evaluate();
        System.out.println(Iterations.toString(result, "\n"));
        Iterations.closeCloseable(result);
    }


    public void testEvaluateInternal2() throws Exception {
    	
    	String q3 = "SELECT * WHERE {"
    			+ " ?document <http://purl.org/ontology/bibo/abstract> ?abstract . "
    			+ " ?document <http://purl.org/dc/terms/title> ?title . "
    			+ "}" ;
    	
    	String q2 = "SELECT * WHERE {"
    			+ " ?document <http://purl.org/ontology/bibo/abstract> ?abstract . "
    			+ " ?document <http://purl.org/dc/terms/title> ?title . "
    			+ " ?document <http://purl.org/dc/terms/creator> ?creator . "
    			+ "}" ;


        String q1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "SELECT *  { ?s ?p ?z. }" ;


        String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "SELECT *  { <htt://localhost/sub> <http://localhost/my> ?z. " +
                "?z <http://rdf.iit.demokritos.gr/2014/my#pred2> \"R\" . } " ;

        String dlo_q = "PREFIX  qb:   <http://purl.org/linked-data/cube#>\n" +
                "SELECT DISTINCT  ?property ?codeList ?var_struct ?shortName ?dimensionSize ?dataType\n" +
                "WHERE\n" +
                "  { <http://semagrow.eu/rdf/data/epic_hadgem2-es_rcp2p6_ssp2_co2_firr_yield_whe_annual_2005_2099_yield> qb:structure ?dstruct .\n" +
                "    ?dstruct qb:component ?var_struct .\n" +
                "    ?var_struct <http://semagrow.eu/rdf/struct/shortName> ?shortName .\n" +
                "    ?var_struct <http://semagrow.eu/rdf/struct/dimensionSize> ?dimensionSize .\n" +
                "    ?var_struct <http://semagrow.eu/rdf/struct/dataType> ?dataType .\n" +
                "    ?var_struct qb:componentProperty ?property .\n" +
                "    ?var_struct qb:codeList ?codeList .\n" +
                "    ?property <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> qb:DimensionProperty\n" +
                "  }";

        String dlo_q2 = "PREFIX  sgstruct: <http://semagrow.eu/rdf/struct/>\n" +
                "PREFIX  qb:   <http://purl.org/linked-data/cube#>\n" +
                "\n" +
                "SELECT DISTINCT  ?short_name ?attr\n" +
                "WHERE\n" +
                "  { sgstruct:epic_hadgem2-es_rcp2p6_ssp2_co2_firr_yield_whe_annual_2005_2099_struct_time qb:component ?var_attribute_struct .\n" +
                "    ?var_attribute_struct qb:componentProperty ?var_attr_name .\n" +
                "    ?var_attribute_struct sgstruct:shortName ?short_name .\n" +
                "sgstruct:epic_hadgem2-es_rcp2p6_ssp2_co2_firr_yield_whe_annual_2005_2099_struct_time ?var_attr_name ?attr\n" +
                "  }";

        String agris = "SELECT ?s WHERE {<http://agris.fao.org/aos/records/AU7500003> <http://purl.org/dc/terms/subject> ?o.\n" +
                " ?s <http://purl.org/dc/terms/subject> ?o.\n" +
                " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdf.iit.demokritos.gr/2015/crawler#doc>}";

        String lifeScience1 = "SELECT ?predicate ?object WHERE {\n" +
                "  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }\n" +
                "  UNION\n" +
                "  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.w3.org/2002/07/owl#sameAs> ?caff .\n" +
                "    ?caff ?predicate ?object . }\n" +
                "}\n";

        String dlo2 = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX  qb:   <http://purl.org/linked-data/cube#>\n" +
                "\n" +
                "SELECT  ?meas_val ?pos_lon ?pos_lat ?pos_time\n" +
                "WHERE\n" +
                "  { ?observation qb:dataSet <http://semagrow.eu/rdf/data/epic_hadgem2-es_rcp2p6_ssp2_co2_firr_yield_whe_annual_2005_2099_yield> .\n" +
                "    ?observation <http://rdf.iit.demokritos.gr/2014/cfconventions#longitude> ?pos_lon .\n" +
                "    ?pos_lon rdf:value ?value_lon\n" +
                "    FILTER ( ( ?value_lon >= \"108.0\"^^xsd:float ) && ( ?value_lon <= \"180.0\"^^xsd:float ) )\n" +
                "    ?observation <http://rdf.iit.demokritos.gr/2014/cfconventions#latitude> ?pos_lat .\n" +
                "    ?pos_lat rdf:value ?value_lat\n" +
                "    FILTER ( ( ?value_lat >= \"-54.0\"^^xsd:float ) && ( ?value_lat <= \"-18.0\"^^xsd:float ) )\n" +
                "    ?observation <http://rdf.iit.demokritos.gr/2014/cfconventions#time> ?pos_time .\n" +
                "    ?pos_time rdf:value ?value_time\n" +
                "    FILTER ( ( ?value_time >= \"120.0\"^^xsd:double ) && ( ?value_time <= \"149.0\"^^xsd:double ) )\n" +
                "    ?observation <http://rdf.iit.demokritos.gr/2014/cfconventions#yield> ?meas_val\n" +
                "  }";

        String lifeScience7 = "SELECT $drug $transform $mass WHERE {  \n" +
                "  { $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/affectedOrganism>  'Humans and other mammals'.\n" +
                "    $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> $cas .\n" +
                "    $keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> $cas .\n" +
                "    $keggDrug <http://bio2rdf.org/ns/bio2rdf#mass> $mass\n" +
                "      FILTER ( $mass > '5' )\n" +
                "  } \n" +
                "  OPTIONAL { $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/biotransformation> $transform . } \n" +
                "}\n";

        //Thread.sleep(10000);

        SailImplConfig config = new SemagrowSailConfig();

        SemagrowRepositoryConfig repoConfig = new SemagrowRepositoryConfig();
        SemagrowSailRepository repo = (SemagrowSailRepository) RepositoryRegistry.getInstance().get(repoConfig.getType()).getRepository(repoConfig);
        repo.initialize();
        SemagrowSailRepositoryConnection conn = repo.getConnection();
        SemagrowTupleQuery query =  conn.prepareTupleQuery(QueryLanguage.SPARQL, lifeScience7);
        query.setIncludeInferred(true);
        query.setIncludeProvenanceData(true);
        //TupleQueryResult result = query.evaluate();
        //System.out.println(Iterations.toString(result, "\n"));
        //Iterations.closeCloseable(result);

        //System.out.println(query.getDecomposedQuery());

        final CountDownLatch latch = new CountDownLatch(1);



        query.evaluate(new TupleQueryResultHandler() {
            @Override
            public void handleBoolean(boolean b) throws QueryResultHandlerException {

            }

            @Override
            public void handleLinks(List<String> list) throws QueryResultHandlerException {

            }

            @Override
            public void startQueryResult(List<String> list) throws TupleQueryResultHandlerException {

            }

            @Override
            public void endQueryResult() throws TupleQueryResultHandlerException {
                latch.countDown();
            }

            @Override
            public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
                System.out.println(bindingSet);
            }
        });

        latch.await();

    }

    public void testCrossProduct() throws Exception {

        String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "SELECT *  { <htt://localhost/sub> <http://localhost/my> ?z. " +
                "?z2 <http://rdf.iit.demokritos.gr/2014/my#pred2> ?w . } " ;

        SailImplConfig config = new SemagrowSailConfig();

        SemagrowRepositoryConfig repoConfig = new SemagrowRepositoryConfig();
        SemagrowSailRepository repo = (SemagrowSailRepository) RepositoryRegistry.getInstance().get(repoConfig.getType()).getRepository(repoConfig);
        repo.initialize();
        SemagrowSailRepositoryConnection conn = repo.getConnection();
        SemagrowTupleQuery query =  conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
        query.setIncludeInferred(true);
        query.setIncludeProvenanceData(true);

        TupleQueryResult result = query.evaluate();
        System.out.println(Iterations.toString(result, "\n"));
        Iterations.closeCloseable(result);
    }

    public void testVOID() throws Exception {
        String q = PREFIX +
                "SELECT * FROM <http://www.semagrow.eu/metadata> { " +
                "?s <" + VOID.PROPERTY +  "> ?o. }";
        //SailImplConfig config = new SemagrowConfig();

        //SailRepositoryConfig repoConfig = new SailRepositoryConfig(config);
        RepositoryImplConfig repoConfig = new SemagrowRepositoryConfig();
        Repository repo = RepositoryRegistry.getInstance().get(repoConfig.getType()).getRepository(repoConfig);
        //Repository repo = RepositoryRegistry.getInstance().get()
        repo.initialize();
        RepositoryConnection conn = repo.getConnection();
        TupleQuery query =  conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
        query.setIncludeInferred(true);
        query.setBinding("o", ValueFactoryImpl.getInstance().createURI("http://localhost/my"));
        TupleQueryResult result = query.evaluate();

        int i = 0;
        while(result.hasNext()) {
            System.out.println(result.next().toString());
            i++;
        }
        System.out.println(i);
    }

    public void testVOIDInference() throws Exception {
        String q = PREFIX +
                "SELECT DISTINCT ?endpoint { " +
                "?s void:sparqlEndpoint ?endpoint . " +
                " }";
        SailImplConfig config = new
                VOIDInferencerConfig(new ForwardChainingRDFSInferencerConfig(
                new MemoryStoreConfig(false)));

        SailRepositoryConfig repoConfig = new SailRepositoryConfig(config);
        Repository repo = RepositoryRegistry.getInstance().get(repoConfig.getType()).getRepository(repoConfig);
        repo.initialize();
        loadRepo(repo);
        RepositoryConnection conn = repo.getConnection();
        TupleQuery query =  conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
        query.setIncludeInferred(true);
        TupleQueryResult result = query.evaluate();

        int i = 0;
        while(result.hasNext()) {
            System.out.println(result.next().toString());
            i++;
        }
        System.out.println(i);
    }

    private void loadRepo(Repository repo) throws Exception {
        RepositoryConnection conn = repo.getConnection();
        conn.add(new File("/tmp/metadata.ttl"), "file:///tmp/metadata.ttl", RDFFormat.TURTLE);
        conn.commit();
        conn.close();
    }
}