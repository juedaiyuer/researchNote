#include <iostream>
#include <vector>
using namespace std;
/*
	1.修改了test4的代码,减少操作对象，方便自己的理解
*/

//count_if函数
template <class InputIter, class Predicate, class Size>
void count_if(InputIter first, InputIter last, Predicate pred, Size& n) {
	for ( ; first != last; ++first)
		if (pred(*first))
			++n;
}
//用来定义一元操作的参数类别和返回值类别
template <class Arg, class Result>
struct unary_function {
	typedef Arg argument_type;
	typedef Result result_type;
};
//用来定义二元操作的参数类别和返回值类别
template <class Arg1, class Arg2, class Result>
struct binary_function {
	typedef Arg1 first_argument_type;
	typedef Arg2 second_argument_type;
	typedef Result result_type;
};
//本测试之用到关系函数对象
template <class T>
struct less_equal : public binary_function<T, T, bool> {
	bool operator()(const T& x, const T& y) const { return x <= y; }
};
//绑定第二个参数
template <class Operation> 
class binder2nd: public unary_function<typename Operation::first_argument_type,typename Operation::result_type> {
public:
	binder2nd(const Operation& x, const typename Operation::second_argument_type& y) : op(x), value(y) { cout<<"binder2nd Constructor"<<endl; }
	typename Operation::result_type operator()(const typename Operation::first_argument_type& x) const {
		cout<<"binder2nd's operator()"<<endl;
		return op(x, value);  //固定第二个参数
	}
protected:
	Operation op;
	typename Operation::second_argument_type value;
};
//外部接口
template <class Operation, class T>
inline binder2nd<Operation> bind2nd(const Operation& fn, const T& x) {
	typedef typename Operation::second_argument_type Arg2_type;
	return binder2nd<Operation>(fn, Arg2_type(x));
}
//一元操作求反
template <class Predicate>
class unary_negate: public unary_function<typename Predicate::argument_type, bool> {
protected:
	Predicate pred;
public:
	explicit unary_negate(const Predicate& x) : pred(x) { cout<<"unary_negate Constructor"<<endl; }
	bool operator()(const typename Predicate::argument_type& x) const {
	cout<<"unary_negate's operator()"<<endl;
    return !pred(x);
  }
};
//外部接口
template <class Predicate>
inline unary_negate<Predicate> not1(const Predicate& pred)
{
	return unary_negate<Predicate>(pred);
}
//测试程序
int main()
{
	vector<int> vec(10, 1);
	int count1 = 0, count2 = 0;
	count_if(vec.begin(), vec.end(), bind2nd(less_equal<int>(), 10),count1);       //求容器中小于等于10的元素个数
//	count_if(vec.begin(), vec.end(), not1(bind2nd(less_equal<int>(), 10)),count2); //求容器中不小于等于10的元素个数，正好是上面函数的取反
//	cout<<count1<<' '<<count2<<endl;  //10 0

	cout<<count1<<endl;

	return 0;
}