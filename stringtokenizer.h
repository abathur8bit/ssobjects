#ifndef STRINGTOKENIZER_H
#define STRINGTOKENIZER_H

char* parse1(char* src);
char* parse2(char* src);
char* parse3(char* dest,const char* src,int* index);

class StringTokenizer
{
public:
    StringTokenizer(const char* src);
    char* next(char* dest);
    int count();

protected:
    int m_index;
    const char* m_source;
};

#endif // STRINGTOKENIZER_H
