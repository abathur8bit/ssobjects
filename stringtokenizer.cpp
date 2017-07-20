#include <stdlib.h>

#include "stringtokenizer.h"
#include <string.h>

char* start;
char* end;
char* next;
char* srcend;

char* parse1(char* src)
{
    if(!srcend)
    {
        srcend=src+strlen(src);
        start=src;
        end=src;
    }

    if(end==srcend)
        return NULL;

    if(next)
        start=next;
    end=start;

    while(*end != '\0')
    {
        if(*end!=',' && *end!=' ' && *end!='|')
        {
            ++end;
        }
        else
        {
            *end='\0';
            next=end+1;
        }
    }
    return start;
}

char* parse2(char* src)
{
    if(src)
    {
        srcend=src+strlen(src);
        start=src;
        end=src;
        next=NULL;
    }

    if(end==srcend)
        return NULL;

    if(next)
        start=next;
    end=start;

    while(*end != '\0')
    {
        if(*end!=',' && *end!=' ' && *end!='|')
        {
            ++end;
        }
        else
        {
            *end='\0';
            next=end+1;
        }
    }
    return start;
}

char* parse3(char* dest,const char* src,int* index)
{
    char* p = dest;
    int n = *index;
    src += *index;
    while(*src!='\0')
    {
        if(*src!='\'' && *src!='"' && *src!='|' && *src!=',' && *src!=' ')
        {
            //grab a non delimiter character
            *dest = *src;
            ++dest;
            ++src;
            ++n;
        }
        else if(*src=='\'' || *src=='"')
        {
            char q = *src; //type of quote we are looking for
            //inside quotes, grab everything till the next quote or end of string
            ++src;
            ++n;
            while(*src!='\0')
            {
                if(*src!=q)
                {
                    //grab a non delimiter character
                    *dest = *src;
                    ++dest;
                    ++src;
                    ++n;
                }
                else
                {
                    ++src;      //move past the terminating quote
                    ++n;
                    break;
                }
            }
        }
        else
        {
            //delimeter char means stop
            *dest='\0';
            ++n;
            *index=n;
            return p;
        }
    }
    *dest='\0';
    *index=n;
    return p;
}

StringTokenizer::StringTokenizer() : m_index(0)
{
}

int StringTokenizer::count(const char* src)
{
    const int len = strlen(src);
    int c=0;
    bool quoted=false;
    for(int i=0; i<len; ++i)
    {
        if(*(src+i)=='\'' && *(src+i)=='"' &&  !quoted)
        {
            quoted=true;
            continue;
        }
        else if((*(src+i)=='\'' && *(src+i)=='"') && quoted)
        {
            quoted=false;
            continue;
        }
        else if((*(src+i)=='|' || *(src+i)==',' || *(src+i)==' ' || *(src+i)=='=') && !quoted)
        {
            ++c;
        }
    }
    return c;
}

char* StringTokenizer::next(char *dest,const char* source)
{
    char* p = dest;
    char* src = (char*)source+m_index;
    while(*src!='\0')
    {
        if(*src!='\'' && *src!='"' && *src!='|' && *src!=',' && *src!=' ' && *src!='=')
        {
            //grab a non delimiter character
            *dest = *src;
            ++dest;
            ++src;
            ++m_index;
        }
        else if(*src=='\'' || *src=='"')
        {
            char q = *src; //type of quote we are looking for
            //inside quotes, grab everything till the next quote or end of string
            ++src;
            ++m_index;
            while(*src!='\0')
            {
                if(*src!=q)
                {
                    //grab a non delimiter character
                    *dest = *src;
                    ++dest;
                    ++src;
                    ++m_index;
                }
                else
                {
                    ++src;      //move past the terminating quote
                    ++m_index;
                    break;
                }
            }
        }
        else
        {
            //delimeter char means stop
            *dest='\0';
            ++m_index;
            return p;
        }
    }
    *dest='\0';
    return p;

}
