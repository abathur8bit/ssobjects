#!/bin/bash
~/workspace/cxxtest-4.4/bin/cxxtestgen --error-printer -o runner.cpp teststringtokenizer.h testproperties.h  && g++ -w -std=c++11 -o runner -I/Users/lee/workspace/cxxtest-4.4 -I.. -I. -DDEBUG ../generalexception.cpp ../cstr.cpp ../properties.cpp ../stringtokenizer.cpp runner.cpp && ./runner $*
