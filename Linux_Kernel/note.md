#Linux内核分析#

[Linux汇编语言开发指南](http://www.ibm.com/developerworks/cn/linux/l-assembly/)

学习步骤
1. 网易云课堂 大学计算机专业 Linux内核分析  
>中国科学技术大学 孟宁 
>个人感觉非常不错,而且还提供了比较好玩的实验楼网站,IT在线实训平台,只要有网络,就可以去折腾

[实验楼](https://www.shiyanlou.com/)

---

**IP**

IP 16位  
EIP 32位  
RIP 64位  

IP可以被CALL,RET,JMP,Conditional JMP修改  

ABI 程序与CPU的接口界面  

1. CPU取指令 cs:eip准确定位一个指令  
>R 代表着64位

##内存寻址方式##

**b,w,l,q 代表着8,16,32,64**  

**寄存器寻址**
    movl %eax,%edx	(edx=eax)
>寄存器模式,以%开头的寄存器标识符,register mode  
  
**立即寻址**
    movl $0x123 , %edx (edx=0x123)
>立即数是以$开头的数值,immediate

**直接寻址**
    movl 0x123 , %edx  edx=*(int32_t*)0x123
>直接访问一个指定的内存地址的数据,direct

**间接寻址**
    movl (%ebx),%edx  edx=*(int32_t*)ebx
>将寄存器的值作为一个内存地址来访问内存,indirect

**变址寻址**

    movl 4(%ebx) , %edx edx=*(int32_t*)(ebx+4)
>在间接寻址之时改变寄存器的数值 displaced


##汇编格式##

AT&T Intel 汇编格式略有不同  
Linux内核使用了AT&T汇编格式  

##汇编指令##

    pushl %eax 
    //等同于
    subl $4 ,%esp 
    movl %eax, %esp

    popl %eax 
    //等同于
    movl (%esp) %eax
    addl $4,%esp
    
    call 0x12345 
    //等同于
    pushl %eip(*)
    movl $0x12345 , %eip(*)

    ret 
    //等同于
    popl %eip(*)

	enter
	//等同于
	pushl %ebp
	movl %esp

	leave
	//等同于
	movl %ebp,%esp
	popl %ebp

1. *的代表意义,eip寄存器不能被直接修改,只能通过特殊指令间接修改
2. call,函数调用堆栈是理解C代码在CPU上执行的关键 

**汇编代码分析**
	pushl $8
	movl %esp,%ebp
	subl $4,%esp
	movl $8,(%esp)
	
*语句分析*
1. 将立即数8压栈,esp向下移动
2. ebp位置移到esp位置(ebp=esp)
3. esp-4
4. esp里面的数值赋予立即数8

	pushl $8
	movl %esp,%ebp
	pushl $8



	pushl $8
	movl %esp,%ebp
	pushl %esp
	pushl $8
	addl $4,%esp
	popl %esp
*代码分析*
1. 立即数8压入栈,esp下移
2. ebp位置指向esp位置
3. 将esp寄存器代表的地址压入栈,esp下移
4. 立即数8压入栈,esp下移
5. esp+4,esp上移
6. 弹出存储之前esp寄存器地址,esp上移

---

##寄存器##

1. ebp 栈底指针寄存器 
2. esp 栈顶指针寄存器 
3. eax 保存函数的返回值 
4. cs(代码段寄存器):eip 总是指向下一条的指令地址 
5. call 将当前cs:eip的值压入栈顶,cs:eip指向被调用函数的入口地址
6. ret 从栈顶弹出原来保存在这里的cs:eip的值,放入cs:eip中

[C/C++堆栈指引](http://www.cnblogs.com/Binhua-Liu/archive/2010/08/24/1803095.html)

1. 在32位系统中,堆栈每一个数据单元的大小为4个字节.  
2. ebp在C语言中记录当前函数调用基址

高级语言机制  
1. 存储程序计算机
2. 函数调用堆栈 
3. 中断机制

Linux中,可以使用objdump工具进行反汇编  


	//在64位环境下编译成32位的汇编
	gcc -S -o ccode32.s ccode.c -m32
	//链接时会缺少构建32位可执行程序缺少的包，使用以下指令安装：
	sudo apt-get install libc6-dev-i386
	//编译链接成32位的可执行文件
	gcc -o ccode32 ccode.c -m32


##汇编一段简单的C程序##

**例子程序**
	int g(int x)
	{
	  return x + 3;
	}

	int f(int x)
	{
	  return g(x);
	}

	int main(void)
	{
	  return f(8) + 1;
	}

*经过汇编之后*

	g:
		pushl	%ebp
		movl	%esp, %ebp
		movl	8(%ebp), %eax
		addl	$3, %eax
		popl	%ebp
		ret
	f:
		pushl	%ebp
		movl	%esp, %ebp
		subl	$4, %esp
		movl	8(%ebp), %eax
		movl	%eax, (%esp)
		call	g
		leave
		ret
	main:
		pushl	%ebp
		movl	%esp, %ebp
		subl	$4, %esp
		movl	$8, (%esp)
		call	f
		addl	$1, %eax
		leave
		ret

*代码分析*
1. main函数开始读,一条流程
2. 将ebp当前的寄存器地址压入栈中,esp下移


**MenuOS实验**

qemu:模拟处理器  

qemu -kernel命令

使用gdb跟踪调试内核  
	qemu -kernel linux-3.18.6/arch/x86/boot/bzImage -initrd rootfs.img -s -S # 关于-s和-S选项的说明：
	 -S freeze CPU at startup (use ’c’ to start execution)
	 -s shorthand for -gdb tcp::1234 若不想使用1234端口，则可以使用-gdb tcp:xxxx来取代-s选项

rootfs.img 根文件系统  

init是第一个用户态进程,是1号进程

	（gdb）file linux-3.18.6/vmlinux # 在gdb界面中targe remote之前加载符号表
	（gdb）target remote:1234 # 建立gdb和gdbserver之间的连接,按c 让qemu上的Linux继续运行
	（gdb）break start_kernel # 断点的设置可以在target remote之前，也可以在之后

(gdb) -c 下一步执行



**start_kernel分析**

分析内核的哪一部分都会涉及

位置:init/main.c  

	set_task_stack_end_magic(&init_task)
	init_task即手工创建的PCB,0号进程,最终的idle进程,位置在<arch/x86/kernel/init_task.c>  
	
	trap_init(); //涉及到系统中断

	set_system_trap_gate(); 设置系统陷阱

	mm_init(); 内存管理模块

	sched_init(); 调度

	reset_init();

	ret=run_init_process(); //一号进程

当系统没有进程需要执行时,调度到idle进程  


##计算机启动过程##

>x86 CPU启动的第一个动作CS:EIP=FFFF:0000H（换算为物理地址为000FFFF0H，因为16位CPU有20根地址线)，即BIOS程序的位置。

>BIOS例行程序检测完硬件并完成相应的初始化之后就会寻找可引导介质，找到后把引导程序加载到指定内存区域后，就把控制权交给了引导程序。这里一般是把硬盘的第一个扇区MBR和活动分区的引导程序加载到内存（即加载BootLoader),加载完整后把控制权交给BootLoader。

>引导程序BootLoader开始负责操作系统初始化，然后起动操作系统。启动操作系统时一般会指定kernel、initrd和root所在的分区和目录，比如root (hd0,0)，kernel (hd0,0)/bzImage root=/dev/ram init=/bin/ash，initrd (hd0,0)/myinitrd4M.img

>内核启动过程包括start_kernel之前和之后，之前全部是做初始化的汇编指令，之后开始C代码的操作系统初始化，最后执行第一个用户态进程init。

>一般分两阶段启动，先是利用initrd的内存文件系统，然后切换到硬盘文件系统继续启动。initrd文件的功能主要有两个：1、提供开机必需的但kernel文件(即vmlinuz)没有提供的驱动模块(modules) 2、负责加载硬盘上的根文件系统并执行其中的/sbin/init程序进而将开机过程持续下去

>道生一（start_kernel....cpu_idle），一生二（kernel_init和kthreadd），二生三（即前面0、1和2三个进程），三生万物（1号进程是所有用户态进程的祖先，2号进程是所有内核线程的祖先），新内核的核心代码已经优化的相当干净，都符合中国传统文化精神了。

这里大神阮一峰写了一个博文  
[计算机是如何启动的](http://www.ruanyifeng.com/blog/2013/02/booting.html)  
[Linux 的启动流程](http://www.ruanyifeng.com/blog/2013/08/linux_boot_process.html)  
[linux的kernel的入口之--start_kernel分析](http://blog.csdn.net/wh_19910525/article/details/17762625)  








