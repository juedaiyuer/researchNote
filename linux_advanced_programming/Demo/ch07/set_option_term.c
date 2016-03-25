#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int main(int argc,char *argv[])
{
	int fd,c, res;
	struct termios oldtio,newtio;
	char buf[255];
	fd = open(argv[1], O_RDWR | O_NOCTTY ); // O_NOCTTY不能被ctrl+c中止
	if (fd <0) {
		perror("open"); exit(EXIT_FAILURE); 
	}
	memset(&newtio,'\0', sizeof(newtio));
	newtio.c_cflag = B38400  | CS8 | CLOCAL | CREAD;//设置波特率，数据位，使能读
	newtio.c_iflag = IGNPAR | ICRNL;//忽略奇偶校验，映射CR
 	newtio.c_oflag = 0;		//输出模式为RAW模式
  	newtio.c_lflag = ICANON;//本地模式，不回显
	tcflush(fd, TCIFLUSH);	//刷新
 	tcsetattr(fd,TCSANOW,&newtio);	//设置属性

 	while (1) {
  	  	res = read(fd,buf,255); 	//从该终端读数据，如果是/dev/tty，即当前终端，遇到CR结束
 	   	buf[res]=0;           //最后一个设置为结束符
  	  	printf(":recv %d bytes:%s\n\r", res,buf);	//打印输出字符数
    	if (buf[0]=='E')		//只有第一个字符为E时，才结束 
		break;
 	}
	tcsetattr(fd,TCSANOW,&oldtio);
}
