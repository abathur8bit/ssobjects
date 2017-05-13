package ssobjects.http;

import java.util.concurrent.Executor;

/**
 * Created by lee on 2/11/17.
 */
public class SingleThreadExecutor implements Executor
{
    public void execute(Runnable r)
    {
        r.run();
    }
}
