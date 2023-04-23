package com.bupt.zhidian.dao;

import com.bupt.zhidian.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchDao extends JpaRepository<Stock, Integer> {
    @Query(name = "findByName", nativeQuery = true, value = "select * from stocks where name LIKE %:name%")
    List<Stock> findByName(@Param("name") String name);
    @Query(name = "findByCode", nativeQuery = true, value = "select * from stocks where code LIKE %:code%")
    List<Stock> findByCode(@Param("code") String code);
}

