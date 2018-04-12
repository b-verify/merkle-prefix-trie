package mpt;

/**
 * This exception indicates that an operation in a MPT
 * required access to a part of the MPT that is not 
 * present (represented by a stub)
 * @author henryaspegren
 *
 */
public class IncompleteMPTException extends Exception {

	private static final long serialVersionUID = 1L;

	public IncompleteMPTException(String message) {
		super(message);
	}
	
}
