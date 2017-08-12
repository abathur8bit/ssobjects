#!/bin/bash
#********************************************************************
#        Copyright (c) 2017, Lee Patterson
#        https://github.com/abathur8bit/ssobjects
#
#        created  :  08/12/2017
#        author   :  Lee Patterson
#        filename :  runtests.sh
#
#        purpose  :  Unit tests for making sure StringTokenizer works
#                    as expected.
#
#*********************************************************************
~/workspace/cxxtest-4.4/bin/cxxtestgen --error-printer -o runner.cpp teststringtokenizer.h testproperties.h  && g++ -w -std=c++11 -o runner -I/Users/lee/workspace/cxxtest-4.4 -I.. -I. -DDEBUG ../generalexception.cpp ../cstr.cpp ../properties.cpp ../stringtokenizer.cpp runner.cpp && ./runner $*
