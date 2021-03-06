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


StringTokenizer::StringTokenizer(const char* src) : m_index(0),m_source(src)
{
}

int StringTokenizer::count()
{
    int len = strlen(m_source);
    if(!len)
        return 0;

    char* buff = (char*)calloc(len,sizeof(char));   //heap not stack, to avoid stack overflows
    int hold=m_index;
    int count=0;
    do
    {
        next(buff);
        len=strlen(buff);
        if(len)
            ++count;
    } while(len);

    free(buff);
    m_index=hold;
    return count;
}

char* StringTokenizer::next(char *dest)
{
    char* p = dest;
    char* src = (char*)m_source+m_index;
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
