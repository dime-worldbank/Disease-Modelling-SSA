package behaviours;

import objects.Person;

/**
 * BehaviourNodes are the behavioural units which identify the actions Persons take.
 * 
 * The nodes contain the behaviours the agents will undertake - e.g. going home, going 
 * out to work, spending time in the community, etc. They are joined together by 
 * BehaviourDecisionLinks, which dictate the circumstances under which each behaviour
 * (that is, each BehaviourNode) is activated.
 * 
 * When activated the BehaviourNodes return a *double* value - this is the
 * amount of time the Person will spend on the Behaviour unless they are interrupted
 * by some other thing. The Person can then use this value to schedule their next 
 * activation.  
 * 
 * @author swise
 *
 */
public interface BehaviourNode extends java.io.Serializable{
	
	public String getTitle(); // the name of the function (makes debugging easier)
	
	/**
	 * overloadable action function
	 * @param p - the Person to activate
	 * @return the amount of time the Person will, unless interrupted, spend on this activity before
	 * 			checking in again. 
	 */
	//public double activate(Person p);
	
	/**
	 * overloadable transition function. Transition the Person to the next activity, and return
	 * the time taken on the new activity before an update is required.
	 * 
	 * @param p - the Person to transition to the next step
	 * @return the next BehaviourNode for the person to go to
	 */
	public double next(Person p, double time);
}