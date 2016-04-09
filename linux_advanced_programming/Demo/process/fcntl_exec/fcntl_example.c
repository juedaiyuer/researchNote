#include<stdio.h>
#include<unistd.h>
#include<sys/types.h>
#include<fcntl.h>
#include<string.h>
#include<stdlib.h>

/**
	1.int sprintf(char *str, const char *format, ...);
	2.执行新代码对打开文件的处理 

*/

int main(int argc,char *argv[])
{
	int fd,status;
	pid_t pid;
	fd=open("test.txt",O_RDWR|O_APPEND|O_CREAT,0644);
	if(fd==-1)
	{
		perror("open");
		exit(EXIT_FAILURE);
	}
	
	//fcntl(fd,F_SETFD,FD_CLOEXEC); //include this ,will error
	printf("befor child process write\n");
	system("cat test.txt");
	
	if((pid=fork())==-1)
	{
		perror("fork");
		exit(EXIT_FAILURE);
	}
	if(pid==0)
	{	
		char buf[128];
		sprintf(buf,"%d",fd);  //fd已参数的形式传递给新代码,fd->buf->atoi->fd
		execl("./newcode","newcode",buf,(char *)0);  
	}
	else
	{
		wait(&status);    //父进程等待子进程执行完毕
		printf("after child_process write\n");
		system("cat test.txt");
	}
}
