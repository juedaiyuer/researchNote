#include<stdio.h>
int main(int argc,char *argv[])
{
    FILE *fp=NULL;
    char str[10];
    if((fp=fopen(argv[1],"r"))==NULL) 			//以只读形式打开文件aa.txt
    {
        printf("can not open!\n");
        return -1;
    }
    fgets(str,sizeof(str),fp);						//从打开的文件中读取sizeof(str)个字节到str
    fputs(str,stdout);							//将str中的内容逐行输出到标准输出
    fclose(fp);								//关闭已打开的文件
    return 0;
}
