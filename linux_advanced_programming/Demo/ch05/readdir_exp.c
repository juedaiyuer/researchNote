#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
int main(int argc,char *argv[])
{
	DIR *dirp;
	struct dirent *dp;
	dirp = opendir(argv[1]);
	while((dp=readdir(dirp))!=NULL)
	{
		if(dp->d_name[0]=='.')
			continue;
		printf("inode=%d\t",dp->d_ino);
		printf("reclen=%d\t",dp->d_reclen);
		printf("name=%s\n",dp->d_name);
	}
	closedir(dirp);
	return 0;
}
