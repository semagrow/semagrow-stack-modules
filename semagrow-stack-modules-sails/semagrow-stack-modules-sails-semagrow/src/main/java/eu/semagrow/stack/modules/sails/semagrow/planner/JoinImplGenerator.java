package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.Join;

import java.util.Collection;

/**
 * Created by angel on 18/5/2015.
 */
interface JoinImplGenerator {

    Collection<Join> generate(Plan p1, Plan p2);

}
