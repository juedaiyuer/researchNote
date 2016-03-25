#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
int main(int argc, char **argv)
{
	int fd;		//file descriptor
	int length;
    char *mapped_mem;	//存储页大小
	int pagesize=0;
	pagesize=getpagesize();  //获取页大小
    fd = open(argv[1], O_RDWR | O_CREAT, S_IRUSR | S_IWUSR);	//打开一个文件
	length=lseek(fd, 0, SEEK_END);	//到达文件结束，获取文件大小
	lseek(fd,(pagesize*2-length%pagesize-1),SEEK_END);	//在后面添加空闲空间，大小为1~2倍的页大小
	write(fd,"-1",1);	//写一个内容，否则mmap不到添加的空空间。
    mapped_mem = mmap(NULL, length/pagesize+2, PROT_READ|PROT_WRITE,MAP_SHARED,fd, 0);

	
    printf("\npls input info you insert(size<%d):",pagesize);//要求输入内容，内容量小于1个页
	char buf[pagesize];
	fgets(buf,pagesize,stdin);
	printf("\npls input info you insert local( <filesize %d):",length); //要求添加内容的位置  
	int local=0;
    scanf("%d",&local);
	
	memmove(mapped_mem+local+strlen(buf),mapped_mem+local,length-local);//移动要添加位置后面的所有信息
	memcpy(mapped_mem+local,buf,strlen(buf)-1);		//将添加信息加入
	msync(mapped_mem,length/pagesize+2,MS_SYNC|MS_INVALIDATE);	//要求回写磁盘
    munmap(mapped_mem, length/pagesize+2);
	ftruncate(fd,length+strlen(buf));	//截短文件，将多余的空间删除掉
	//退出后，直接在终端查看文件内容更新情况。　  
    return 0;
}

 
