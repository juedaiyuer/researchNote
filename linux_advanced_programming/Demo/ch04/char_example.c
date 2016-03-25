#include<stdio.h>
int main(int argc,char *argv[])
{
    FILE *fp=NULL;
    char ch;
    if(argc<=1)
    {
        printf("check usage of %s \n",argv[0]);
        return -1;
    }
if((fp=fopen(argv[1],"r"))==NULL)			//以只读形式打开argv[1]所指明的文件
    {
        printf("can not open %s\n",argv[1]);
        return -1;
    }
    while ((ch=fgetc(fp))!=EOF)			//把已打开的文件中的数据逐字节的输出到标准输出stdout
        fputc(ch,stdout);
    fclose(fp);						//关闭文件
    return 0;
}
