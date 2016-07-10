package if3t.exceptions;

public class NoPermissionException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoPermissionException(){
		super("ERROR: You don't have permissions to perform this action!");
	}
	
	public NoPermissionException(String message){
		super(message);
	}
}
