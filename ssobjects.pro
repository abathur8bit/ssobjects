#-------------------------------------------------
#
# Project created by QtCreator 2017-06-22T18:09:21
#
#-------------------------------------------------

QT       -= core gui

TARGET = ssobjects
TEMPLATE = lib
CONFIG += staticlib

SOURCES += \
    asyncclientconnector.cpp \
    bufferedsocket.cpp \
    clientconnector.cpp \
    cstr.cpp \
    file.cpp \
    flags.cpp \
    generalexception.cpp \
    gettimeofday.cpp \
    logs.cpp \
    mclautolock.cpp \
    mclcritsec.cpp \
    mclevent.cpp \
    mclthread.cpp \
    packetbuffer.cpp \
    packetmessageque.cpp \
    Parseit.cpp \
    serverhandler.cpp \
    servermanager.cpp \
    serversocket.cpp \
    simplemanager.cpp \
    simpleserver.cpp \
    socketinstance.cpp \
    stopwatch.cpp \
    telnetserver.cpp \
    telnetserversocket.cpp \
    threadutils.cpp \
    tsleep.cpp \
    websocketinstance.cpp \
    websocketserver.cpp

HEADERS += \
    asyncclientconnector.h \
    bufferedsocket.h \
    clientconnector.h \
    cstr.h \
    defs.h \
    file.h \
    flags.h \
    generalexception.h \
    gettimeofday.h \
    linkedlist.h \
    logs.h \
    mcl.h \
    mclautolock.h \
    mclcritsec.h \
    mclevent.h \
    mclglobals.h \
    mclmutex.h \
    mclthread.h \
    msdefs.h \
    packetbuffer.h \
    packetmessageque.h \
    Parseit.h \
    serverhandler.h \
    servermanager.h \
    serversocket.h \
    SimpleDate.h \
    simplemanager.h \
    simpleserver.h \
    socketinstance.h \
    ssobjects.h \
    stdafx.h \
    stopwatch.h \
    str.h \
    telnetserver.h \
    telnetserversocket.h \
    threadutils.h \
    timeval.h \
    tsleep.h \
    websocketinstance.h \
    websocketserver.h
unix {
    target.path = /usr/lib
    INSTALLS += target
}

DISTFILES += \
    Makefile \
    ssobjects-ddoc.xml
