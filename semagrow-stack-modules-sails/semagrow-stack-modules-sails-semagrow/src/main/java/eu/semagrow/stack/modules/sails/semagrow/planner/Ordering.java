package eu.semagrow.stack.modules.sails.semagrow.planner;

import org.openrdf.query.algebra.OrderElem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by angel on 10/2/14.
 */
public class Ordering {

    static public Ordering NOORDERING = new Ordering(new LinkedList<OrderElem>());

    private List<OrderElem> orderElements;

    public Ordering(List<OrderElem> orderElements) {
        this.orderElements = new LinkedList<OrderElem>(orderElements);
    }

    public Ordering cover(Ordering ordering) {
        if (isPrefix(orderElements.iterator(), ordering.orderElements.iterator())) {
            return this.clone();
        } else if (isPrefix(orderElements.iterator(), ordering.orderElements.iterator())) {
            return ordering.clone();
        } else {
            return null;
        }
    }

    public boolean isCoverOf(Ordering ordering) {
        return isPrefix(ordering.orderElements.iterator(), orderElements.iterator());
    }

    public Ordering clone() {
        return new Ordering(this.orderElements);
    }

    static private <T> boolean isPrefix(Iterator<T> iter1, Iterator<T> iter2) {

        while (iter1.hasNext()) {
            T o1 = iter1.next();

            if (!iter2.hasNext())
                return false;

            T o2 = iter2.next();
            if (!o1.equals(o2))
                return false;
        }
        return true;
    }


    public String toString() {
        if (orderElements.isEmpty())
            return "(no ordering)";
        else
        {
            Iterator<OrderElem> elem = orderElements.iterator();
            String s = "";
            while (elem.hasNext()) {
                OrderElem oe = elem.next();
                s += ((oe.isAscending()) ? "ASC" : "DESC") + oe.getExpr().toString();
            }
            return s;
        }
    }
}
