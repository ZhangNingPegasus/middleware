package org.wyyt.springcloud.permission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.wyyt.springcloud.entity.dto.ApiDto;

import java.util.List;

@Slf4j
public class PermissionPersister implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private PermissionAutoScanProxy permissionAutoScanProxy;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() instanceof AnnotationConfigApplicationContext) {
            final List<ApiDto> permissions = this.permissionAutoScanProxy.getPermissions();
            System.out.println(permissions);
            // 权限数据入库
        }
    }
}