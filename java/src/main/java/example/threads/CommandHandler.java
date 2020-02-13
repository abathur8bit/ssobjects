package example.threads;

public abstract class CommandHandler
{
	public CommandHandler()
	{
	}
	
	public abstract String getHelp();
	public abstract String getCmd();
	public abstract String execute();
}
