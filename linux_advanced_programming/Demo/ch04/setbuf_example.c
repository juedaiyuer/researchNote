/* Example show usage of setbuf() &setvbuf() */
#include<stdio.h>
#include<error.h>
#include<string.h>
int main( int argc , char ** argv )
{
	int i;
	FILE * fp;
	char msg1[]="hello,wolrd\n";
	char msg2[] = "hello\nworld";
	char buf[128];

//open a file and set nobuf(used setbuf).and write string to it,check it before close of flush the stream
	if(( fp = fopen("no_buf1.txt","w")) == NULL)
	{
		perror("file open failure!");
		return(-1);
	}
	setbuf(fp,NULL);
	memset(buf,'\0',128);
	fwrite( msg1 , 7 , 1 , fp );
	printf("test setbuf(no buf)!check no_buf1.txt\n");
	printf("now buf data is :buf=%s\n",buf);

	printf("press enter to continue!\n");
	getchar();
	fclose(fp);


//open a file and set nobuf(used setvbuf).and write string to it,check it before close of flush the stream
	if(( fp = fopen("no_buf2.txt","w")) == NULL)
	{
		perror("file open failure!");
		return(-1);
	}
	setvbuf( fp , NULL, _IONBF , 0 );
	memset(buf,'\0',128);
	fwrite( msg1 , 7 , 1 , fp );
	printf("test setvbuf(no buf)!check no_buf2.txt\n");

	printf("now buf data is :buf=%s\n",buf);

	printf("press enter to continue!\n");
	getchar();
	fclose(fp);

//open a file and set line buf(used setvbuf).and write string(include '\n') to it,
//
//check it before close of flush the stream
	if(( fp = fopen("l_buf.txt","w")) == NULL)
	{
		perror("file open failure!");
		return(-1);
	}
	setvbuf( fp , buf , _IOLBF , sizeof(buf) );
	memset(buf,'\0',128);
	fwrite( msg2 , sizeof(msg2) , 1 , fp );
	printf("test setvbuf(line buf)!check l_buf.txt, because line buf ,only data before enter send to file\n");

	printf("now buf data is :buf=%s\n",buf);
	printf("press enter to continue!\n");
	getchar();
	fclose(fp);

//open a file and set full buf(used setvbuf).and write string to it for 20th time (it is large than the buf)
//check it before close of flush the stream
	if(( fp = fopen("f_buf.txt","w")) == NULL){
		perror("file open failure!");
		return(-1);
	}
	setvbuf( fp , buf , _IOFBF , sizeof(buf) );
	memset(buf,'\0',128);
	fwrite( msg2 , sizeof(msg2) , 1 , fp );
	printf("test setbuf(full buf)!check f_buf.txt\n");
	
	printf("now buf data is :buf=%s\n",buf);
	printf("press enter to continue!\n");
	getchar();

	fclose(fp);
	
}
