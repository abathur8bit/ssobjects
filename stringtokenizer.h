#ifndef STRINGTOKENIZER_H
#define STRINGTOKENIZER_H

char* parse1(char* src);
char* parse2(char* src);
char* parse3(char* dest,const char* src,int* index);

class StringTokenizer
{
public:
    StringTokenizer();
    char* next(char* dest, const char *source);
    int count(const char *src);

protected:
    int m_index;
};

#endif // STRINGTOKENIZER_H
