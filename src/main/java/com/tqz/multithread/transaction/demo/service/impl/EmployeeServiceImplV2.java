package com.tqz.multithread.transaction.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tqz.multithread.transaction.demo.entity.Employee;
import com.tqz.multithread.transaction.demo.mapper.EmployeeMapper;
import com.tqz.multithread.transaction.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 编程式事务的使用方式
 *
 * @author tianqingzhao
 * @since 2023/12/15 13:56
 */
@Service
public class EmployeeServiceImplV2 extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Override
    public void insertAndUpdate() throws SQLException {
        Employee employee = new Employee(1, "test1", new Date());
        Employee employee2 = new Employee(1, "test2", new Date());

        // 编程式事务方式一，新建一个TransactionTemplate
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(platformTransactionManager);
      /*  Object result = transactionTemplate.execute(
                status -> {
                    try {
                        baseMapper.insert(employee);

                        // int i = 1 / 0;

                        return baseMapper.updateById(employee2);
                    } catch (Exception e) {
                        status.setRollbackOnly();

                        throw new IllegalArgumentException("执行数据库操作失败");
                    }
                });*/ // 执行execute方法进行事务管理

        // 编程式事务方式一，定义一个某个框架平台的TransactionManager，如JDBC、Hibernate
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        // 设置数据源
        dataSourceTransactionManager.setDataSource(jdbcTemplate.getDataSource());
        // 定义事务属性
        DefaultTransactionDefinition transDef = new DefaultTransactionDefinition();
        // 设置传播行为属性
        transDef.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        // 获得事务状态
        TransactionStatus status = dataSourceTransactionManager.getTransaction(transDef);
        try {
            // 数据库操作，执行业务
            baseMapper.insert(employee);

            // int i = 1 / 0;

            baseMapper.updateById(employee2);

            // 提交
            dataSourceTransactionManager.commit(status);
        } catch (Exception e) {
            // 回滚
            dataSourceTransactionManager.rollback(status);
        }

    }

    @Override
    public void saveThread1(List<Employee> employeeList) throws SQLException {

    }

    @Override
    public void saveThread2(List<Employee> employeeList) throws SQLException {

    }

    @Override
    public void saveThread3(List<Employee> employeeList) throws SQLException {

    }
}
