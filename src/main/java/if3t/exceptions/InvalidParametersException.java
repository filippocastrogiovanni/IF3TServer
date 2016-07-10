package if3t.exceptions;

public class InvalidParametersException extends Exception {

	private static final long serialVersionUID = 601195891602762377L;

	public InvalidParametersException(){
		super();
	}

	public InvalidParametersException(String message){
		super(message);
	}

}
