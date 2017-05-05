package io.rapid;

enum CloseReason
{
	UNKNOWN, INTERNET_CONNECTION_LOST, NO_INTERNET_CONNECTION, CLOSED_MANUALLY, CLOSED_FROM_SERVER;


	// for lib connection
	static CloseReason get(int code)
	{
		switch(code)
		{
			case 1006:
				return INTERNET_CONNECTION_LOST;
			case -1:
				return NO_INTERNET_CONNECTION;
			case 1000:
				return CLOSED_MANUALLY;
			default:
				return UNKNOWN;
		}
	}


	// for Async connection
	static CloseReason get(Exception ex)
	{
		if(ex == null)
		{
			return CLOSED_FROM_SERVER;
		}
		else
		{
			switch(ex.getMessage())
			{
				case "Software caused connection abort":
					return INTERNET_CONNECTION_LOST;
				default:
					return UNKNOWN;
			}
		}
	}
}