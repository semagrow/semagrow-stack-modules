/**
 * 
 */
package eu.semagrow.stack.modules.utils.impl;

import java.util.ArrayList;
import java.util.List;

import eu.semagrow.stack.modules.utils.EquivalentURI;
import eu.semagrow.stack.modules.utils.StatementEquivalents;

/* (non-Javadoc)
 * @see eu.semagrow.stack.modules.utils.StatementEquivalents
 */
public class StatementEquivalentsImpl implements StatementEquivalents {
	
	private List<EquivalentURI> subject_equivalents;
	private List<EquivalentURI> predicate_equivalents;
	private List<EquivalentURI> object_equivalents;
	/**
	 * 
	 */
	public StatementEquivalentsImpl() {
		super();
		subject_equivalents = new ArrayList<EquivalentURI>();
		predicate_equivalents = new ArrayList<EquivalentURI>();
		object_equivalents = new ArrayList<EquivalentURI>();
	}
	/* (non-Javadoc)
	 * @see eu.semagrow.stack.modules.utils.StatementEquivalents#getSubject_equivalents()
	 */
	public List<EquivalentURI> getSubject_equivalents() {
		return subject_equivalents;
	}
	/* (non-Javadoc)
	 * @see eu.semagrow.stack.modules.utils.StatementEquivalents#setSubject_equivalents(java.util.ArrayList)
	 */
	public void setSubject_equivalents(List<EquivalentURI> subject_equivalents) {
		this.subject_equivalents = subject_equivalents;
	}
	/* (non-Javadoc)
	 * @see eu.semagrow.stack.modules.utils.StatementEquivalents#getPredicate_equivalents()
	 */
	public List<EquivalentURI> getPredicate_equivalents() {
		return predicate_equivalents;
	}
	/* (non-Javadoc)
	 * @see eu.semagrow.stack.modules.utils.StatementEquivalents#setPredicate_equivalents(java.util.ArrayList)
	 */
	public void setPredicate_equivalents(
			List<EquivalentURI> predicate_equivalents) {
		this.predicate_equivalents = predicate_equivalents;
	}
	/* (non-Javadoc)
	 * @see eu.semagrow.stack.modules.utils.StatementEquivalents#getObject_equivalents()
	 */
	public List<EquivalentURI> getObject_equivalents() {
		return object_equivalents;
	}
	/* (non-Javadoc)
	 * @see eu.semagrow.stack.modules.utils.StatementEquivalents#setObject_equivalents(java.util.ArrayList)
	 */
	public void setObject_equivalents(List<EquivalentURI> object_equivalents) {
		this.object_equivalents = object_equivalents;
	}
	
	

}
