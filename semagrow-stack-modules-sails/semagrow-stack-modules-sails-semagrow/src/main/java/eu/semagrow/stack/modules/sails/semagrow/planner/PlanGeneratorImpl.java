package eu.semagrow.stack.modules.sails.semagrow.planner;

import eu.semagrow.stack.modules.api.estimator.CardinalityEstimator;
import eu.semagrow.stack.modules.api.source.SourceMetadata;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import eu.semagrow.stack.modules.sails.semagrow.algebra.BindJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.HashJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.MergeJoin;
import eu.semagrow.stack.modules.sails.semagrow.algebra.SourceQuery;
import eu.semagrow.stack.modules.sails.semagrow.estimator.CostEstimator;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;

import java.util.*;

/**
 * Created by angel on 27/4/2015.
 */
public class PlanGeneratorImpl implements PlanGenerator<Plan> {

    private SourceSelector       sourceSelector;
    private CostEstimator        costEstimator;
    private CardinalityEstimator cardinalityEstimator;

    private DecomposerContext ctx;

    protected Collection<JoinImplGenerator> joinImplGenerators;

    public PlanGeneratorImpl(DecomposerContext ctx,
                             SourceSelector selector,
                             CostEstimator costEstimator,
                             CardinalityEstimator cardinalityEstimator)
    {
        this.ctx = ctx;
        this.sourceSelector = selector;
        this.costEstimator = costEstimator;
        this.cardinalityEstimator = cardinalityEstimator;

        this.joinImplGenerators = new LinkedList<JoinImplGenerator>();
        this.joinImplGenerators.add(new BindJoinGenerator());
        this.joinImplGenerators.add(new RemoteJoinGenerator());
        //this.joinImplGenerators.add(new HashJoinGenerator());
        //this.joinImplGenerators.add(new MergeJoinGenerator());
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

        plan.getArg().replaceWith(e);

        updatePlanProperties(plan);
    }

    protected void updatePlanProperties(Plan plan)
    {
        TupleExpr e = plan.getArg();

        PlanProperties properties = PlanPropertiesUpdater.process(e, plan.getProperties());

        plan.setProperties(properties);

        // update cardinality and cost properties
        plan.getProperties().setCost(costEstimator.getCost(e, plan.getProperties().getSite()));
        plan.getProperties().setCardinality(cardinalityEstimator.getCardinality(e, plan.getProperties().getSite().getURI()));
    }

    protected Plan createPlan(TupleExpr innerExpr)
    {
        PlanImpl p = new PlanImpl(innerExpr);
        updatePlanProperties(p);
        return p;
    }

    protected Plan createPlan(TupleExpr innerExpr, SourceMetadata metadata)
    {
        URI source = metadata.getEndpoints().get(0);

        PlanImpl p = new PlanImpl(innerExpr);

        Set<String> varNames = innerExpr.getBindingNames();

        /*
        for (String varName : varNames) {
            Collection<URI> schemas = metadata.getSchema(varName);
            if (!schemas.isEmpty())
                p.setSchemas(varName, schemas);
        }
        */
        p.getProperties().setSite(new Site(source));
        p.getProperties().setRelations(Collections.singleton(innerExpr));
        updatePlan(p);

        return p;
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
            p = createPlan(new Union(enforceLocalSite(p), enforceLocalSite(i)));
        }

        return p;
    }

    public Collection<Plan> access(TupleExpr expr, BindingSet bindings, Dataset dataset)
    {

        Collection<Plan> plans = new LinkedList<Plan>();

        // get sources for each pattern
        Collection<SourceMetadata> sources = getSources(expr, dataset, bindings);

        // apply filters that can be applied to the statementpattern

        List<Plan> sourcePlans = new LinkedList<Plan>();

        for (SourceMetadata sourceMetadata : sources)
        {
             //URI source = sourceMetadata.getEndpoints().get(0);
             //Plan p1 = createPlan(exprLabel, sourceMetadata.target(), source, ctx);
             // FIXME: Don't use always the first source.
             Plan p1 = createPlan(sourceMetadata.target().clone(), sourceMetadata);
             sourcePlans.add(p1);
        }

        Plan p  = createUnionPlan(sourcePlans);
        plans.add(p);

        return plans;
    }

    protected Collection<SourceMetadata> getSources(TupleExpr pattern, Dataset dataset, BindingSet bindings) {
        return sourceSelector.getSources(pattern,dataset,bindings);
    }

    public Collection<Plan> combine(Collection<Plan> plan1, Collection<Plan> plan2)
    {
        Collection<Plan> plans = new LinkedList<Plan>();

        for (JoinImplGenerator generator : joinImplGenerators) {

            for (Plan p1 : plan1) {
                for (Plan p2 : plan2) {
                    //Set<TupleExpr> s = getKey(p1.getKey(), p2.getKey());

                    Collection<Join> joins = generator.generate(p1, p2);

                    for (Join j : joins)
                        plans.add(createPlan(j));
                }
            }
        }
        return plans;
    }

    public Collection<Plan> finalize(Collection<Plan> plans, PlanProperties properties) {
        Collection<Plan> pl = new LinkedList<Plan>();

        for (Plan p : plans)
            pl.add(enforceLocalSite(p));

        return pl;
    }


    private Plan enforceLocalSite(Plan p)
    {
        Site s = p.getProperties().getSite();
        if (s.isLocal())
            return p;
        else
            return createPlan(new SourceQuery(p, s.getURI()));
    }

    private Plan enforceOrdering(Plan p, Ordering ordering)
    {
        if (p.getProperties().getOrdering().isCoverOf(ordering)) {
            return p;
        } else {
            return createPlan(new Order(p, ordering.getOrderElements()));
        }
    }

    protected interface PropertyEnforcer
    {
        Plan enforceSite(Plan p, Site s);

        Plan enforceOrdering(Plan p, Ordering o);

    }

    private class PropertyEnforcerImpl implements PropertyEnforcer
    {
        @Override
        public Plan enforceSite(Plan p, Site s) {
            if (p.getProperties().getSite().equals(s))
                return p;
            else
                return createPlan(new SourceQuery(p, s.getURI()));
        }

        @Override
        public Plan enforceOrdering(Plan p, Ordering o) {
            return p;
        }
    }

    private class RemoteJoinGenerator implements JoinImplGenerator {

        @Override
        public Collection<Join> generate(Plan p1, Plan p2) {
            Collection<Join> l = new LinkedList<Join>();

            if (p1.getProperties().getSite().isRemote() &&
                    p2.getProperties().getSite().isRemote() &&
                    p1.getProperties().getSite().equals(p2.getProperties().getSite())) {
                Join j = new Join(p1, p2);
                l.add(j);
            }

            return l;
        }
    }

    private class BindJoinGenerator implements JoinImplGenerator {

            @Override
            public Collection<Join> generate(Plan p1, Plan p2) {

                Collection<Join> l = new LinkedList<Join>();

                if (isBindable(p2)) {
                    Join expr = new BindJoin(enforceLocalSite(p1), enforceLocalSite(p2));
                    l.add(expr);
                }

                return l;
            }

            private boolean isBindable(TupleExpr expr) {
                IsBindableVisitor v = new IsBindableVisitor();
                expr.visit(v);
                return v.condition;
            }

            private class IsBindableVisitor extends PlanVisitorBase<RuntimeException> {
                boolean condition = false;

                @Override
                public void meet(Union union) {
                    union.getLeftArg().visit(this);

                    if (condition)
                        union.getRightArg().visit(this);
                }

                @Override
                public void meet(Join join) {
                    condition = false;
                }

                @Override
                public void meet(PlanImpl e) {
                    if (e.getProperties().getSite().isRemote())
                        condition = true;
                    else
                        e.getArg().visit(this);
                }

                @Override
                public void meet(SourceQuery query) {
                    condition = true;
                }
            }

    }

    private class HashJoinGenerator implements JoinImplGenerator {

        @Override
        public Collection<Join> generate(Plan p1, Plan p2) {

            Collection<Join> l = new LinkedList<Join>();

            Join expr = new HashJoin(enforceLocalSite(p1), enforceLocalSite(p2));

            l.add(expr);

            return l;
        }
    }

    private class MergeJoinGenerator implements JoinImplGenerator {

        @Override
        public Collection<Join> generate(Plan p1, Plan p2) {

            Collection<Join> l = new LinkedList<Join>();

            Ordering o = null;

            Join expr = new MergeJoin(
                    enforceLocalSite(enforceOrdering(p1, o)),
                    enforceLocalSite(enforceOrdering(p2, o)));

            l.add(expr);

            return l;
        }
    }

}
