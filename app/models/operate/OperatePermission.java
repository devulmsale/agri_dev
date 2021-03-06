package models.operate;

import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

//@Entity
//@Table(name = "operate_permissions")
public class OperatePermission extends Model {

    private static final long serialVersionUID = 234119113062L;


    @Column(name = "perm_text")
    public String text;

    @Column(name = "perm_key")
    public String key;

    public String description;

    @OrderColumn(name = "display_order")
    public Integer displayOrder;

    @Column(name = "application_name")
    public String applicationName;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "parent_id", nullable = true)
    public OperatePermission parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, targetEntity = OperatePermission.class)
    @OrderBy("displayOrder")
    public List<OperatePermission> children;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "operate_permissions_roles",
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            joinColumns = @JoinColumn(name = "permission_id"))
    public Set<OperateRole> roles;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "operate_permissions_users",
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            joinColumns = @JoinColumn(name = "permission_id"))
    public Set<OperateUser> users;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "operate_navs_perms",
            inverseJoinColumns = @JoinColumn(name = "navigation_id"),
            joinColumns = @JoinColumn(name = "permission_id"))
    public Set<OperateNavigation> navigations;

    /**
     * 加载版本，使用应用程序加载时间，在处理完成后，删除不是当前loadVersion的记录，以完成同步.
     */
    @Column(name = "load_version")
    public long loadVersion;


    /**
     * 删除不是当前loadVersion的记录，这样可以保证只有一个版本的rbac.xml的数据在数据库中.
     *
     * @param applicationName
     * @param loadVersion
     */
    public static void deleteUndefinedPermissions(String applicationName, long loadVersion) {
//        List<OperatePermission> list = OperatePermission.find.where()
//                .eq("applicationName", applicationName).ne("loadVersion", loadVersion)
//                .orderByDesc("parent").orderByDesc("id").findList();

        List<OperatePermission> list = OperatePermission.find("applicationName = ?", applicationName).fetch();
        for (OperatePermission perm : list) {
            perm.delete();
        }
    }

    /**
     * 按用户角色得到权限列表。
     *
     * @param userId
     * @return
     */
    public static List<OperatePermission> findByUserRole(Long userId) {
        String hql = "select p from OperatePermission p join p.roles r join r.users u where u.id = :id";
        TypedQuery<OperatePermission> query = JPA.em().createQuery(
                hql, OperatePermission.class);
        query.setParameter("id", userId);
        return query.getResultList();
    }

}
