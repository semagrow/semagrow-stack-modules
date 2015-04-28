package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.api.estimator.CardinalityEstimator;
import eu.semagrow.stack.modules.api.source.SourceMetadata;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import eu.semagrow.stack.modules.sails.semagrow.algebra.BindJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.SourceQuery;
import eu.semagrow.stack.modules.sails.semagrow.estimator.CostEstimator;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

import java.util.*;

/**
 * Created by angel on 27/4/2015.
 */
public class PlanGeneratorImpl implements PlanGenerator {

    private SourceSelector       sourceSelector;
    private CostEstimator        costEstimator;
    private CardinalityEstimator cardinalityEstimator;
    private DecomposerContext ctx;

    public PlanGeneratorImpl(DecomposerContext ctx,
                             SourceSelector selector,
                             CostEstimator costEstimator,
                             CardinalityEstimator cardinalityEstimator)
    {
        this.ctx = ctx;
        this.sourceSelector = selector;
        this.costEstimator = costEstimator;
        this.cardinalityEstimator = cardinalityEstimator;
    }

    /**
     * Update the properties of a plan
     * @param plan
     */
    protected void updatePlan(Plan plan)
    {
        TupleExpr innerExpr = plan.getArg();

        // apply filters that can be applied
        TupleExpr e = PlanUtils.applyRemainingFilters(innerExpr.clone(), ctx.filters);

        // update cardinality and cost properties
        plan.getProperties().setCost(costEstimator.getCost(e, plan.getProperties().getSite()));
        plan.getProperties().setCardinality(cardinalityEstimator.getCardinality(e, plan.getProperties().getSite().getURI()));

        // update site

        // update ordering

        plan.getArg().replaceWith(e);
        //FIXME: update ordering, limit, distinct, group by
    }

    protected void updatePlanProperties(Plan plan)
    {
        TupleExpr innerExpr = plan.getArg();

        TupleExpr e = PlanUtils.applyRemainingFilters(innerExpr.clone(), ctx.filters);

        //FIXME: update ordering, limit, distinct, group by
        PlanProperties properties = PlanPropertiesUpdater.process(e);

        // update cardinality and cost properties
        plan.getProperties().setCost(costEstimator.getCost(e, plan.getProperties().getSite()));
        plan.getProperties().setCardinality(cardinalityEstimator.getCardinality(e, plan.getProperties().getSite().getURI()));

        plan.setProperties(properties);

        plan.getArg().replaceWith(e);
    }

    protected Plan createPlan(Set<TupleExpr> planId, TupleExpr innerExpr)
    {
        Plan p = new Plan(planId, innerExpr);
        updatePlan(p);
        return p;
    }

    protected Plan createPlan(Set<TupleExpr> planId, TupleExpr innerExpr, Site source)
    {
        Plan p = new Plan(planId, innerExpr);
        p.getProperties().setSite(source);
        updatePlan(p);
        return p;
    }

    protected Plan createPlan(Set<TupleExpr> planId, TupleExpr innerExpr, SourceMetadata metadata)
    {
        URI source = metadata.getEndpoints().get(0);
        Plan p = new Plan(planId, innerExpr);
        p.getProperties().setSite(new Site(source));

        Set<String> varNames = innerExpr.getBindingNames();
        /*
        for (String varName : varNames) {
            Collection<URI> schemas = metadata.getSchema(varName);
            if (!schemas.isEmpty())
                p.setSchemas(varName, schemas);
        }
        */
        updatePlan(p);
        return p;
    }

    @Override
    public Collection<Plan> accessPlans(TupleExpr expr, BindingSet bindings, Dataset dataset)
    {

        Collection<Plan> plans = new LinkedList<Plan>();

        // extract the statement patterns
        List<StatementPattern> statementPatterns = StatementPatternCollector.process(expr);

        // extract the filter conditions of the query

        for (StatementPattern pattern : statementPatterns) {

            // get sources for each pattern
            Collection<SourceMetadata> sources = getSources(pattern, dataset, bindings);

            // apply filters that can be applied to the statementpattern
            TupleExpr e = pattern;

            Set<TupleExpr> exprLabel =  new HashSet<TupleExpr>();
            exprLabel.add(e);

            List<Plan> sourcePlans = new LinkedList<Plan>();

            for (SourceMetadata sourceMetadata : sources) {
                //URI source = sourceMetadata.getEndpoints().get(0);
                //Plan p1 = createPlan(exprLabel, sourceMetadata.target(), source, ctx);
                // FIXME: Don't use always the first source.
                Plan p1 = createPlan(exprLabel, sourceMetadata.target().clone(), sourceMetadata);
                sourcePlans.add(p1);
            }

            Plan p  = createUnionPlan(sourcePlans);
            plans.add(p);
        }

        // SPLENDID also cluster statementpatterns of the same source.

        return plans;
    }

    public Plan createUnionPlan(List<Plan> plans)
    {
        Iterator<Plan> it = plans.iterator();
        Plan p;

        if (it.hasNext())
            p = it.next();
        else
            return null;

        while (it.hasNext()) {
            Plan i = it.next();
            p = createPlan(p.getPlanId(), new Union(enforceLocalSite(p), enforceLocalSite(i)));
        }

        return p;
    }

    protected Collection<SourceMetadata> getSources(StatementPattern pattern, Dataset dataset, BindingSet bindings) {
        return sourceSelector.getSources(pattern,dataset,bindings);
    }

    @Override
    public Collection<Plan> joinPlans(Collection<Plan> plan1, Collection<Plan> plan2)
    {
        Collection<Plan> plans = new LinkedList<Plan>();

        for (Plan p1 : plan1)
        {
            for (Plan p2 : plan2)
            {
                Set<TupleExpr> s = getId(p1.getPlanId(), p2.getPlanId());

                Collection<TupleExpr> joins = createPhysicalJoins(p1, p2);

                for (TupleExpr plan : joins) {
                    //TupleExpr e = applyRemainingFilters(plan, ctx.filters);
                    Plan p = createPlan(s, plan);
                    plans.add(p);
                }

                Plan expr = pushJoinRemote(p1, p2);

                if (expr != null) {
                    plans.add(expr);
                }
            }
        }
        return plans;
    }

    private Set<TupleExpr> getId(Set<TupleExpr> id1, Set<TupleExpr> id2) {
        Set<TupleExpr> s = new HashSet<TupleExpr>(id1);
        s.addAll(id2);
        return s;
    }

    private Collection<TupleExpr> createPhysicalJoins(Plan e1, Plan e2)
    {
        Collection<TupleExpr> plans = new LinkedList<TupleExpr>();

        TupleExpr expr;

        //TupleExpr expr = new Join(e1, e2);
        //if (!e2.getSite().equals(Plan.LOCAL)) {
        // FIXME
        if (isBindable(e2)) {
            expr = new BindJoin(enforceLocalSite(e1), enforceLocalSite(e2));
            plans.add(expr);
        }

        return plans;
    }

    private Plan pushJoinRemote(Plan e1, Plan e2) {

        Site site1 = e1.getProperties().getSite();
        Site site2 = e2.getProperties().getSite();

        if (site1.equals(site2) && !site1.isLocal()) {
            Set<TupleExpr> planid = new HashSet<TupleExpr>(e1.getPlanId());
            planid.addAll(e2.getPlanId());
            return createPlan(planid, new Join(e1,e2), site1);
        }

        return null;
    }

    private Plan enforceLocalSite(Plan p)
    {
        Site s = p.getProperties().getSite();
        if (s.isLocal())
            return p;
        else
            return createPlan(p.getPlanId(), new SourceQuery(p, s.getURI()), Site.LOCAL);
    }

    @Override
    public Collection<Plan> finalizePlans(Collection<Plan> plans) { return plans; }

    private boolean isBindable(TupleExpr expr) {
        IsBindableVisitor v = new IsBindableVisitor();
        expr.visit(v);
        return v.condition;
    }

    private class IsBindableVisitor extends PlanVisitorBase<RuntimeException>
    {
        boolean condition = false;

        @Override
        public void meet(Union union) {
            union.getLeftArg().visit(this);

            if (condition)
                union.getRightArg().visit(this);
        }

        @Override
        public void meetPlan(Plan e) {
            e.getArg().visit(this);
        }

        @Override
        public void meet(SourceQuery query) {
            condition = true;
        }

        @Override
        public void meetOther(QueryModelNode node) {
            if (node instanceof Plan)
                meetPlan((Plan)node);
            else if (node instanceof SourceQuery)
                meet((SourceQuery)node);
            else if (node instanceof Union)
                meet((Union) node);
            else
                condition = false;
        }
    }
}
