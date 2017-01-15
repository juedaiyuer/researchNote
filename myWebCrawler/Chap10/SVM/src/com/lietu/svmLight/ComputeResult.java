package com.lietu.svmLight;

public class ComputeResult {
	public double	train_error;
	public double	max_difference;
	public double	precision;
	public double	recall;
	public int	train_number;
	public int	support_number;
	public int	test_number;
	public int	train_misclassify;
	public int	test_misclassify;

	public long	kernel_cache_statistic;
}
