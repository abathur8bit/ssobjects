#ifndef TESTPROPERTIES_H
#define TESTPROPERTIES_H

#include <unistd.h>
#include <sys/types.h>
#include <pwd.h>

#include "properties.h"
#include "generalexception.h"
#include "logs.h"

using namespace ssobjects;

class TestPropertiesSuite : public CxxTest::TestSuite
{
public:
    void testFileNotFound()
    {
        try
        {
            Properties p("nofile");

            TS_TRACE("This should have thrown an exception but it didn't");
            TS_ASSERT(false);   //this should have failed
        }
        catch(GeneralException& e)
        {
            TS_TRACE("File not found threw successfully");
            TS_ASSERT(true);    //it worked
        }
    }

    void testPropertyExist()
    {
        const char* propertyfile = "test.properties";
//        const char* propertyfile = "~/.iserc";
        try
        {
            Properties p(propertyfile);
            const char* value = (char*)p.property("greeting","xyz");
            TS_ASSERT(value);
            if(value)   //avoid a segfault if the key was not found
            {
                TS_TRACE("greet value:");
                TS_TRACE(value);
                TS_ASSERT(0==strcmp(value,"hello"));
            }

            value = (char*)p.property("path","/abc/xyz/");
            TS_ASSERT(value);
            if(value)   //avoid a segfault if the key was not found
            {
                TS_TRACE("path value:");
                TS_TRACE(value);
                TS_ASSERT(0==strcmp(value,"/tmp/foobar"));
            }
        }
        catch(GeneralException& e)
        {
            TS_TRACE("Could not find:");
            TS_TRACE(propertyfile);
            TS_ASSERT(false);    //it worked
        }
    }
    //request a property that doesn't exist, it should return the default value
    void testPropertyDefault()
    {
        const char* propertyfile = "test.properties";
        try
        {
            Properties p(propertyfile);
            const char* value = (char*)p.property("xxx","xyz");
            TS_ASSERT(value);   //make sure we got something back
            if(value)
            {
                TS_ASSERT(0==strcmp(value,"xyz"));
            }

        }
        catch(GeneralException& e)
        {
            TS_TRACE("Could not find:");
            TS_TRACE(propertyfile);
            TS_ASSERT(false);    //it worked
        }
    }

    void testHomeDir()
    {
        const char *homedir = getenv("HOME");
        TS_ASSERT(homedir);
        if(homedir)
            TS_TRACE(homedir);
        homedir = getpwuid(getuid())->pw_dir;
        TS_ASSERT(homedir);
        if(homedir)
            TS_TRACE(homedir);

    }
};

#endif // TESTPROPERTIES_H
