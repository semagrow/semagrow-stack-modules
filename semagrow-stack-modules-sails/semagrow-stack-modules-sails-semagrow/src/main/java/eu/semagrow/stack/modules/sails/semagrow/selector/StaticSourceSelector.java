package eu.semagrow.stack.modules.sails.semagrow.selector;

import eu.semagrow.stack.modules.api.source.SourceMetadata;
import eu.semagrow.stack.modules.api.source.SourceSelector;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;

import java.util.*;

/**
 * Created by angel on 16/6/2015.
 */
public class StaticSourceSelector implements SourceSelector {

    private Map<StatementPattern, List<SourceMetadata>> map = new HashMap<>();

    public StaticSourceSelector(List<SourceMetadata> list) {
        buildMap(list);
    }

    private void buildMap(List<SourceMetadata> list) {
        for (SourceMetadata l : list) {
            List<SourceMetadata> ll = new LinkedList<SourceMetadata>();
            ll = map.getOrDefault(l.original(), ll);
            ll.add(l);
            map.put(l.original(), ll);
        }
    }

    @Override
    public List<SourceMetadata> getSources(StatementPattern pattern, Dataset dataset, BindingSet bindings) {
        if (map.containsKey(pattern))
            return map.get(pattern);
        else
            return Collections.emptyList();
    }

    @Override
    public List<SourceMetadata> getSources(Iterable<StatementPattern> patterns, Dataset dataset, BindingSet bindings) {
        List<SourceMetadata> list = new LinkedList<SourceMetadata>();
        for (StatementPattern p : patterns) {
            list.addAll(this.getSources(p, dataset, bindings));
        }
        return list;
    }

    @Override
    public List<SourceMetadata> getSources(TupleExpr expr, Dataset dataset, BindingSet bindings) {
        if (expr instanceof StatementPattern)
            return getSources((StatementPattern) expr,dataset,bindings);
        else
            return Collections.emptyList();
    }
}
