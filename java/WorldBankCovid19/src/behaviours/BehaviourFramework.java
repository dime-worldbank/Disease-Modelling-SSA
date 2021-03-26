package behaviours;

/**
 * The BehaviourFramework holds together a series of behaviours (encoded in BehaviourNodes)
 * which are linked together by a series of decisions (encoded in DecisionLinks).
 * 
 * @author swise
 *
 */
class BehaviourFramework {
	
	BehaviourNode entryPoint; // the point at which Agents are initialised
	
	public BehaviourNode getEntryPoint(){
		return entryPoint;
	}
}