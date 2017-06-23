#-------------------------------------------------
#
# Project created by QtCreator 2017-06-22T18:09:21
#
#-------------------------------------------------

QT       -= core gui

TARGET = ssobjects
TEMPLATE = lib
CONFIG += staticlib

SOURCES += aserver.cpp

HEADERS += aserver.h
unix {
    target.path = /usr/lib
    INSTALLS += target
}
