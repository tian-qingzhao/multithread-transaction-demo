package com.tqz.multithread.transaction.demo.service;

import com.tqz.multithread.transaction.demo.entity.Employee;

import java.sql.SQLException;
import java.util.List;

public interface EmployeeService {

    /**
     * 新增并且更新
     *
     * @throws SQLException sql异常
     */
    void insertAndUpdate() throws SQLException;

    /**
     * 事务在多线程异常的场景下，不会回滚
     *
     * @param employeeList 员工集合
     * @throws SQLException sql异常
     */
    void saveThread1(List<Employee> employeeList) throws SQLException;

    /**
     * 事务在多线程异常的场景下，会回滚
     *
     * @param employeeList 员工集合
     * @throws SQLException sql异常
     */
    void saveThread2(List<Employee> employeeList) throws SQLException;

    /**
     * 事务在多线程场景正常的情况下，正常操作数据库
     *
     * @param employeeList 员工集合
     * @throws SQLException sql异常
     */
    void saveThread3(List<Employee> employeeList) throws SQLException;
}