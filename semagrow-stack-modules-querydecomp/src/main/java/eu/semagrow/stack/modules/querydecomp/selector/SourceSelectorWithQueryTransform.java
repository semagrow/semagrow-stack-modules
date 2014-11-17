package eu.semagrow.stack.modules.querydecomp.selector;

import eu.semagrow.stack.modules.api.source.SourceSelector;
import eu.semagrow.stack.modules.api.transformation.EquivalentURI;
import eu.semagrow.stack.modules.api.transformation.QueryTransformation;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by angel on 11/17/14.
 */
public class SourceSelectorWithQueryTransform extends SourceSelectorWrapper {

    private QueryTransformation queryTransformation;

    public SourceSelectorWithQueryTransform(SourceSelector selector,
                                            QueryTransformation queryTransformation)
    {
        super(selector);
        this.queryTransformation = queryTransformation;
    }


    private Collection<StatementPattern> transformPattern(StatementPattern pattern) {

        Set<StatementPattern> transformedPatterns = new HashSet<StatementPattern>();

        //queryTransformation.retrieveEquivalentURIs(pattern)
        Var sVar = pattern.getSubjectVar();
        Var pVar = pattern.getPredicateVar();
        Var oVar = pattern.getObjectVar();

        for (Var sVar1 : transformVar(sVar) ) {
            for (Var pVar1 : transformVar(pVar) ) {
                for (Var oVar1 : transformVar(oVar) ) {
                    StatementPattern p = new StatementPattern(sVar1, pVar1, oVar1);
                    transformedPatterns.add(p);
                }
            }
        }

        return transformedPatterns;
    }

    // FIXME: take into account the semantic proximity
    private Collection<Var> transformVar(Var v) {
        Set<Var> vars = new HashSet<Var>();

        if (v.hasValue() ) {
            Value val = v.getValue();
            if (val instanceof URI) {

                URI uri = (URI)val;
                Collection<EquivalentURI> transformedURIs = queryTransformation.retrieveEquivalentURIs(uri);
                for (EquivalentURI eqUri : transformedURIs) {
                    URI uri2 = eqUri.getTargetURI();
                    Var v2 = v.clone();
                    v2.setValue(uri2);
                    vars.add(v2);
                }
                return vars;
            }
        }

        vars.add(v.clone());

        return vars;
    }
}
