/********************************************************************
        Copyright (c) 2017, Lee Patterson
        https://github.com/abathur8bit/ssobjects

        created  :  08/12/2017
        author   :  Lee Patterson
        filename :  teststringtokenizer.h

        purpose  :  Unit tests for making sure StringTokenizer works
                    as expected.

*********************************************************************/

#include <cxxtest/TestSuite.h>

#include <string>
#include <list>
#include <string.h>

#include "../stringtokenizer.h"

using namespace std;

/**
 * \brief Unit tests for making sure StringTokenizer works as expected.
 */
class TestStringTokenizer : public CxxTest::TestSuite
{
public:
    void testTokenizer()
    {
        const char* csv = "3,MOVE 'object name',1";
        char buffer[100];
        char* ptr=buffer;
        StringTokenizer tok;

        TS_ASSERT(0==strcmp(ptr=tok.next(buffer,csv),"3"));
        TS_ASSERT(0==strcmp(ptr=tok.next(buffer,csv),"MOVE"));
        TS_ASSERT(0==strcmp(ptr=tok.next(buffer,csv),"object name"));
        TS_ASSERT(0==strcmp(ptr=tok.next(buffer,csv),"1"));
    }

    void testTokenizerCountQuoted()
    {
        const char* csv = "HELO 'username=bleh'";
        char buffer[strlen(csv)];
        StringTokenizer tok;
        TS_ASSERT(2==tok.count(csv));
        TS_TRACE(tok.count(csv));
        TS_ASSERT(0==strcmp(tok.next(buffer,csv),"HELO"));
        TS_ASSERT(0==strcmp(tok.next(buffer,csv),"username=bleh"));
    }

    /** Make sure we get the right count while using spaces. **/
    void testTokenizerCountSpaces()
    {
        const char* csv = "HELO username password";
        char buffer[strlen(csv)];
        StringTokenizer tok;
        TS_ASSERT(3==tok.count(csv));
        TS_TRACE(tok.count(csv));
    }
};
