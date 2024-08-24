package com.board.thymeleaf.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.primitives.Ints;

public class BaseSO extends LinkedHashMap<String,Object> {
	// 사용할 변수명
	public static final String VAR_PAGE = "page";
	public static final String VAR_SIZE = "size";
	public static final String VAR_ORDER = "order";
	public static final String VAR_BY = "by";

  // 미정의시 기본값
	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_SIZE = 10;
	private static final String DEFAULT_ORDER_BY = "desc";

  public BaseSO() {
    init();
  }
  
  public BaseSO(Map<String, Object> map) {
    super(map);
		init();
	}

  private void init() {
		this.put(VAR_PAGE, Optional.ofNullable((String) this.get(VAR_PAGE)).map(Ints::tryParse).orElse(DEFAULT_PAGE));
		this.put(VAR_SIZE, Optional.ofNullable((String) this.get(VAR_SIZE)).map(Ints::tryParse).orElse(DEFAULT_SIZE));
		this.put(VAR_ORDER, Optional.ofNullable((String) this.get(VAR_ORDER)).map(String::trim).orElse(""));
		this.put(VAR_BY, Optional.ofNullable((String) this.get(VAR_BY)).filter(d -> d.equalsIgnoreCase("asc") || d.equalsIgnoreCase("desc")).orElse(DEFAULT_ORDER_BY));
  }

  
}
