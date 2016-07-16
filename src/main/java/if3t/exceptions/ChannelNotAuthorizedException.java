package if3t.exceptions;

public class ChannelNotAuthorizedException extends Exception{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8858597094786355426L;

	public ChannelNotAuthorizedException(){
		super();
	}
	
	public ChannelNotAuthorizedException(String message){
		super(message);
	}
}
