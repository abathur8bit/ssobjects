#ifndef PROPERTIES_H
#define PROPERTIES_H

#include <limits.h>
#include <stdio.h>

#include "linkedlist.h"

namespace ssobjects
{

class Properties
{
public:
    Properties();
    Properties(const char *filepath);
    ~Properties();
    const char* property(const char* key,const char* defaultValue);
    const char* setProperty(const char* key,const char* value);
    int indexOf(const char *key) const;
    void deleteProperties();
    void load(const char* filepath);
    void reload();
    int countLines(FILE* fp) const;

protected:
    char m_filepath[FILENAME_MAX];
    int m_numLines;
    char** m_keys;
    char** m_values;
};

};

#endif // PROPERTIES_H
