#ifndef HTMLHEADER_H
#define HTMLHEADER_H

#include <list>
#include <string>

using namespace std;

class Header
{
public:
    Header(const char* key,const char* value);
    const char* key() {return m_key.c_str();}
    const char* value() {return m_value.c_str();}

protected:
    string m_key;
    string m_value;
};

class HtmlHeaders
{
public:
    HtmlHeaders();
    virtual ~HtmlHeaders();
    void add(const char* key,const char* value);
    void add(string& key,string& value);
    Header* find(const char* key);
    int size() {return m_headers.size();}

protected:
    list<Header*> m_headers;
};

#endif // HTMLHEADER_H
