#include <stdio.h>
#include <malloc.h>
#include <unistd.h>
#include <alloca.h>

extern void afunc(void);
extern etext,edata,end;

int bss_var;				//no init globel data must be in bss

int data_var=42;			//init globel data must be in data

#define SHW_ADR(ID,I) printf("the %8s\t is at adr:%8x\n",ID,&I);		//the macro to printf the addr

int main(int argc,char *argv[])
{
	char *p,*b,*nb;

	printf("Adr etext:%8x\t Adr edata %8x\t Adr end %8x\t\n",&etext,&edata,&end);
	
	printf("\ntext Location:\n");
	SHW_ADR("main",main);			//text section function
	SHW_ADR("afunc",afunc);			//text section function

	printf("\nbss Location:\n");
	SHW_ADR("bss_var",bss_var);		//bss section var

	printf("\ndata location:\n");
	SHW_ADR("data_var",data_var);	//data section var
	
	
	printf("\nStack Locations:\n");
	afunc();
	
	p=(char *)alloca(32);			//alloc memory from statck
	if(p!=NULL)
	{
		SHW_ADR("start",p);
		SHW_ADR("end",p+31);
	}
	
	b=(char *)malloc(32*sizeof(char));	//malloc memory from heap
	nb=(char *)malloc(16*sizeof(char));
	
	printf("\nHeap Locations:\n");	
	printf("the Heap start: %p\n",b);
	printf("the Heap end:%p\n",(nb+16*sizeof(char)));
	printf("\nb and nb in Stack\n");
	SHW_ADR("b",b);
	SHW_ADR("nb",nb);
	free(b);
	free(nb);
}


void afunc(void)
{
	static int long level=0;	//data section static var
	int	 stack_var;				//temp var ,in stack section
	if(++level==5)
	{
		return;
	}
	SHW_ADR("stack_var in stack section",stack_var);
	SHW_ADR("Level in data section",level);
	afunc();
}

