package eu.semagrow.stack.modules.sails.semagrow.planner;

/**
 * Created by angel on 18/5/2015.
 */
class LeftJoinPredicate extends QueryPredicate {

    public LeftJoinPredicate(String variable) { this.joinVariable = variable; }

    private String joinVariable;
}
