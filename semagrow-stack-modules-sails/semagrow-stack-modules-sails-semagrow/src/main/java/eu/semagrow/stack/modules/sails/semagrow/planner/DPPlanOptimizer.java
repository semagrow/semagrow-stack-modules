package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.sails.semagrow.helpers.CombinationIterator;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by angel on 27/4/2015.
 */
public class DPPlanOptimizer implements PlanOptimizer {

    private PlanGenerator planGenerator;

    final private Logger logger = LoggerFactory.getLogger(DPPlanOptimizer.class);

    public DPPlanOptimizer(PlanGenerator planGenerator)
    {
        this.planGenerator = planGenerator;
    }

    protected void prunePlans(Collection<Plan> plans)
    {
        List<Plan> bestPlans = new ArrayList<Plan>();

        boolean inComparable;

        for (Plan candidatePlan : plans) {
            inComparable = true;

            ListIterator<Plan> pIter = bestPlans.listIterator();
            while (pIter.hasNext()) {
                Plan plan = pIter.next();

                if (isPlanComparable(candidatePlan, plan)) {
                    inComparable = false;
                    int plan_comp = comparePlan(candidatePlan, plan);


                    if (plan_comp != 0)
                        inComparable = false;

                    if (plan_comp < 1) {
                        pIter.remove();
                        pIter.add(candidatePlan);
                    }
                }
            }

            // check if plan is incomparable with all best plans yet discovered.
            if (inComparable)
                bestPlans.add(candidatePlan);
        }

        int planSize = plans.size();
        plans.retainAll(bestPlans);
        logger.debug("Pruned " + (planSize - plans.size()) + " suboptimal plans of " + planSize + " plans");
    }

    protected int comparePlan(Plan plan1, Plan plan2)
    {
        if (isPlanComparable(plan1, plan2)) {
            return plan1.getProperties().getCost().compareTo(plan2.getProperties().getCost());
        } else {
            return 0;
        }
    }

    private boolean isPlanComparable(Plan plan1, Plan plan2) {
        // FIXME: take plan properties into account
        return plan1.getPlanId().equals(plan2.getPlanId()) &&
                plan1.getProperties().isComparable(plan2.getProperties());
    }

    public Plan getBestPlan(TupleExpr expr, BindingSet bindings, Dataset dataset)
    {
        // optPlans is a function from (Set of Expressions) to (Set of Plans)
        PlanCollection optPlans = new PlanCollection();

        Collection<Plan> accessPlans = planGenerator.accessPlans(expr, bindings, dataset);

        optPlans.addPlan(accessPlans);

        // plans.getExpressions() get basic expressions
        // subsets S of size i
        //
        Set<TupleExpr> r = optPlans.getExpressions();

        int count = r.size();

        // TODO: implement a generic pair enumerator.
        // bottom-up starting for subplans of size "k"
        for (int k = 2; k <= count; k++) {

            // enumerate all subsets of r of size k
            for (Set<TupleExpr> s : subsetsOf(r, k)) {

                for (int i = 1; i < k; i++) {

                    // let disjoint sets o1 and o2 such that s = o1 union o2
                    for (Set<TupleExpr> o1 : subsetsOf(s, i)) {

                        Set<TupleExpr> o2 = new HashSet<TupleExpr>(s);
                        o2.removeAll(o1);

                        Collection<Plan> plans1 = optPlans.get(o1);
                        Collection<Plan> plans2 = optPlans.get(o2);

                        Collection<Plan> newPlans = planGenerator.joinPlans(plans1, plans2);

                        optPlans.addPlan(newPlans);
                    }
                }
                prunePlans(optPlans.get(s));
            }
        }

        Collection<Plan> fullPlans = optPlans.get(r);
        fullPlans = planGenerator.finalizePlans(fullPlans);

        if (!fullPlans.isEmpty()) {
            logger.info("Found " + fullPlans.size() + " complete optimal plans");
        }

        return getBestPlan(fullPlans);
    }

    private Plan getBestPlan(Collection<Plan> plans)
    {
        if (plans.isEmpty())
            return null;

        Plan bestPlan = plans.iterator().next();

        for (Plan p : plans)
            if (p.getProperties().getCost().compareTo(bestPlan.getProperties().getCost()) == -1)
                bestPlan = p;

        return bestPlan;
    }

    private static <T> Iterable<Set<T>> subsetsOf(Set<T> s, int k) {
        return new CombinationIterator<T>(k, s);
    }


    private class Pair<A, B>
    {

        final private A first;

        final private B second;

        public Pair(A first, B second)
        {
            this.first = first;
            this.second = second;
        }


        public A getFirst() { return first; }

        public B getSecond() { return second; }

        public String toString() { return "(" + first.toString() + ", " + second.toString() + ")"; }


    }

    /*
    private class SubsetPairIterator<A> extends Iterator<Pair<Set<A>, Set<A>>>
    {


    }
    */
}
