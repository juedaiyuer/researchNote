#include <iostream>
#include <algorithm>
#include <functional>
/*
	1.排序算法sort()为递增排序
*/
using namespace std;
int main()
{
	int a[] = {1,3,2,4,5,7};
	sort(&a[0], &a[6]);         //递增排序
	for(int i = 0; i < 6; i++)
		cout<<a[i]<<' ';
	cout<<endl;
/*
	1.实现递减排序
	2.函数实参增加了函数对象
	3.头文件添加了 functional
*/
	sort(&a[0], &a[6], greater<int>()); 
	for(int j = 0; j < 6; j++)  
        cout<<a[j]<<' ';  
    cout<<endl; 

	return 0;
}