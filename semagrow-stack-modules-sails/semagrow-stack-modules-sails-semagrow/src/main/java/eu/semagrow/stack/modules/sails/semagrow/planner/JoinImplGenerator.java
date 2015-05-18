package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.Join;

import java.util.Collection;

/**
 * Created by angel on 18/5/2015.
 */
interface JoinImplGenerator {

    /**
     * Combines two plans using a Join operator. The method must check
     * if the plans respect the given Join Implentation requirements
     * @param p1 the left plan
     * @param p2 the right plan
     * @return a collection (possibly empty) of join trees
     */
    Collection<Join> generate(Plan p1, Plan p2);

}
