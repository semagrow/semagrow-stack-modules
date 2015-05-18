package eu.semagrow.stack.modules.sails.semagrow.planner;

/**
 * Created by angel on 18/5/2015.
 */
class JoinPredicate extends QueryPredicate {

    public JoinPredicate(String varName) {
        joinVariable = varName;
    }

    private String joinVariable;
}
