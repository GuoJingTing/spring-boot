package com.base.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Description : TODO(角色表)
 * User: h819
 * Date: 2015/10/16
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */


//定义一个角色：对哪些树节点有哪些权限，这些节点的权限相同；
// 如果出现不同的节点不同的权限，那么定义为不同的角色。
@Entity
@Table(name = "base_role")
@Getter
@Setter
@ToString
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 该 entity 启用 auditing
public class RoleEntity extends BaseEntity {


    @ManyToMany(fetch = FetchType.LAZY, targetEntity = GroupEntity.class)// 如果是单向多对多，只在发出方设置，接收方不做设置
    @JoinTable(name = "base_ref_group_roles", //指定关联表名
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},////生成的中间表的字段，对应关系的发出端(主表) id
            inverseJoinColumns = {@JoinColumn(name = "group_id", referencedColumnName = "id")}, //生成的中间表的字段，对应关系的接收端(从表) id
            uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "group_id"})}) // 唯一性约束，是从表的联合字段
    private List<GroupEntity> groups = new ArrayList<>();


    @ManyToMany(fetch = FetchType.LAZY, targetEntity = UserEntity.class) // 单向多对多，只在发出方设置，接收方不做设置
    @JoinTable(name = "base_ref_users_roles", //指定关联表名
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},////生成的中间表的字段，对应关系的发出端(主表) id
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}, //生成的中间表的字段，对应关系的接收端(从表) id
            uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role_id"})}) // 唯一性约束，是从表的联合字段
    private List<UserEntity> users = new ArrayList<>();

//    @ManyToMany(fetch = FetchType.LAZY, targetEntity = PrivilegeEntity.class)// 单向多对多，只在发出方设置，接收方不做设置
//    @JoinTable(name = "base_ref_roles_privileges", //指定关联表名
//            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},////生成的中间表的字段，对应关系的发出端(主表) id
//            inverseJoinColumns = {@JoinColumn(name = "privilege_id", referencedColumnName = "id")}, //生成的中间表的字段，对应关系的接收端(从表) id
//            uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "privilege_id"})}) // 唯一性约束，是从表的联合字段
//    @Fetch(FetchMode.SUBSELECT)
//    @BatchSize(size = 100)//roles 过多的情况下应用。
//    private List<PrivilegeEntity> privileges = new ArrayList<>();


    @ManyToMany(fetch = FetchType.LAZY, targetEntity = TreeNodeEntity.class)// 单向多对多，只在发出方设置，接收方不做设置
    @JoinTable(name = "base_ref_roles_treenode", //指定关联表名
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},////生成的中间表的字段，对应关系的发出端(主表) id
            inverseJoinColumns = {@JoinColumn(name = "treenode_id", referencedColumnName = "id")}, //生成的中间表的字段，对应关系的接收端(从表) id
            uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "treenode_id"})}) // 唯一性约束，是从表的联合字段
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 100)//roles 过多的情况下应用。
    private List<TreeNodeEntity> treeNodes = new ArrayList<>();

    //昵称

    @Column(name = "name", unique = true)
    private String name;

    /**
     * JPA spec 需要无参的构造方法，用户不能直接使用。
     * 如果想要生成 Entity ，用其他有参数的构造方法。
     */
    public RoleEntity() {
        // no-args constructor required by JPA spec
        // this one is protected since it shouldn't be used directly
    }

    public RoleEntity(final String name) {
        super();
        this.name = name;
    }


    /**
     * 自定义方法，添加
     * 注意建立两个对象关联的方法(单向的不需要)
     *
     * @param treeNode
     */
    public void addTreeNode(TreeNodeEntity treeNode) {

        if (!this.treeNodes.contains(treeNode)) {
            this.treeNodes.add(treeNode);
            treeNode.getRoles().add(this);
        }
    }

    /**
     * 添加多个
     *
     * @param treeNodes
     */
    public void addTreeNodes(List<TreeNodeEntity> treeNodes) {
        this.setTreeNodes(treeNodes);
    }

    /**
     * 自定义方法，删除
     * 注意解除关联的方法(单向的不需要)
     *
     * @param treeNode
     */
    public void removeTreeNode(TreeNodeEntity treeNode) {

        if (this.treeNodes.contains(treeNode)) {
            this.treeNodes.remove(treeNode);
            treeNode.getRoles().remove(this);

        }
    }

    /**
     * 删除多个
     *
     * @param treeNodes
     */
    public void removeTreeNodes(List<TreeNodeEntity> treeNodes) {
        for (TreeNodeEntity entity : treeNodes)
            removeTreeNode(entity);
    }

    /**
     * 清空子
     */
    public void clearTreeNodes() {
        this.treeNodes.clear();
    }

    /**
     * 自定义方法，添加用户
     * 注意建立两个对象关联的方法(单向的不需要)
     *
     * @param user
     */
    public void addUser(UserEntity user) {

        if (!this.users.contains(user)) {
            this.users.add(user);
            user.getRoles().add(this);
        }

    }

    public void addUsers(List<UserEntity> users) {

        this.setUsers(users);

    }

    /**
     * 自定义方法，删除用户
     * 注意删除关联的方法(单向的不需要)
     *
     * @param user
     */
    public void removeUser(UserEntity user) {

        if (this.users.contains(user)) {
            this.users.remove(user);
            user.getRoles().remove(this);

        }

    }

    /**
     * 自定义方法，添加学生
     * 注意建立关联的方法(单向的不需要)
     *
     * @param group
     */
    public void addGroup(GroupEntity group) {

        if (!this.groups.contains(group)) {
            this.groups.add(group);
            group.getRoles().add(this);
        }

    }

    public void addGroups(List<GroupEntity> groups) {

        this.setGroups(groups);

    }

    /**
     * 自定义方法，删除学生
     * 注意删除关联的方法(单向的不需要)
     *
     * @param group
     */
    public void removeGroup(GroupEntity group) {

        if (this.groups.contains(group)) {
            this.groups.remove(group);
            group.getRoles().remove(this);

        }
    }
}
