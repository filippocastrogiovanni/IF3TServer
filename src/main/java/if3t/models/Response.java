package if3t.models;

public class Response 
{
	private String message;
	private Integer code;
	private String reasonPhrase;
	
	public Response(String message, Integer code, String reasonPhrase) 
	{
		super();
		this.message = message;
		this.code = code;
		this.reasonPhrase = reasonPhrase;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}
}