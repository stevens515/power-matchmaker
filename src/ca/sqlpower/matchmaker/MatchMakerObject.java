package ca.sqlpower.matchmaker;

import java.util.List;

import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The interface for all of the match maker specific business objects
 *
 */
public interface MatchMakerObject<T extends MatchMakerObject> extends Auditable {
	
	/**
	 * Support for adding match maker event listeners
	 */
	void addMatchMakerListener(MatchMakerListener<MatchMakerObject,T> l);
	
	/**
	 * Support for adding match maker event listeners
	 */
	void removeMatchMakerListener(MatchMakerListener<MatchMakerObject,T> l);
	
	/**
	 * get the parent of this object
	 */
	MatchMakerObject getParent();
	
	/**
	 * Set the parent (ie. the object that holds this one as a child)
	 */
	 void setParent(MatchMakerObject parent);
	
	/**
	 * Returns the object's primary children
	 */
	List<T> getChildren();
	
	/**
	 * Get the number of children
	 */
	int getChildCount();
	
	/**
	 * Add a new child to this object
	 */
	void addChild(T child);
	
	/**
	 * remove a child from this object
	 */
	void removeChild(T child);
	
	/**
	 * makes this object aware of the session's application user name
	 */
	public void setAppUserName(String appUserName);
	
	public int hashCode();
	
	public boolean equals(Object obj);
}
