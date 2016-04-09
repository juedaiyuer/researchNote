#include <sys/types.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>

/**
	1.父子进程对文件描述符的文件表项信息(文件读写位置)共享使用
	2.例子程序,父子进程对一个文件进行写操作,结果写操作都是在同一文件,且写入数据不交叉覆盖,说明父子进程共享文件偏移,因此共享文件表项
*/
int main(int argc,char *argv[])
{
    pid_t pid;
    int fd;
    int i=1;
    int status;
    char *ch1="hello";
    char *ch2="world";
    char *ch3="IN";
    if((fd=open("test.txt",O_RDWR|O_CREAT,0644))==-1)
    {
	perror("parent open");
	exit(EXIT_FAILURE);
    }
    if(write(fd,ch1,strlen(ch1))==-1)
    {	 
	perror("parent write");
	exit(EXIT_FAILURE);
    }
    
    if((pid=fork())==-1)
    {
	perror("fork");
	exit(EXIT_FAILURE);
    }
    else if(pid==0)
    {
	i=2;	
	printf("in child\n");
	printf("i=%d\n",i);
	if(write(fd,ch2,strlen(ch2))==-1)
	    perror("child write");
	return 0;
    }
    else
    {
	sleep(1);
	printf("in parent\n");
	printf("i=%d\n",i);
	if(write(fd,ch3,strlen(ch3))==-1)
	    perror("parent,write");
	wait(&status);
	return 0;
    }
}
