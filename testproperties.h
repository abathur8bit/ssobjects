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

    //check that we get NULL if a property doesn't exist
    void testPropertyNotExist()
    {
        const char* propertyfile = "test.properties";
        try
        {
            Properties p(propertyfile);
            char* value = (char*)p.property("foobar");
            TS_ASSERT(!value);
        }
        catch(GeneralException& e)
        {
            TS_TRACE("Could not find:");
            TS_TRACE(propertyfile);
            TS_ASSERT(false);    //it worked
        }
    }
    void testPropertyExist()
    {
        const char* propertyfile = "test.properties";
//        const char* propertyfile = "~/.iserc";
        try
        {
            Properties p(propertyfile);
            const char* value = (char*)p.property("greeting");
            TS_ASSERT(value);
            if(value)   //avoid a segfault if the key was not found
            {
                TS_TRACE("greet value:");
                TS_TRACE(value);
                TS_ASSERT(0==strcmp(value,"hello"));
            }

            value = (char*)p.property("path");
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
    void testPropertyExistPropertyDoesnot()
    {
        const char* propertyfile = "test.properties";
        try
        {
            Properties p(propertyfile);
            const char* value = (char*)p.property("notthere");
            TS_ASSERT(!value);
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
