#include <iostream>
#include <functional>
#include <vector>
#include <algorithm>
using namespace std;
int main()
{
	vector<int> vec(10, 1);
	int count1 = count_if(vec.begin(), vec.end(), bind2nd(less_equal<int>(), 10));       //求容器中小于等于10的元素个数
	int count2 = count_if(vec.begin(), vec.end(), not1(bind2nd(less_equal<int>(), 10))); //求容器中不小于等于10的元素个数，正好是上面函数的取反
	cout<<count1<<' '<<count2<<endl;  //10 0
	return 0;
}