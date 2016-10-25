package com.base.spring.domain.Deprecated;

import com.base.spring.domain.BaseEntity;
import com.base.spring.domain.RoleEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * 代表页面资源，或者具体的功能如 ：下载、打印
 * 操作可以为任何类型，只要想要进行控制的资源，就可以放入此表。
 * 页面用标签进行控制，只要拥有此 PrivilegeEntity 的 id ，即进行显示，表示该用户可以继续此操作
 */
//@Entity
//@Table(name = "base_privilege"
@Deprecated
public class PrivilegeEntity extends BaseEntity {


    @Column(name = "name", unique = true)
    private String name;

    /**
     * 针对不特定页面上的具体页面资源，如 按钮、图片等。如果拥有页面权限权限，就你看到这种资源，看不到代表没有该资源
     * --
     * 权限控制方法
     * 1. 能否看见
     * 2. 点击该资源的时候，能否执行，用 spring security 在方法级别上控制
     */
    @Column(name = "type", nullable = false)
    private PrivilegeType type;

    /**
     * 一个角色，可以拥有多个权限
     */

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "privileges", targetEntity = RoleEntity.class)
    private List<RoleEntity> roles = new ArrayList<>();


    /**
     * 菜单 url
     */
    @Column(name = "url")
    private String url;

//
//    /**
//     * 多个节点，可以拥有同一种权限，如 节点编辑  ...
//     * 一个节点，也可以又有多种权限
//     */
//    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "privileges", targetEntity = TreeNodeEntity.class)
//    private List<TreeNodeEntity> treeNodes = new ArrayList<TreeNodeEntity>();


    /**
     * 权限和页面资源为 OneToOne，一个页面资源，对应一种权限
     */
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    // 双向：发出端、接收端都需要设置。
//    // 自动生成中间表，生成的字段默认是: 实体名称_id ，可以通过 @JoinColumn(name = "parent_id", referencedColumnName = "id")指定，这样可以显示的增加唯一约束条件
//    // 双方实体表不发生变化
//    @JoinTable(name = "base_ref_privilege_page_element", //指定关联表名
//            joinColumns = {@JoinColumn(name = "privilege_id", referencedColumnName = "id")},////生成的中间表的字段，对应关系的发出端(主表) id
//            inverseJoinColumns = {@JoinColumn(name = "page_element_id", referencedColumnName = "id")}, //生成的中间表的字段，对应关系的接收端(从表) id
//            uniqueConstraints = {@UniqueConstraint(columnNames = {"privilege_id", "page_element_id"})})
//    // 唯一性约束，是从表的联合字段
//    private PageElementEntity pageElement;
    public PrivilegeEntity(final String name, PrivilegeType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * JPA spec 需要无参的构造方法，用户不能直接使用。
     * 如果想要生成 Entity ，用其他有参数的构造方法。
     */
    protected PrivilegeEntity() {
        // no-args constructor required by JPA spec
        // this one is protected since it shouldn't be used directly
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEntity> roles) {
        this.roles = roles;
    }

    public PrivilegeType getType() {
        return type;
    }

    public void setType(PrivilegeType type) {
        this.type = type;
    }

//    public List<TreeNodeEntity> getTreeNodes() {
//        return treeNodes;
//    }
//
//    public void setTreeNodes(List<TreeNodeEntity> treeNodes) {
//        this.treeNodes = treeNodes;
//    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //    public PageElementEntity getPageElement() {
//        return pageElement;
//    }
//
//    public void setPageElement(PageElementEntity pageElement) {
//        this.pageElement = pageElement;
//    }
}
