package eu.semagrow.stack.modules.utils.querytransformation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.openrdf.model.URI;

import eu.semagrow.stack.modules.utils.resourceselector.EquivalentURI;

/**
 * {@todo Descrption}
 * 
 * @author Antonis Kukurikos
 */

public interface QueryTranformation {

	/**
	 * @return A list of equivalent URIs aligned with a certain confidence with the initial URI and belonging to a specific schema
	 */
	public List<EquivalentURI> retrieveEquivalentPatterns(URI schema)
			throws IOException, ClassNotFoundException, SQLException;

}