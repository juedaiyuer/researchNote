#include <sys/stat.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
int main(void)
{
	int fd;
	FILE *stream;
	unlink("test.txt");
	fd = open("test.txt", O_CREAT|O_WRONLY,S_IREAD | S_IWRITE);
	/* now turn the handle into a stream */
	stream = fdopen(fd, "w");	// you must open the file with O_WRONLY mode
	if (stream == NULL)
		printf("fdopen failed\n");
	else
	{
		fprintf(stream, "Hello world\n");
		fclose(stream);
	}
	printf("the content of the test.txt is:\n");
	system("cat test.txt");
	return 0;
}
