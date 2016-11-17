package org.h819.web.spring.jdbc;

import org.h819.web.spring.vo.PageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Description : JdbcTemplate工具类 ，参数绑定方式，可以避免 sql 注入
 * User: h819
 * Date: 2015/6/11
 * Time: 11:27
 * To change this template use File | Settings | File Templates.
 */

// 例子见 standard-open-api-server , com.open.api.oracle.service.StStandardNativeService
public class JdbcUtils {

    /**
     * 占位符只能用于 value ，不能用于列名(column)
     * 所以 order by ? ,? asc 不可以
     */

    //spring jdbc ，占位符方式 like 表达式的写法固定，只有 like  特殊，其他的和普通 sql 相同
    //where name like '%中国%' ，转换为占位符方式，相当于
    //where name like '%'||?||'%'
    public static String BIND_LIKE_STRING = " '%'||?||'%' ";
    private static Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

    //其他的例子
    //= ：  name =?
    //between :  act_time between to_date(?,'yyyy-mm-dd') and to_date(?,'yyyy-mm-dd')

    @Autowired
    //@Qualifier("oracleDataSource")  // 配置文件中，有了 @Primary ，会默认连接次数据库，不要在指定
    //       JdbcTemplate jdbcTemplate;

    /**
     * 构造分页查询，有排序提交件
     * -
     * 利用 spring JdbcTemplate 进行分页查询，BeanPropertyRowMapper 包装
     * 分页需要进行两次查询
     * 排序条件不在这里维护，占位符不能用于 order by
     *
     * @param jdbcTemplate   注入 JdbcTemplate 方法
     *                       //@Autowired JdbcTemplate jdbcTemplate;
     *                       -
     *                       如果是多数据源，并且配置时数据源设置了　 @Primary 指向需要连接的数据库，仅需要
     *                       //@Autowired JdbcTemplate jdbcTemplate;
     *                       -
     *                       如果是多数据源，配置时数据源没有设置 @Primary
     *                       //@Autowired //@Qualifier("oracleDataSource") JdbcTemplate jdbcTemplate; 需要指定数据源名称
     * @param dbDialect      数据库类型，不同的数据库生成的分页语句不一样
     * @param queryNativeSql 本地查询条件，和不分页时相同，如
     *                       select * from standard st where st.namecn like '%中国%' ,
     *                       select st.id,st.name from standard st where st.id ='239711'  等
     * @param queryArgs      arguments to bind to the query ，查询时，绑定的参数
     *                       (leaving it to the PreparedStatement to guess the corresponding SQL type ，是 Object 类型，系统可以自动匹配成 sql 类型)
     *                       无参数时，传入 new Objects[]{} 空数组即可
     *                       占位符方式，可以避免 sql 注入  Queries with Named Parameters
     * @param countNativeSql 本地计算总数的语句
     * @param countArgs      计算总数时，绑定的参数
     * @param currentPageNo  当前页码，从 1 开始
     * @param pageSize       页大小
     * @param resultClass    返回值类型
     * @return
     */
    public static <T> PageBean<T> findPageByNativeSqlString(final JdbcTemplate jdbcTemplate, final SqlUtils.Dialect dbDialect,
                                                            final String queryNativeSql, Object[] queryArgs,
                                                            final String countNativeSql, Object[] countArgs,
                                                            int currentPageNo, int pageSize,
                                                            Class resultClass) {

        if (currentPageNo < 1)
            throw new IllegalArgumentException("currentPageNo : 起始页应从 1 开始。");

        if (pageSize < 0)
            throw new IllegalArgumentException("pageSize : 页大小不能小于 0");

        if (!countNativeSql.toLowerCase().contains("count"))
            throw new IllegalArgumentException("queryNativeSql 和 countNativeSql 参数顺序不对");


        String queryNativeSqlString = SqlUtils.createNativePageSqlString(dbDialect, queryNativeSql, currentPageNo, pageSize);

        logger.info("countNativeSql : \n {} ", countNativeSql);
        logger.info("queryPageNativeSql : \n {} ", queryNativeSqlString);


        // 计算总数
        final int totalRecordsSize = jdbcTemplate.queryForObject(countNativeSql, countArgs, Integer.class);
//        logger.info("totalRecordsSize : " + totalRecordsSize);
        List<T> content = jdbcTemplate.query(queryNativeSqlString, queryArgs, new BeanPropertyRowMapper(resultClass));

        return new PageBean(pageSize, currentPageNo, totalRecordsSize, content);
    }

    /**
     * 同上，构造查询语句，不分页
     *
     * @param jdbcTemplate
     * @param queryNativeSql
     * @param resultClass
     * @return
     */
    public static <T> List<T> findByNativeSqlString(final JdbcTemplate jdbcTemplate,
                                                    final String queryNativeSql, Object[] queryArgs,
                                                    Class resultClass) {
        return jdbcTemplate.query(queryNativeSql, queryArgs, new BeanPropertyRowMapper(resultClass));
    }


    /**
     * 构造排序条件
     * 其他参数，需要时在完善
     *
     * @param sortParameters
     * @param sort
     * @return
     */
    public static String createOrderByString(String[] sortParameters, SqlUtils.SortDirection sort) {
        return " order by  " + Arrays.toString(sortParameters).replace("[", "").replace("]", "") + " " + sort;

    }


}