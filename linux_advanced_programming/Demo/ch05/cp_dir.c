#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <dirent.h>
#include <linux/types.h>
#include <fcntl.h>
#include <errno.h>

/*cp the link_file's src to dst*/
int cp_file(const char *src, const char *dst,mode_t mode)
{
	int fd_src,fd_dst;
	if(-1 == (fd_src =open(src,O_RDONLY)))
	{
		perror("open src");exit(EXIT_FAILURE);
	}
	
	if(-1 == (fd_dst =open(dst,O_WRONLY|O_TRUNC|O_CREAT,mode)))
	{
			perror("open dst");exit(EXIT_FAILURE);
	}
	
	int len=0;
	do
	{
		char buf[1024];
		len=read(fd_src,buf,1024);
		write(fd_dst,buf,len);
	}while(len>0);
	
	close(fd_src);
	close(fd_dst);
}


int cp_dir(const char *src,const char *dst)
{
	DIR *dirp = NULL;
	
	if(NULL== (dirp=opendir(src)))
	{
		perror("opendir");exit(EXIT_FAILURE);
	}	
	
	struct dirent *entp = NULL;
	while ( NULL != (entp =readdir(dirp)))
	{
		if (strcmp(entp->d_name, "..")==0 || strcmp(entp->d_name, ".")==0)	//ignore ./ ../
        {
          continue;
        }
		
		char *name_src =(char *) malloc( strlen(src) + 1 + strlen(entp->d_name) + 1 );
		sprintf(name_src,"%s/%s\0",src,entp->d_name);
		char *name_dst =(char *) malloc( strlen(dst) + 1 + strlen(entp->d_name) + 1 );
		sprintf(name_dst,"%s/%s\0",dst,entp->d_name);

		struct stat stat_src;
		if (stat(name_src, &stat_src) == -1) 
		{
			fprintf(stderr, "%s(%d): stat error(%s)!\n", __FILE__, __LINE__, strerror(errno));
			exit(EXIT_FAILURE);
		}	
	
		if (S_ISREG(stat_src.st_mode))		//regular file
		{
			cp_file(name_src,name_dst,stat_src.st_mode);
			free(name_src);
			free(name_dst);
		}	
		else if ( S_ISDIR( stat_src.st_mode ))
		{		
			if( -1 ==mkdir(name_dst,stat_src.st_mode))
			{
				perror("mkdir");exit(EXIT_FAILURE);
			}
			cp_dir( name_src, name_dst);		
			free(name_src);
			free(name_dst);
		}
	}
}


int main(int argc,char *argv[])
{
	if (argc < 3)
	{
		fprintf(stderr,"usage %s src_dir dst_src\n",argv[0]);exit(EXIT_FAILURE);
	}
	
	struct stat stat_src;
	if (stat(argv[1], &stat_src) != 0) 
	{
      fprintf(stderr, "%s(%d): stat error(%s)!\n", __FILE__, __LINE__, strerror(errno));
      exit(EXIT_FAILURE);
    }	
	
	umask(0000);
	if (S_ISREG(stat_src.st_mode))		//regular file
    {
		struct stat stat_dst;
		if (stat(argv[2], &stat_dst) == -1) 
		{
			if(errno != ENOENT)			//if errno not cause by file/dir not exist
			{
				fprintf(stderr, "%s(%d): stat error(%s)!\n", __FILE__, __LINE__, strerror(errno));
				exit(EXIT_FAILURE);
			}
			else						//if dst_flie not exist.
			{
				cp_file(argv[1],argv[2],stat_src.st_mode);
			}
		}	
		else		//dst file exist.
		{
			if(S_ISDIR(stat_dst.st_mode))	//cp a file to a exist dir
			{				
				char *ptr=(char *)malloc(strlen(argv[2])+1+strlen(argv[1])+1);
				sprintf(ptr,"%s/%s\0",argv[2],argv[1]);			
				cp_file(argv[1],ptr,stat_src.st_mode);
			}
			else						//cp file to a exist file
			{
				printf("file %s exist, do you want overwrite it[y/n]:",argv[2]);
				char ch;
				while(!scanf("%c",&ch))
				{
					getchar();
				}
				if(ch =='Y' || ch == 'y' )
				{
					unlink(argv[2]);
					cp_file(argv[1],argv[2],stat_src.st_mode);	
				}
				else
					return 1;	
			}
		}
    }	
	
	else if (S_ISDIR(stat_src.st_mode))	//dir
    {
		struct stat stat_dst;
		if (stat(argv[2], &stat_dst) == -1) 
		{
			if(errno != ENOENT)			//if errno not cause by file/dir not exist
			{
				fprintf(stderr, "%s(%d): stat error(%s)!\n", __FILE__, __LINE__, strerror(errno));
				exit(EXIT_FAILURE);
			}
			else						//file/dir not exist
			{
				errno=0;
				if(-1 == mkdir(argv[2],stat_src.st_mode))
				{
					perror("mkdir");exit(EXIT_FAILURE);
				}
				cp_dir(argv[1],argv[2]);
			}
		}
		else if(S_ISREG(stat_dst.st_mode))		//can't copy a dir to a file
		{
			fprintf(stderr,"can't copy a dir to a file\n");exit(EXIT_FAILURE);
		}
		else								//copy a dir to a exsit dir ,
		{			
			char *ptr=(char *)malloc(strlen(argv[1])+1+strlen(argv[2])+1);
			sprintf(ptr,"%s/%s\0",argv[2],argv[1]);
			if(-1 == mkdir(ptr,stat_src.st_mode))
			{
				perror("mkdir");exit(EXIT_FAILURE);
			}			
			cp_dir(argv[1],ptr);
			free(ptr);
		}
	}
	
}	

