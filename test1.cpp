#include <iostream>
#include <algorithm>
#include <vector> 
#include <functional>

using namespace std;

int main()
{
//	<span style="font-size:18px;">    
	vector<int> V(100);
    generate(V.begin(), V.end(), rand);//</span>
	for(int i=0;i<100;i++)      //这种输出感觉为最差最小版本的“汽车”
	{
		printf("%d\n",V[i]);
	}

	return 0;
}