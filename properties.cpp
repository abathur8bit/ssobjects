#include <string.h>

#include "generalexception.h"
#include "properties.h"
#include "logs.h"
#include "stringtokenizer.h"

using namespace ssobjects;

Properties::Properties()
{
}

Properties::Properties(const char* filepath)
{
    load(filepath);
}

Properties::~Properties()
{
    deleteProperties();
}

void Properties::load(const char *filepath)
{
    strncpy(m_filepath,filepath,sizeof(m_filepath));
    NULL_TERMINATE(m_filepath,sizeof(m_filepath));

    FILE* fp = fopen(filepath,"r");
    if(!fp)
    {
        char msg[FILENAME_MAX+80];  //room for filename and some error text
        snprintf(msg,sizeof(msg),"File %s not found",filepath);
        NULL_TERMINATE(msg,sizeof(msg));
        throwGeneralException(msg);
    }

    char line[1024];
    char key[1024];
    char value[1024];
    m_numLines = countLines(fp);
    m_keys = new char*[m_numLines];
    m_values = new char*[m_numLines];
    int index = 0;

    while((fgets(line,sizeof(line),fp)) != NULL)
    {
        line[strlen(line)-1]='\0';  //chop the newline at the end that fgets leaves on
//        DUMP(line,strlen(line));

        StringTokenizer tok(line);
        tok.next(key);
        m_keys[index] = new char[strlen(key)+1];
        strcpy(m_keys[index],key);

        tok.next(value);
        m_values[index] = new char[strlen(value)+1];
        strcpy(m_values[index],value);

        ++index;
    }
    fclose(fp);
}

void Properties::reload()
{
    deleteProperties();
    load(m_filepath);
}

void Properties::deleteProperties()
{
    if(m_keys)
    {
        for(int i=0; i<m_numLines; ++i)
        {
            delete m_keys[i];
        }
        delete [] m_keys;
        m_keys=NULL;
    }
    if(m_values)
    {
        for(int i=0; i<m_numLines; ++i)
        {
            delete m_values[i];
        }
        delete [] m_values;
        m_values = NULL;
    }
    m_numLines = 0;
}

int Properties::indexOf(const char *key) const
{
    if(!m_keys)
    {
        for(int i=0; i<m_numLines; ++i)
        {
            if(0==strcmp(m_keys[i],key))
                return i;
        }
    }
    return -1;    //no key found
}

const char* Properties::property(const char *key,const char* defaultValue)
{
    if(m_keys && m_values)
    {
        for(int i=0; i<m_numLines; ++i)
        {
            if(0==strcmp(m_keys[i],key))
                return m_values[i];
        }
    }
    return defaultValue;    //no key found, send the default back
}

/**
 * \brief Set the existing property to the new value.
 * \returns NULL if the key doesn't exist, or a pointer to the new key.
 */
const char* Properties::setProperty(const char* key,const char* value)
{
    if(m_keys && m_values)
    {
        int i = indexOf(key);
        if(-1 == i)
            return NULL; //key not found

        delete m_keys[i];
        m_keys[i] = new char[strlen(value)+1];
        strcpy(m_keys[i],value);
        return m_keys[i];
    }
    return NULL;
}

int Properties::countLines(FILE* fp) const
{
    char line[1024];
    int count = 0;
    while((fgets(line,sizeof(line),fp)) != NULL)
        ++count;
    rewind(fp); //back to beginning of the file
    return count;
}
