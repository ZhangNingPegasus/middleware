package org.wyyt.springcloud.springbootadmin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.service.ApiService;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.RpcService;

import java.util.ArrayList;
import java.util.List;

/**
 * the service of API loading
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class ApiLoadService {
    private final RpcService rpcService;
    private final ApiService apiService;

    public ApiLoadService(final RpcService rpcService,
                          final ApiService apiService) {
        this.rpcService = rpcService;
        this.apiService = apiService;
    }

    public Api.Result updateApi(final String serviceId,
                                final String serviceUrl) {
        Api.Result result = new Api.Result();
        final List<Api> apiList = new ArrayList<>();

        try {
            int tryTimes = 0;
            String text = "";
            while (ObjectUtils.isEmpty(text)) {
                text = this.rpcService.post(String.format("%s/%s", serviceUrl, Names.API_DOCS));
                Thread.sleep(100);
                tryTimes++;
                if (tryTimes >= 10) {
                    break;
                }
            }
            if (ObjectUtils.isEmpty(text)) {
                return result;
            }
            final JSONObject jsonObject = JSON.parseObject(text);
            final Object pathsObj = jsonObject.get("paths");


            if (pathsObj instanceof JSONObject) {
                final JSONObject paths = (JSONObject) pathsObj;

                paths.forEach((path, obj) -> {
                    final Api api = new Api();
                    api.setPath(path);
                    if (obj instanceof JSONObject) {
                        final List<String> methodList = new ArrayList<>();
                        final JSONObject[] methodJson = new JSONObject[1];
                        final JSONObject jsonFun = (JSONObject) obj;
                        jsonFun.forEach((method, o) -> {
                            methodList.add(method);
                            if (null == methodJson[0] && o instanceof JSONObject) {
                                methodJson[0] = (JSONObject) o;
                            }
                        });
                        api.setName(methodJson[0].getString("summary"));
                        api.setMethod(StringUtils.join(methodList, ","));
                        api.setServiceId(serviceId);
                    }
                    apiList.add(api);
                });
            }
        } catch (final Exception exception) {
            log.info(ExceptionTool.getRootCauseMessage(exception), exception);
        }

        if (!apiList.isEmpty()) {
            result = this.apiService.save(apiList);
        }

        return result;
    }
}