package eu.semagrow.stack.modules.sails.semagrow.planner;


import org.openrdf.query.algebra.TupleExpr;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by angel on 21/4/2015.
 */
public class PlanProperties {

    private Cost nodeCost;

    private Cost cumulativeCost;

    private Ordering ordering;

    private long cardinality;

    private Site site;

    private Set<TupleExpr> relations;

    public long getCardinality() { return cardinality; }

    public void setCardinality(long card) { this.cardinality = card;}

    public Cost getCost() { return nodeCost; }

    public void setCost(Cost cost) { this.nodeCost = cost; }

    public Site getSite() { return site; }

    public void setSite(Site site) { this.site = site; }

    /*
    public Collection<URI> getSchemas(String var) {
        if (schemas.containsKey(var))
            return schemas.get(var);

        return Collections.emptySet();
    }

    public void setSchemas(String var, Collection<URI> varSchemas) {
        this.schemas.put(var, varSchemas);
    }
    */

    public Ordering getOrdering() { return ordering; }

    public void setOrdering(Ordering ordering) { this.ordering = ordering; }

    public Set<TupleExpr> getRelations() { return relations; }

    public void setRelations(Set<TupleExpr> relations) { this.relations = relations; }

    public static PlanProperties defaultProperties() {
        PlanProperties p = new PlanProperties();
        p.setOrdering(Ordering.NOORDERING);
        p.setSite(Site.LOCAL);
        return p;
    }

    public PlanProperties clone() {
        PlanProperties p = new PlanProperties();
        p.nodeCost = this.nodeCost;
        p.ordering = this.ordering;
        p.site = this.site;
        p.relations = new HashSet<>(this.relations);
        return p;
    }

    public boolean isComparable(PlanProperties properties) {
        return (properties.getSite().equals(this.getSite()));
    }
}
