package com.mikitarudy.dao;

import java.util.List;

public interface CrudDAO<T> {
    T save (T t);
    List<T> findAll ();
}
