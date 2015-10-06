#include <iostream>
using namespace std;

/*
	1.调整数组顺序使奇数位于偶数前面（数组）
    2.使得所有奇数位于数组的前半部分,所有偶数位于数组的后半部分
	3.要求时间复杂度为O(n)
*/

//函数对象
template<class T>
struct IsOdd
{
	bool operator() (T x){
		return (x & 1)?true: false;
	}
};
//函数功能 ： 调整数组顺序使奇数位于偶数前面  
//函数参数 ： pArray指向数组的指针，nLen为数组长度  
//返回值 ：   无  
template <class T, class F>
void PartionArray(T *pArray, int nLen, F func)
{  
    int i = -1;  
    for(int j = 0; j < nLen; j++)  
    {  
        if(func(pArray[j])) //满足调整条件  
        {  
            i++;  
            T tmp = pArray[j];  
            pArray[j] = pArray[i];  
            pArray[i] = tmp;  
        }  
    }  
}  
//测试用例
int main()
{
	short a[] = {1,2,3,4,5,6};
	long b[] = {1,2,3,4,5,6};

	PartionArray(a, 6, IsOdd<short>()); //测试代码
	for(int i=0;i<6;i++)
	{
		printf("%d\n",a[i]);
	}

	cout<<"-----------"<<endl;

	PartionArray(b, 6, IsOdd<long>()); //测试代码
	for(int j=0;j<6;j++)
	{
		printf("%d\n",a[j]);
	}

	return 0;
}