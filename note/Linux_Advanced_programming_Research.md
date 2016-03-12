#Linux高级程序设计#

1. [Linux高级程序设计](http://study.163.com/course/courseMain.htm?courseId=490007)

**课程讲师的网页地址**

[守望者](http://watchmen.cn/portal.php)

---

工具:sourceinsight

**Linux下编辑调试工具**
gcc gdb  

**四个步骤**
1. 预处理
  去掉注释,进行宏替换(#define相关),头文件(#include)包含  
  gcc -E ...

2. 编译 
  编译成汇编语言,基于不同的平台,X86,ARM...  
  gcc -S ...

3. 汇编
  将汇编语言翻译成二进制的目标代码  
  gcc -c ...

4. 链接
  包含各函数库的入口,得到可执行代码  
  gcc -o ...  


Linux的C源代码文件后缀.c  
.C C++  
优化:目标是使代码性能更优,去掉冗余代码,工具自动完成,-O 0/1/2/3  

gcc -g 生成调试信息  

gdb  
l是list的意思,列出各行编号  
b 行号 设置断点  
n是下一步  
c是continue 下一个断点  
p 变量名 打印这个变量的值  

**库的使用**
系统定义的头文件
/usr/include
/usr/local/include
/usr/target/include

库文件 /lib  
ldd file(可执行文件) 可以列出此文件使用了哪些库文件  

math库  
编译时默认链接c库,如果要使用其它的库,编译时要用-l  

系统限制  
limit.h float.h(数据类型限制)  
本身限制:资源有限,不可能无限制的申请资源   
命令行:ulimit来修改和获取  
编程:getrlimit函数来获取,setrlimit来设置系统的限制  

RLIMIT_CORE :core文件的最大字节数,core文件是系统某个进程出现异常退出时,系统为其保存的上下文信息,在调试程序时经常要用  
RLIMIT_CPU:CPU时间的最大值(s)  
RLIMIT_DATA:一个进程数据段的最大字节数  
RLIMIT_FSIZE:可创建文件的最大值  
RLIMIT_NOFILE:每个进程可以打开的文件的个数  
RLIMIT_STACK:进程栈空间的最大值  
RLIMIT_VMEN:虚拟地址空间的最大值  
RLIMIT_AS:系统进程可用内存空间最大值  

##命令行参数##

**main函数的参数问题**
argc:参数的个数  
argv[]:指针数组,存放具体的参数列表  

测试代码:
	
	#include<stdio.h>
	int main(int argc,char* argv[]){
		int i;
		for(i=0;i<argc;i++)
			printf("argv[%d]=%s\n",i,argv[i]);
		return 0;
	}


main函数提取命令行参数列表  
getopt  
getlongopt  

	#include<unistd.h>
	int getopt(int argc,char* const argv[],const char* optstring);


##内存管理##

###进程地址空间###
*可执行文件*

查看文件的结构:size file

*三个部分*

1. 代码段 主要存放指令,操作,只读常量
2. 数据段 全局,静态,二者初始化过的
3. BSS段 未初始化的变量


程序存在磁盘上(读取慢),运行时加载到内存中(读取快,CPU可直接访问读写)  
进程:最小的资源管理单元,一个程序运行,必然为其创建一个进程,进程是有生命周期的,一个进程是执行的程序段.当然一个程序也可有创建多个进程.  

内核中的资源:  
PCB进程控制块,结构体task struct,负责管理进程的所有资源  
*成员* 
mm_struct 指向相关的内存资源

1. 代码段,数据段,BSS段,直接从磁盘拷贝到当前内存空间,大小相等
2. 动态空间 堆栈,mmap段(映射其它的库的相关信息)

**进程所在的目录 /proc**  
ps -aux | grep file(可执行文件名)  

进程的内存地址信息列表  

1. cat /proc/进程id/maps 
2. pmap 

地址实际并不是真正的物理地址,而是虚拟地址空间,用于对资源的保护. 内存资源是宝贵的,执行一个程序,并不需要将所有内存资源加载到内存中,采用写时申请技术.  

*好处*

1. 保护系统,用户程序非法访问不能造成内核崩溃,段错误出现时,非法访问进程自动退出
2. 节约资源,采用内存映射,一个程序执行时,不是将所有资源都加载到内存中,而是使用缺页真正申请物理空间.

32位平台,一个进程拥有自己的4G的虚拟地址空间

###虚拟地址空间布局###

**进程执行时,如何申请内存空间,申请哪些空间**























