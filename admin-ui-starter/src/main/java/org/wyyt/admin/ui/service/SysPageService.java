package org.wyyt.admin.ui.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.admin.ui.entity.dto.SysPage;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.entity.vo.PageVo;
import org.wyyt.tool.db.CrudPage;
import org.wyyt.tool.db.CrudService;

import java.util.*;

/**
 * The service for table 'sys_page'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysPageService {
    private final CrudService crudService;

    public SysPageService(final @Qualifier("adminUiCrudService") CrudService crudService) {
        this.crudService = crudService;
    }

    public SysPage getById(final Long id) throws Exception {
        return this.crudService.selectOne(SysPage.class, "SELECT * FROM `sys_page` WHERE `id`=?", id);
    }

    public List<SysPage> getEmptyUrl() throws Exception {
        return this.crudService.select(SysPage.class, "SELECT * FROM `sys_page` WHERE `url`='' ORDER BY `name` ASC");
    }

    public List<SysPage> list() throws Exception {
        return this.crudService.select(SysPage.class, "SELECT * FROM `sys_page`");
    }

    public IPage<PageVo> list(@Nullable final String name,
                              final Integer pageNum,
                              final Integer pageSize) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT" +
                " `p`.`id`," +
                " `p`.`name`," +
                " `p`.`url`," +
                " `p`.`is_menu`," +
                " `p`.`is_default`," +
                " `p`.`is_blank`," +
                " `p`.`icon_class`," +
                " `p`.`parent_id`," +
                " `parent`.`name` AS `parent_name`," +
                " `p`.`order_num`," +
                " `p`.`remark`" +
                " FROM `sys_page` `p`" +
                " LEFT OUTER JOIN `sys_page` `parent` ON `p`.`parent_id` = `parent`.`id` WHERE 1=1");
        if (!ObjectUtils.isEmpty(name)) {
            sql.append(" AND `p`.`name` LIKE ?");
        }
        sql.append(" ORDER BY p.parent_id ASC, p.order_num ASC");

        final IPage<PageVo> result = new Page<>(pageNum, pageSize);
        final CrudPage<PageVo> crudPage = this.crudService.page(PageVo.class, pageNum, pageSize, sql.toString(), ObjectUtils.isEmpty(name) ? null : "%".concat(name).concat("%"));
        result.setRecords(crudPage.getRecrods());
        result.setTotal(crudPage.getTotal());
        return result;
    }

    public Long getMaxOrderNum(final Long parentId) throws Exception {
        return this.crudService.executeScalar(Long.class,
                "SELECT IFNULL(MAX(`order_num`), 0) FROM `sys_page` WHERE `parent_id` = ?",
                parentId);
    }

    public List<SysPage> listSysPages() throws Exception {
        return this.crudService.select(SysPage.class,
                "SELECT * FROM `sys_page` WHERE `is_menu`=? ORDER BY parent_id ASC, order_num ASC", 1);
    }

    public List<SysPage> listEmptyUrl() throws Exception {
        return this.crudService.select(SysPage.class, "SELECT * FROM `sys_page` WHERE `url`!='' ORDER BY `name` ASC");
    }

    public List<PageVo> listPermissionPages(Long adminId) throws Exception {
        return this.crudService.select(PageVo.class,
                "SELECT" +
                        " `page`.`id`," +
                        " `page`.`name`," +
                        " `page`.`url`," +
                        " `page`.`is_menu`," +
                        " `page`.`is_default`," +
                        " `page`.`icon_class`," +
                        " `page`.`parent_id`," +
                        " `parent`.`name` AS `parent_name`," +
                        " `page`.`order_num`," +
                        " `page`.`remark`," +
                        " `p`.`can_insert`," +
                        " `p`.`can_delete`," +
                        " `p`.`can_update`," +
                        " `p`.`can_select`" +
                        " FROM `sys_page` `page`" +
                        " LEFT OUTER JOIN `sys_permission` `p` ON `page`.`id` = `p`.`sys_page_id`" +
                        " LEFT OUTER JOIN `sys_role` `r` ON `r`.`id` = `p`.`sys_role_id`" +
                        " LEFT OUTER JOIN `sys_admin` `a` ON `a`.`sys_role_id` = `r`.`id`" +
                        " LEFT OUTER JOIN `sys_page` `parent` ON `page`.`parent_id` = `parent`.`id`" +
                        " WHERE `a`.`id` = ?", adminId);
    }

    public void fillPages(final AdminVo adminVo) throws Exception {
        if (null == adminVo) {
            return;
        }
        adminVo.setPermissions(getPages(adminVo));
        adminVo.getPermissions().sort((o1, o2) -> (int) (o1.getOrderNum() - o2.getOrderNum()));
    }


    private List<PageVo> getPages(final AdminVo adminVo) throws Exception {
        final List<SysPage> allPages = this.listSysPages();
        final Map<Long, SysPage> allPageMap = toMap(allPages);

        // 非顶级集合
        final List<PageVo> nonRootPageList = new ArrayList<>();
        // 顶级集合
        final List<PageVo> rootPageList = new ArrayList<>();
        final List<PageVo> pageVoList = this.listPermissionPages(adminVo.getId());

        final Map<Long, PageVo> permission = new HashMap<>((int) (pageVoList.size() / 0.75));
        for (final PageVo pageVo : pageVoList) {
            permission.put(pageVo.getId(), pageVo);
        }

        for (final SysPage sysPage : allPages) {
            if (!adminVo.getSysRole().getSuperAdmin() && !permission.containsKey(sysPage.getId())) {
                continue;
            }
            if (0 == sysPage.getParentId()) {
                final PageVo rootPageVo = Utils.toVo(sysPage, PageVo.class);
                final PageVo permissionPageVo = permission.get(rootPageVo.getId());
                if (null != permissionPageVo) {
                    rootPageVo.setCanInsert(permissionPageVo.getCanInsert());
                    rootPageVo.setCanDelete(permissionPageVo.getCanDelete());
                    rootPageVo.setCanUpdate(permissionPageVo.getCanUpdate());
                    rootPageVo.setCanSelect(permissionPageVo.getCanSelect());
                }
                rootPageList.add(rootPageVo);
            } else {
                nonRootPageList.add(Utils.toVo(sysPage, PageVo.class));
            }
            if (sysPage.getIsDefault()) {
                adminVo.setDefaultPage(sysPage.getUrl());
            }
        }

        for (final PageVo pageVo : nonRootPageList) {
            final SysPage sysPageParent = allPageMap.get(pageVo.getParentId());
            if (null != sysPageParent) {
                final Optional<PageVo> first = rootPageList.stream().filter(p -> p.getId().equals(sysPageParent.getId())).findFirst();
                if (!first.isPresent()) {
                    final PageVo rootPageVo = Utils.toVo(sysPageParent, PageVo.class);
                    final PageVo permissionPageVo = permission.get(rootPageVo.getId());
                    if (null != permissionPageVo) {
                        rootPageVo.setCanInsert(permissionPageVo.getCanInsert());
                        rootPageVo.setCanDelete(permissionPageVo.getCanDelete());
                        rootPageVo.setCanUpdate(permissionPageVo.getCanUpdate());
                        rootPageVo.setCanSelect(permissionPageVo.getCanSelect());
                    }
                    rootPageList.add(rootPageVo);
                }
            }
        }

        if (ObjectUtils.isNotNull(rootPageList) || ObjectUtils.isNotNull(nonRootPageList)) {
            final Set<Long> map = Sets.newHashSetWithExpectedSize(nonRootPageList.size());
            rootPageList.forEach(rootPage -> getChild(adminVo, permission, rootPage, nonRootPageList, map));
            filter(rootPageList);
            return rootPageList;
        }
        return null;
    }

    private Map<Long, SysPage> toMap(final List<SysPage> sysPageList) {
        final Map<Long, SysPage> result = new HashMap<>((int) (sysPageList.size() / 0.75));

        for (final SysPage sysPage : sysPageList) {
            result.put(sysPage.getId(), sysPage);
        }
        return result;
    }

    private void filter(final List<PageVo> pageVoList) {
        final Iterator<PageVo> iterator = pageVoList.iterator();
        while (iterator.hasNext()) {
            final PageVo page = iterator.next();
            if (ObjectUtils.isEmpty(page.getUrl()) && (page.getChildren() == null || page.getChildren().size() < 1)) {
                iterator.remove();
            } else {
                filter(page.getChildren());
            }
        }
    }

    private void getChild(final AdminVo adminVo,
                          final Map<Long, PageVo> permission,
                          final PageVo parentPage,
                          final List<PageVo> childrenPageList,
                          final Set<Long> set) {
        final List<PageVo> childList = Lists.newArrayList();
        childrenPageList.stream().//
                filter(p -> !set.contains(p.getId())). // 判断是否已循环过当前对象
                filter(p -> p.getParentId().equals(parentPage.getId())). // 判断是否父子关系
                filter(p -> set.size() <= childrenPageList.size()).// set集合大小不能超过childrenPageList的大小
                forEach(p -> {
            if (adminVo.getSysRole().getSuperAdmin() || ObjectUtils.isEmpty(p.getUrl()) || permission.containsKey(p.getId())) {
                // 放入set, 递归循环时可以跳过这个页面，提高循环效率
                set.add(p.getId());
                // 递归获取当前类目的子类目
                getChild(adminVo, permission, p, childrenPageList, set);

                if (permission.containsKey(p.getId())) {
                    final PageVo page = permission.get(p.getId());
                    p.setCanInsert(page.getCanInsert());
                    p.setCanDelete(page.getCanDelete());
                    p.setCanUpdate(page.getCanUpdate());
                    p.setCanSelect(page.getCanSelect());
                }
                childList.add(p);
            }
        });
        parentPage.setChildren(childList);
    }

    public void removeByIds(final List<Long> idsList) throws Exception {
        if (null == idsList || idsList.isEmpty()) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM `sys_page` WHERE `id` IN (");
        for (final Long id : idsList) {
            sql.append(id.toString().concat(","));
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(")");

        this.crudService.execute(sql.toString());
    }

    public void updateById(final SysPage sysPage) throws Exception {
        this.crudService.execute("UPDATE `sys_page` SET `name`=?,`url`=?,`is_menu`=?,`is_default`=?,`is_blank`=?,`icon_class`=?,`parent_id`=?,`order_num`=?,`remark`=? WHERE `id`=?",
                sysPage.getName(),
                sysPage.getUrl(),
                sysPage.getIsMenu(),
                sysPage.getIsDefault(),
                sysPage.getIsBlank(),
                sysPage.getIconClass(),
                sysPage.getParentId(),
                sysPage.getOrderNum(),
                sysPage.getRemark(),
                sysPage.getId());
    }

    public void insert(final SysPage sysPage) throws Exception {
        this.crudService.execute("INSERT INTO `sys_page`(`name`,`url`,`is_menu`,`is_default`,`is_blank`,`icon_class`,`parent_id`,`order_num`,`remark`) VALUES(?,?,?,?,?,?,?,?,?)",
                sysPage.getName(),
                sysPage.getUrl(),
                sysPage.getIsMenu(),
                sysPage.getIsDefault(),
                sysPage.getIsBlank(),
                sysPage.getIconClass(),
                sysPage.getParentId(),
                sysPage.getOrderNum(),
                sysPage.getRemark());
    }
}