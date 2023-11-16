package com.tqz.multithread.transaction.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tqz.multithread.transaction.demo.config.ExecutorConfig;
import com.tqz.multithread.transaction.demo.config.SqlContext;
import com.tqz.multithread.transaction.demo.entity.Employee;
import com.tqz.multithread.transaction.demo.exception.ServiceException;
import com.tqz.multithread.transaction.demo.mapper.EmployeeMapper;
import com.tqz.multithread.transaction.demo.service.EmployeeService;
import com.tqz.multithread.transaction.demo.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在有事务的方法中，有时候我们需要对这个方法进行性能优化，性能优化的措施采用多线程并发处理，
 * 不同的线程，在mybatis中创建的sqlSession不同，导致多线程之间同一时刻不能使用同一个connection（数据库的session），
 * 通过@Transactional实现事务处理会导致多线程执行的sql不会同时进行事务的回滚。
 *
 * <p>如下是connection，transation,sqlSession之间的关系：
 * 链接可以通过数据库链接池被复用。在MyBatis中，不同时刻的SqlSession可以复用同一个Connection，
 * 同一个SqlSession中可以提交多个事务。因此，链接—会话—事务的关系如下：
 * connection ---1:N---> SqlSession ---1:N---> Transaction
 *
 * @author tianqingzhao
 * @since 2023/11/16 21:03
 */
@Slf4j
@Service("employeeService")
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private SqlContext sqlContext;

    /**
     * 共用一个sqlSession，则共用一个connection。
     */
    @Override
    public void insertAndUpdate() throws SQLException {
        SqlSession sqlSession = sqlContext.getSqlSession();
        Connection connection = sqlSession.getConnection();
        EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
        try {
            // 设置手动提交
            connection.setAutoCommit(false);
            employeeMapper.insert(new Employee(1, "test", new Date()));
            List<Future<Integer>> futureList = new ArrayList<>();

            futureList.add(ExecutorConfig.getThreadPool().submit(
                    () -> employeeMapper.updateById(new Employee(1, "test1", new Date()))));
            futureList.add(ExecutorConfig.getThreadPool().submit(
                    () -> employeeMapper.updateById(new Employee(1, "test2", new Date()))));

            for (Future<Integer> future : futureList) {
                Integer i = future.get();
                if (i != 1) {
                    throw new ServiceException("更新异常", 500);
                }
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 注意：数据库要现有数据，因为这里先执行了删除。
     * 上述虽然有多线程，但是共用一个connection，不能达到并发执行sql的作用，还是串行执行sql，如果需要并发执行sql，
     * 需要在mybatis中创建多个SqlSession，则对应多个connection，则代码如下。
     *
     * <p>可以发现子线程组执行时，有一个线程执行失败，其他线程也会抛出异常，
     * 但是主线程中执行的删除操作，没有回滚，@Transactional注解没有生效。
     */
    @Override
    @Transactional
    public void saveThread1(List<Employee> employeeList) {
        try {
            // 先做删除操作,如果子线程出现异常,此操作不会回滚
            employeeMapper.delete(null);
            // 获取线程池
            ExecutorService service = ExecutorConfig.getThreadPool();
            // 拆分数据,拆分5份
            List<List<Employee>> lists = ThreadUtil.averageAssign(employeeList, 5);
            // 执行的线程
            Thread[] threadArray = new Thread[lists.size()];
            // 监控子线程执行完毕,再执行主线程,要不然会导致主线程关闭,子线程也会随着关闭
            CountDownLatch countDownLatch = new CountDownLatch(lists.size());
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            for (int i = 0; i < lists.size(); i++) {
                if (i == lists.size() - 1) {
                    atomicBoolean.set(false);
                }
                List<Employee> list = lists.get(i);
                threadArray[i] = new Thread(() -> {
                    try {
                        // 最后一个线程抛出异常
                        if (!atomicBoolean.get()) {
                            throw new ServiceException("出现异常", 1);
                        }
                        // 批量添加
                        employeeMapper.saveBatchEmployee(list);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            for (int i = 0; i < lists.size(); i++) {
                service.execute(threadArray[i]);
            }
            // 当子线程执行完毕时,主线程再往下执行
            countDownLatch.await();
            System.out.println("添加完毕");
        } catch (Exception e) {
            log.info("error", e);
            throw new ServiceException("出现异常", 2);
        }
    }

    /**
     * 注意：数据库要现有数据，因为这里先执行了删除。
     * 删除操作的数据回滚了，数据库中的数据依旧存在，说明事务成功了。
     *
     * <p>多个SqlSession，多个connection。
     */
    @Override
    public void saveThread2(List<Employee> employeeList) throws SQLException {
        // 获取数据库连接,获取会话(内部自有事务)
        SqlSession sqlSession = sqlContext.getSqlSession();
        Connection connection = sqlSession.getConnection();
        try {
            // 设置手动提交
            connection.setAutoCommit(false);
            // 获取mapper
            EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
            // 先做删除操作
            employeeMapper.delete(null);
            // 获取执行器
            ExecutorService service = ExecutorConfig.getThreadPool();
            List<Callable<Integer>> callableList = new ArrayList<>();
            // 拆分list
            List<List<Employee>> lists = ThreadUtil.averageAssign(employeeList, 5);
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            for (int i = 0; i < lists.size(); i++) {
                if (i == lists.size() - 1) {
                    atomicBoolean.set(false);
                }
                List<Employee> list = lists.get(i);
                // 使用返回结果的callable去执行,
                Callable<Integer> callable = () -> {
                    // 让最后一个线程抛出异常
                    if (!atomicBoolean.get()) {
                        throw new ServiceException("出现异常", 1);
                    }
                    return employeeMapper.saveBatchEmployee(list);
                };
                callableList.add(callable);
            }
            // 执行子线程
            List<Future<Integer>> futures = service.invokeAll(callableList);
            for (Future<Integer> future : futures) {
                // 如果有一个执行不成功,则全部回滚
                if (future.get() <= 0) {
                    connection.rollback();
                    return;
                }
            }
            connection.commit();
            System.out.println("添加完毕");
        } catch (Exception e) {
            connection.rollback();
            log.info("error", e);
            throw new ServiceException("出现异常", 2);
        } finally {
            connection.close();
        }
    }

    /**
     * 注意：数据库要现有数据，因为这里先执行了删除。
     * 删除的删除了，添加的添加成功了，测试成功。
     */
    @Override
    public void saveThread3(List<Employee> employeeList) throws SQLException {
        // 获取数据库连接,获取会话(内部自有事务)
        SqlSession sqlSession = sqlContext.getSqlSession();
        Connection connection = sqlSession.getConnection();
        try {
            // 设置手动提交
            connection.setAutoCommit(false);
            EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
            // 先做删除操作
            employeeMapper.delete(null);
            ExecutorService service = ExecutorConfig.getThreadPool();
            List<Callable<Integer>> callableList = new ArrayList<>();
            List<List<Employee>> lists = ThreadUtil.averageAssign(employeeList, 5);
            for (List<Employee> list : lists) {
                Callable<Integer> callable = () -> employeeMapper.saveBatchEmployee(list);
                callableList.add(callable);
            }
            // 执行子线程
            List<Future<Integer>> futures = service.invokeAll(callableList);
            for (Future<Integer> future : futures) {
                if (future.get() <= 0) {
                    connection.rollback();
                    return;
                }
            }
            connection.commit();
            System.out.println("添加完毕");
        } catch (Exception e) {
            connection.rollback();
            log.info("error", e);
            throw new ServiceException("出现异常", 2);
        }
    }
}