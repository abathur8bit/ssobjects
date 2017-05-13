package example.threads;

public class HelpCommandHandler extends CommandHandler
{
	public HelpCommandHandler()
	{
	}

	@Override
	public String getHelp()
	{
		return "Lists all commands";
	}

	@Override
	public String getCmd()
	{
		return null;
	}

	@Override
	public String execute()
	{
		return null;
	}

}
