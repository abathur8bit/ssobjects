package example;

public enum UserStateEnum {
    LOGIN,
    PASSWORD,
    ACTIVE,
    BUSY,
    PLAYING,
    ERROR,		//user had a socket error, and needs to be closed and removed from any game
}
