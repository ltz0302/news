package com.ltz.news.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 
 * @Title: PagedGridResult.java
 * @Description: 用来返回分页Grid的数据格式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedGridResult {
	
	private int page;			// 当前页数
	private long total;			// 总页数
	private long records;		// 总记录数
	private List<?> rows;		// 每行显示的内容

}
