#include<stdio.h>
int main(int argc,char *argv[])
{
    struct student
    {
        char name[10];
        int number;
    };
    FILE *fp=NULL;
    struct student student[1],*qq;
if((fp=fopen("aa.txt","r"))==NULL)
    {
        printf("can not open file!\n");
return -1;
    }
    fseek(fp,sizeof(struct student),0); //定位到第二个结构体
    fread(student,sizeof(struct student),1,fp); // 将第二个结构体的内容写student中 
    printf("name\t\t number\n");
    qq=student;
    printf("%s\t\t %d\n",qq->name,qq->number); //输出student中的内容
    fclose(fp); //关闭文件流
    return 0;
}
