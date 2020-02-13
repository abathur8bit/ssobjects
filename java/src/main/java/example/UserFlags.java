package example;

public class UserFlags
{
	public static final int UFLAG_TICK = 1;
	public static final int UFLAG_STATS = 2;
	
	public static int set(int value,int flag)
	{
		return value|flag;
	}
	public static int reset(int value,int flag)
	{
		return value & (Integer.MAX_VALUE-flag);
	}
	public static boolean isSet(int value,int flag)
	{
		return (value & flag) == flag;
	}
	
	/** Changes a set flag to reset, and a reset flag to set. */
	public static int toggle(int value,int flag)
	{
		if(isSet(value,flag))
			return UserFlags.reset(value,flag);
		return UserFlags.set(value,flag);
	}
}
