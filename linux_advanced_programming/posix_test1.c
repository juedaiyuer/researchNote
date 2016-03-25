#include<fcntl.h>
#include<sys/types.h>
#include<stdio.h>
#include<stdlib.h>

// cp src -> dst
// argv[1] src
// argv[2] dst
int main(int argc,char* argv[])
{
	if(argc !=3)
	{
		printf("please input message format as :%s src_file dst_file\n",argv[0]);
		exit(EXIT_FAILURE);
	}
	
	int fd_src,fd_dst;
	fd_src = open(argv[1],O_RDONLY);
	if(-1 == fd_src)
	{
		perror("open");
		exit(EXIT_FAILURE);
	}
	printf("fd_src:%d\n",fd_src);
	
	fd_dst = open(argv[2],O_WRONLY|O_CREAT,0664);
	if(-1 == fd_dst)
        {
                perror("open");
                exit(EXIT_FAILURE);
        }
	
	//内存空间,并且初始化
	char buf[1024];
	int ret = 0;
	
	while(1)
	{
		memset(buf,'\0',1024);

		ret = read(fd_src,buf,1024); //src文件读入内存,返回读入的字节数
		
		if(ret == -1)
		{
			perror("read");
			exit(EXIT_FAILURE);
		}
		else if(ret == 0)
			break;
		else
			write(fd_dst,buf,ret);
	}
	
	close(fd_src);
	close(fd_dst);
}
