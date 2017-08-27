package org.h819.web.spring.jpa;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.h819.web.jqgird.JqgridUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 工具类，只适用于 spring data jpa ，可以用于生成 jqgrid 页数据
 * JPA 1.10 有 Example 类 ，适时修改此类
 * <p>
 * 当 filters 参数为 null  时，可以用作非 jqgrid 情况下
 * <p>
 * <p>
 * 根据 jqgrid 传递过来的参数，通过 Repository 进行查询，封装了各项查询条件。
 */
public class JpaUtils {

    private static final Logger logger = LoggerFactory.getLogger(JpaUtils.class);

    /**
     * 仅通过静态方法调用
     */
    private JpaUtils() {
    }

    //org.springframework.data.domain.Page 规定起始页为 0

    /**
     * 根据 customSpecification，进行分页查询。
     * <p>
     * findAll() 方法，会进行两次查询，先做 count 查询，之后是具体查询，所以 Page 中包含了总数和具体查询结果集
     *
     * @param repository          查询器
     * @param currentPageNo       当前页，实际对应 jqgrid 传递过来的 page 参数，jqgrid 规定起始页为 1
     * @param pageSize            页面可显示行数
     * @param sort
     * @param customSpecification 除了 jqgrid 传递过来的查询条件外，自己又附加查询条件,与 filters AND 关系的查询条件，specification 的构造符合 SearchFilter 写法，详见示例项目。
     *                            customSpecification 中指定的属性名称应该是待查询的 entity 中的属性名称，并且用改 entity 的 repository 进行查询
     * @return
     */

    public static Page getPage(JpaSpecificationExecutor repository, int currentPageNo, int pageSize, Sort sort, Specification customSpecification) {

        //jpa 中起始页为 0，但传递过来的参数 currentPageNo 不能小于1
        Assert.isTrue(currentPageNo >= 1, "currentPageNo  需要 >= 1 ");
        currentPageNo = currentPageNo - 1;
        return repository.findAll(customSpecification, new PageRequest(currentPageNo, pageSize, sort));


    }

    /**
     * 根据 jqgrid 的 search 操作传递过来的条件(含 filters 条件)，进行分页查询。
     * <p>
     * findAll() 方法，会进行两次查询，先做 count 查询，之后是具体查询，所以 Page 中包含了总数和具体查询结果集
     *
     * @param repository          查询器
     * @param currentPageNo       当前页，实际对应 jqgrid 传递过来的 page 参数，jqgrid 规定起始页为 1
     * @param pageSize            页面可显示行数
     * @param property            用于排序的列名 ，启用 groups 时，此项复杂，需要特殊解析
     * @param direction           排序的方式，只能为  desc 或 asc
     * @param jqgridFilters       通过 jqgrid search 按键查询，多个查询条件时，包含查询条件的 json 格式数据   ,  filters 为 null  时，可以用作非 jqgrid 情况下
     * @param customSpecification 除了 jqgrid 传递过来的查询条件外，自己又附加查询条件,与 filters AND 关系的查询条件，specification 的构造符合 SearchFilter 写法，详见示例项目。
     *                            customSpecification 中指定的属性名称应该是待查询的 entity 中的属性名称，并且用改 entity 的 repository 进行查询
     * @return
     */

    public static Page getJqGridPage(Repository repository, int currentPageNo, int pageSize, String direction, String property, String jqgridFilters, Specification customSpecification) {

        JpaSpecificationExecutor rep = (JpaSpecificationExecutor) repository;


        //jpa 中起始页为 0，但传递过来的参数 currentPageNo 不能小于1
        Assert.isTrue(currentPageNo >= 1, "currentPageNo  需要 >= 1 ");
        currentPageNo = currentPageNo - 1;

        if (jqgridFilters == null || jqgridFilters.isEmpty()) {  //刷新表格时，filters.isEmpty() = true
            Sort sort = getJqGirdSort(direction, property);
            if (sort == null)
                return rep.findAll(customSpecification, new PageRequest(currentPageNo, pageSize));
            else
                return rep.findAll(customSpecification, new PageRequest(currentPageNo, pageSize, sort));

        } else {

            JqgridUtils.Filter f = JqgridUtils.getSearchFilters(jqgridFilters);
            //根据 jqgrid filters 参数，构造查询条件

            Specification specificationFilters = null;
            if (f.getGroupRelation().equals(SearchFilter.Relation.AND))
                specificationFilters = new JpaDynamicSpecificationBuilder().and(f.getSearchFilters()).build();
            if (f.getGroupRelation().equals(SearchFilter.Relation.OR))
                specificationFilters = new JpaDynamicSpecificationBuilder().or(f.getSearchFilters()).build();

            Sort sort = getJqGirdSort(direction, property);
            if (sort == null)
                return rep.findAll(
                        new JpaDynamicSpecificationBuilder().and(customSpecification, specificationFilters).build(),
                        new PageRequest(currentPageNo, pageSize)
                );
            else
                return rep.findAll(
                        new JpaDynamicSpecificationBuilder().and(customSpecification, specificationFilters).build(),
                        new PageRequest(currentPageNo, pageSize, sort)
                );
        }
    }

    /**
     * 无查询条件，进行分页查询。
     * findAll() 方法，会进行两次查询，先做 count 查询，之后是具体查询，所以 Page 中包含了总数和具体查询结果集
     * <p>
     * 仅是 repository 类型不同，没有和上一个方法合并
     *
     * @param repository    查询器，必须是 extends JpaRepository<???, Long>, JpaSpecificationExecutor 类型的写法。
     * @param currentPageNo 当前页，起始页为 1
     * @param pageSize      页面可显示行数
     * @param sort
     * @return
     */
    public static Page getPage(JpaRepository repository, int currentPageNo, int pageSize, Sort sort) {
        //jpa 中起始页为 0，但传递过来的参数 currentPageNo 不能小于1
        Assert.isTrue(currentPageNo >= 1, "currentPageNo  需要 >= 1 ");
        currentPageNo = currentPageNo - 1;
        return repository.findAll(new PageRequest(currentPageNo, pageSize, sort));
    }


    /**
     * 根据 jqgrid 的 search 操作传递过来的条件(含 filters 条件)，和附加的其他查询信息，进行分页查询。
     * findAll() 方法，会进行两次查询，先做 count 查询，之后是具体查询，所以 Page 中包含了总数和具体查询结果集
     * <p>
     * 仅是 repository 类型不同，没有和上一个方法合并
     *
     * @param repository    查询器，必须是 extends JpaRepository<???, Long>, JpaSpecificationExecutor 类型的写法。
     * @param currentPageNo 当前页，实际对应 jqgrid 传递过来的 page 参数，jqgrid 规定起始页为 1
     * @param pageSize      页面可显示行数
     * @param property      用于排序的列名 ，启用 groups 时，此项复杂，需要特殊解析
     * @param direction     排序的方式，只能为  desc 或 asc
     * @param jqgridFilters 通过 jqgrid search 按键查询，多个查询条件时，包含查询条件的 json 格式数据
     * @return
     */
    public static Page getJqGridPage(Repository repository, int currentPageNo, int pageSize, String direction, String property, String jqgridFilters) {

        //jpa 中起始页为 0，但传递过来的参数 currentPageNo 不能小于1
        Assert.isTrue(currentPageNo >= 1, "currentPageNo  需要 >= 1 ");
        currentPageNo = currentPageNo - 1;


        if (jqgridFilters == null || jqgridFilters.isEmpty()) {  //刷新表格时，filters.isEmpty() = true
            JpaRepository rep = (JpaRepository) repository;
            Sort sort = getJqGirdSort(direction, property);
            if (sort == null)
                return rep.findAll(new PageRequest(currentPageNo, pageSize));
            else
                return rep.findAll(new PageRequest(currentPageNo, pageSize, sort));

        } else {

            JpaSpecificationExecutor rep = (JpaSpecificationExecutor) repository;
            JqgridUtils.Filter f = JqgridUtils.getSearchFilters(jqgridFilters);
            //根据 jqgrid filters 参数，构造查询条件
            Specification specificationFilters = null;
            if (f.getGroupRelation().equals(SearchFilter.Relation.AND))
                specificationFilters = new JpaDynamicSpecificationBuilder().and(f.getSearchFilters()).build();
            if (f.getGroupRelation().equals(SearchFilter.Relation.OR))
                specificationFilters = new JpaDynamicSpecificationBuilder().or(f.getSearchFilters()).build();

            //  Specification spec = JpaDynamicSpecificationUtils.joinSearchFilter(f.getGroupRelation(), f.getSearchFilters());

            Sort sort = getJqGirdSort(direction, property);
            if (sort == null)
                return rep.findAll(specificationFilters, new PageRequest(currentPageNo, pageSize));
            else
                return rep.findAll(specificationFilters, new PageRequest(currentPageNo, pageSize, sort));
        }
    }

    /**
     * 根据 jqgrid 传过来的排序信息，构造排序所需要的 Sort
     *
     * @param direction 排序的方式，只能为  desc 或 asc
     * @param property  用于排序的列名, grouping:true 时格式特殊，需要正确解析
     * @return
     */
    private static Sort getJqGirdSort(String direction, String property) {

        if (property == null || property.isEmpty()) {
            logger.info("排序字段为 null 或 空");
            return null;
        }

        //排序字段
        if (!property.contains(",")) { //未分组

            return createSort(direction, property);

        } else { //分组,grouping:true 时

            String[] arrays = StringUtils.removeEnd(property.trim(), ",").split(",");  //传来的排序请求字符串，形如 sidx =name asc, herf desc,实际经过参数对应后变成字符串 name asc, herf desc,
            //arrays = {[name asc],[herf desc]}

            List<Sort.Order> orders = Lists.newArrayList();
            List<String> unique = Lists.newArrayList();   //为了避免同一个属性，重复添加。此情况发生在 grouping:true 时，没有进一步测试。
            for (String s : arrays) { //拼接所有的排序请求。
                String propertyT = StringUtils.substringBefore(s.trim(), " ");
                if (unique.contains(propertyT))
                    continue;
                unique.add(propertyT);
                String directionT = StringUtils.substringAfter(s.trim(), " ");
                orders.add(createOrder(directionT, propertyT));
            }
            return createSort(orders);
        }
    }


    /**
     * 单个排序条件创建 Sort
     *
     * @param property
     * @param direction
     * @return
     */
    public static Sort createSort(Sort.Direction direction, String property) {
        return new Sort(direction, property);
    }

    /**
     * 单个排序条件创建 Sort
     *
     * @param property
     * @param direction
     * @return
     */
    public static Sort createSort(String direction, String property) {
        return new Sort(createOrder(direction, property));
    }

    /**
     * 多个排序条件，一个排序方向
     *
     * @param direction
     * @param property
     * @return
     */

    public static Sort createSort(String direction, String[] property) {
        return new Sort(createDirection(direction), property);
    }


    /**
     * 通过创建多个 Order，创建多个排序条件，多个排序方向
     *
     * @param orders
     * @return
     */
    public static Sort createSort(List<Sort.Order> orders) {
        return new Sort(orders);
    }

    /**
     * 创建排序条件 Order
     *
     * @param direction
     * @param property
     * @return
     */
    public static Sort.Order createOrder(String direction, String property) {
        Assert.isTrue(!property.isEmpty(), " 排序字段没有指定");
        return new Sort.Order(createDirection(direction), property);
    }

    /**
     * String to Sort.Direction
     *
     * @param direction
     * @return
     */
    private static Sort.Direction createDirection(String direction) {

        // logger.info(direction);
        Assert.state(direction.equalsIgnoreCase("ASC") || direction.equalsIgnoreCase("DESC"), " 排序 direction 只能为 ASC or DESC");
        Sort.Direction directionTemp;
        if (direction.toLowerCase().equals("asc"))
            directionTemp = Sort.Direction.ASC;
        else directionTemp = Sort.Direction.DESC;
        return directionTemp;
    }

    /**
     * 创建分页请求.
     *
     * @param currentPageNo 当前页码，第一页为 0
     * @param pageSize      每页记录数
     * @param property      排序字段
     * @param direction     排序关键字，应为 DESC、ASC，可以为 null ，此时按默认排序，即 DESC
     * @return
     */
    public static PageRequest createPageRequest(int currentPageNo, int pageSize, String direction, String property) {

        Assert.hasText(property, "fieldName must not be null or empty!");
        return new PageRequest(currentPageNo, pageSize, createSort(property, direction));
    }

    /**
     * 创建分页请求，默认排序.
     *
     * @param currentPageNo 当前页码，第一页为 0
     * @param pageSize      每页记录数
     * @return
     */
    public static PageRequest createPageRequest(int currentPageNo, int pageSize) {
        return new PageRequest(currentPageNo, pageSize);
    }

    /**
     * 创建分页请求，自定义排序.
     *
     * @param currentPageNo 当前页码，第一页为 0
     * @param pageSize      每页记录数
     * @param sort          排序
     * @return
     */
    public static PageRequest createPageRequest(int currentPageNo, int pageSize, Sort sort) {
        return new PageRequest(currentPageNo, pageSize, sort);
    }
}