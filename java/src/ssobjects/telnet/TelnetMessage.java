package ssobjects.telnet;


/** Wraps the socket and text that was recieved from a socket. */
public class TelnetMessage {
    /** Socket text came from. */
    public TelnetServerSocket sock;
    /** What user typed, CR & LF has been stripped out. */
    public String text="";

    public TelnetMessage(TelnetServerSocket s,String t) {
        sock = s;
        if(null == t)
            t="";
        text = t;
    }

    public String toString() {
        return text;
    }
}
