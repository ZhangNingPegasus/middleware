package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.entity.dto.SysPage;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;
import org.wyyt.kafka.monitor.entity.vo.PageVo;
import org.wyyt.kafka.monitor.mapper.SysPageMapper;
import org.wyyt.tool.bean.BeanTool;

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
public class SysPageService extends ServiceImpl<SysPageMapper, SysPage> {

    private final SysPermissionService sysPermissionService;

    public SysPageService(final SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    @TranRead
    public IPage<PageVo> list(final Integer pageNum,
                              final Integer pageSize,
                              @Nullable final String name) {
        final IPage<PageVo> page = new Page<>(pageNum, pageSize);
        final List<PageVo> list = this.baseMapper.list(page, name);
        page.setRecords(list);
        return page;
    }

    @TranRead
    public Long getMaxOrderNum(final Long parentId) {
        return this.baseMapper.getMaxOrderNum(parentId);
    }

    @TranRead
    public void fillPages(final AdminVo adminVo) {
        if (null == adminVo) {
            return;
        }
        adminVo.setPermissions(getPages(adminVo));
        adminVo.getPermissions().sort((o1, o2) -> (int) (o1.getOrderNum() - o2.getOrderNum()));
    }

    @TranRead
    List<PageVo> getPages(final AdminVo adminVo) {
        final List<SysPage> allPages = list(new QueryWrapper<SysPage>().lambda().eq(SysPage::getIsMenu, true).orderByAsc(SysPage::getParentId).orderByAsc(SysPage::getOrderNum));
        final Map<Long, SysPage> allPageMap = toMap(allPages);

        // 非顶级集合
        final List<PageVo> nonRootPageList = new ArrayList<>();
        // 顶级集合
        final List<PageVo> rootPageList = new ArrayList<>();
        final Map<Long, PageVo> permission = this.sysPermissionService.getPermissionPages(adminVo.getId());

        for (final SysPage sysPage : allPages) {
            if (!adminVo.getSysRole().getSuperAdmin() && !permission.containsKey(sysPage.getId())) {
                continue;
            }
            if (0 == sysPage.getParentId()) {
                final PageVo rootPageVo = BeanTool.copy(sysPage, PageVo.class);
                final PageVo permissionPageVo = permission.get(rootPageVo.getId());
                if (permissionPageVo != null) {
                    rootPageVo.setCanInsert(permissionPageVo.getCanInsert());
                    rootPageVo.setCanDelete(permissionPageVo.getCanDelete());
                    rootPageVo.setCanUpdate(permissionPageVo.getCanUpdate());
                    rootPageVo.setCanSelect(permissionPageVo.getCanSelect());
                }
                rootPageList.add(rootPageVo);
            } else {
                nonRootPageList.add(BeanTool.copy(sysPage, PageVo.class));
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
                    final PageVo rootPageVo = BeanTool.copy(sysPageParent, PageVo.class);
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
            this.filter(rootPageList);
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
            if (StringUtils.isEmpty(page.getUrl()) && (null == page.getChildren() || page.getChildren().size() < 1)) {
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
            if (adminVo.getSysRole().getSuperAdmin() || StringUtils.isEmpty(p.getUrl()) || permission.containsKey(p.getId())) {
                // 放入set, 递归循环时可以跳过这个页面，提高循环效率
                set.add(p.getId());
                // 递归获取当前类目的子类目
                getChild(adminVo, permission, p, childrenPageList, set);

                if (permission.containsKey(p.getId())) {
                    PageVo page = permission.get(p.getId());
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
}