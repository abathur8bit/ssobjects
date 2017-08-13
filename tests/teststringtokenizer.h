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
        StringTokenizer tok(csv);

        TS_ASSERT(0==strcmp(ptr=tok.next(buffer),"3"));
        TS_ASSERT(0==strcmp(ptr=tok.next(buffer),"MOVE"));
        TS_ASSERT(0==strcmp(ptr=tok.next(buffer),"object name"));
        TS_ASSERT(0==strcmp(ptr=tok.next(buffer),"1"));
    }

    void testTokenizerCountQuoted()
    {
        const char* csv = "HELO 'username=bleh'";
        char buffer[strlen(csv)];
        StringTokenizer tok(csv);
        TS_ASSERT(2==tok.count());
        TS_TRACE(tok.count());
        TS_ASSERT(0==strcmp(tok.next(buffer),"HELO"));
        TS_ASSERT(0==strcmp(tok.next(buffer),"username=bleh"));
    }

    /** Make sure we get the right count while using spaces. **/
    void testTokenizerCountSpaces()
    {
        const char* csv = "HELO username password";
        char buffer[strlen(csv)];
        StringTokenizer tok(csv);
        TS_ASSERT(3==tok.count());
        TS_TRACE(tok.count());
    }
};
