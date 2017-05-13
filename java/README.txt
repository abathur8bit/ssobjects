Copyright (c) 2001, Lee Patterson
http://ssobjects.sourceforge.net

This is a Java port of ssobjects, and is not complete or fully tested. Class names are similar to their C++ counterpart. But as a quick example, you can build and run a sample telnet chat server. The server runs then waits for a connection. In a different console, type "telnet localhost 4002". You will be connected to the server, prompted for your name, then you can type text and it will be broadcast to other users connected. Telnet server will accept multiple connections.

Build: 
    $ ant

Run all junit tests:
    $ ant test

Run simple telnet chat server (Ctrl-C to exit server, runs on port 4002):
    $ ant run
