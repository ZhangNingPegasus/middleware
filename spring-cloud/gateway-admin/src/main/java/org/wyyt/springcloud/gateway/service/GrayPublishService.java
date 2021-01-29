package org.wyyt.springcloud.gateway.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nepxion.discovery.common.entity.RuleEntity;
import com.nepxion.discovery.common.entity.StrategyConditionGrayEntity;
import com.nepxion.discovery.common.entity.StrategyCustomizationEntity;
import com.nepxion.discovery.common.entity.StrategyRouteEntity;
import com.nepxion.discovery.plugin.framework.parser.PluginConfigParser;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.apollo.tool.ApolloTool;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.GrayVo;
import org.wyyt.springcloud.gateway.entity.InspectVo;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.Gray;
import org.wyyt.springcloud.gateway.entity.entity.Route;
import org.wyyt.springcloud.gateway.entity.service.GrayService;
import org.wyyt.springcloud.gateway.entity.service.RouteService;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.dingtalk.Message;
import org.wyyt.tool.sql.SqlTool;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The service of Gray publish
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
public class GrayPublishService {
    private final String grayKey;
    private final ApolloTool apolloTool;
    private final GrayService grayService;
    private final RouteService routeService;
    private final GatewayService gatewayService;
    private final PluginConfigParser pluginConfigParser;
    private final DingTalkService dingTalkService;

    public GrayPublishService(final ApolloTool apolloTool,
                              final PropertyConfig propertyConfig,
                              final GrayService grayService,
                              RouteService routeService,
                              final GatewayService gatewayService,
                              final PluginConfigParser pluginConfigParser,
                              final DingTalkService dingTalkService) {
        this.apolloTool = apolloTool;
        this.grayKey = String.format("%s-%s", propertyConfig.getGatewayConsulGroup(), propertyConfig.getGatewayConsulGroup());
        this.grayService = grayService;
        this.routeService = routeService;
        this.gatewayService = gatewayService;
        this.pluginConfigParser = pluginConfigParser;
        this.dingTalkService = dingTalkService;
    }

    public List<GrayVo> listGrayVo() throws Exception {
        final List<GrayVo> result = new ArrayList<>();
        final String grayConfig = this.getGrayConfig();
        if (StringUtils.isEmpty(grayConfig)) {
            return result;
        }
        final RuleEntity ruleEntity = this.pluginConfigParser.parse(grayConfig);
        final StrategyCustomizationEntity strategyCustomizationEntity = ruleEntity.getStrategyCustomizationEntity();
        final Map<String, Integer> versionWeight = new HashMap<>();
        final Map<String, Gray> grayMap = this.grayService.listMap();

        final List<StrategyConditionGrayEntity> strategyConditionGrayEntityList = strategyCustomizationEntity.getStrategyConditionGrayEntityList();
        for (final StrategyConditionGrayEntity strategyConditionGrayEntity : strategyConditionGrayEntityList) {
            versionWeight.putAll(strategyConditionGrayEntity.getVersionWeightEntity().getWeightMap());
        }

        final List<StrategyRouteEntity> strategyRouteEntityList = strategyCustomizationEntity.getStrategyRouteEntityList();
        for (final StrategyRouteEntity strategyRouteEntity : strategyRouteEntityList) {
            final GrayVo grayVo = new GrayVo();
            grayVo.setGrayId(strategyRouteEntity.getId());
            grayVo.setValue(strategyRouteEntity.getValue());
            grayVo.setWeight(versionWeight.get(strategyRouteEntity.getId()));
            grayVo.setDescription(grayMap.containsKey(grayVo.getGrayId()) ? grayMap.get(grayVo.getGrayId()).getDescription() : "");
            result.add(grayVo);
        }
        return result;
    }

    @TranSave
    public void publish(final List<GrayVo> grayVoList) throws Exception {
        final StringBuilder content = new StringBuilder();
        final List<Gray> grayList = new ArrayList<>();
        int i = 1;
        for (final GrayVo grayVo : grayVoList) {
            final Gray gray = new Gray();
            gray.setGrayId(String.format("route_%s", i++));
            grayVo.setGrayId(gray.getGrayId());
            gray.setDescription(grayVo.getDescription());
            grayList.add(gray);
            content.append(toRouteStr(grayVo));
        }
        this.grayService.update(grayList);

        if (grayVoList.isEmpty()) {
            this.updateGrayConfig("");
        } else {
            final String xml = this.toGlobalHeaderConfig(grayVoList);
            this.updateGrayConfig(xml);
        }

        final Message message = new Message();
        message.setMsgtype("text");
        if (ObjectUtils.isEmpty(content.toString())) {
            message.setText(new Message.Text(String.format("灰度发布已全部撤销, 将会轮询调用所有版本\n发布时间: %s", DateTool.format(new Date()))));
        } else {
            final String prefix = String.format("灰度发布已生效, 详情如下:\n发布时间: %s\n\n---------------------------\n\n", DateTool.format(new Date()));
            content.append("当前状态: OK");
            message.setText(new Message.Text(prefix.concat(content.toString().trim())));
        }
        this.dingTalkService.send(message);
    }

    public String inspect(final List<InspectVo> inspectVos) throws Exception {
        if (null == inspectVos || inspectVos.isEmpty()) {
            return "";
        }

        final Set<String> serviceIdList = new HashSet<>(inspectVos.size());
        for (InspectVo vo : inspectVos) {
            serviceIdList.add(String.format("\"%s\"", vo.getService().trim()));
        }

        final List<String> ndVersionList = new ArrayList<>(inspectVos.size());
        for (final InspectVo inspectVo : inspectVos) {
            ndVersionList.add(String.format("\"%s\":\"%s\"", inspectVo.getService(), inspectVo.getVersion()));
        }

        final Map<String, String> headers = new HashMap<>();
        headers.put(Names.N_D_VERSION, String.format("{%s}", StringUtils.join(ndVersionList, ",")));

        final URI gatewayUri = this.gatewayService.getGatewayUri();
        if (null == gatewayUri) {
            return "";
        }

        final Route firstRoute = this.getFirstServiceName(inspectVos.stream().map(InspectVo::getService).collect(Collectors.toList()));

        if (null == firstRoute) {
            return "缺少网关路由配置,请先进行路由配置";
        }

        serviceIdList.remove(String.format("\"%s\"", firstRoute.getServiceName()));

        final String inspect = Unirest.post(String.format("%s/%s/inspector/inspect", gatewayUri.toString(), firstRoute.getPathPredicate()))
                .header("Content-Type", "application/json")
                .headers(headers)
                .body(String.format("{\"serviceIdList\":%s}", Arrays.toString(serviceIdList.stream().sorted(Comparator.naturalOrder()).toArray())))
                .asString()
                .getBody();

        return formatInspect(inspect);
    }

    public String globalInspect() throws Exception {
        final String grayConfig = this.getGrayConfig();
        final RuleEntity ruleEntity = this.pluginConfigParser.parse(grayConfig);

        final Set<String> serviceIdSet = new HashSet<>();
        for (final StrategyRouteEntity strategyRouteEntity : ruleEntity.getStrategyCustomizationEntity().getStrategyRouteEntityList()) {
            final String value = strategyRouteEntity.getValue();
            final Map<String, String> map = JSON.parseObject(value, Map.class);
            serviceIdSet.addAll(map.keySet());
        }

        if (serviceIdSet.isEmpty()) {
            return "";
        }

        final List<String> serviceIdList = serviceIdSet.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());

        final Route firstRoute = this.getFirstServiceName(serviceIdList);

        if (null == firstRoute) {
            return "缺少网关路由配置,请先进行路由配置";
        }

        serviceIdList.remove(firstRoute.getServiceName());
        final URI gatewayUri = this.gatewayService.getGatewayUri();
        final String inspect = Unirest.post(String.format("%s/%s/inspector/inspect", gatewayUri.toString(), firstRoute.getPathPredicate()))
                .header("Content-Type", "application/json")
                .body(String.format("{\"serviceIdList\":%s}", Arrays.toString(serviceIdList.stream().map(p -> String.format("\"%s\"", p)).toArray())))
                .asString()
                .getBody();

        return formatInspect(inspect);
    }

    private Route getFirstServiceName(final List<String> serviceNameList) {
        final List<Route> routeList = this.routeService.list();
        if (null == routeList || routeList.isEmpty()) {
            return null;
        }

        for (final String serviceName : serviceNameList) {
            final Optional<Route> first = routeList.stream().filter(p -> p.getUri().endsWith(serviceName)).findFirst();
            if (first.isPresent()) {
                final Route route = first.get();
                route.setServiceName(serviceName);
                return route;
            }
        }

        return null;
    }

    private String toGlobalHeaderConfig(final List<GrayVo> grayVoList) throws IOException {
        final Document doc = DocumentHelper.createDocument();
        final Element rule = doc.addElement("rule");
        final Element customization = rule.addElement("strategy-customization");
        final Element conditions = customization.addElement("conditions").addAttribute("type", "gray");
        final Element routes = customization.addElement("routes");

        final List<String> conditionList = new ArrayList<>();
        for (final GrayVo grayVo : grayVoList) {
            conditionList.add(String.format("%s=%s", grayVo.getGrayId(), grayVo.getWeight()));
            routes
                    .addElement("route")
                    .addAttribute("id", grayVo.getGrayId())
                    .addAttribute("type", "version")
                    .addText(grayVo.getValue());
        }

        conditions
                .addElement("condition")
                .addAttribute("id", "condition1")
                .addAttribute("version-id", StringUtils.join(conditionList, ";"));

        return formatXml(doc);
    }

    private void updateGrayConfig(String config) {
        this.apolloTool.updateConfig(this.grayKey, config);
    }

    private String getGrayConfig() throws Exception {
        return this.apolloTool.getConfig(this.grayKey);
    }

    private static String formatXml(final Document document) throws IOException {
        try (final StringWriter out = new StringWriter()) {
            final OutputFormat formater = OutputFormat.createPrettyPrint();
            formater.setEncoding(StandardCharsets.UTF_8.name());
            XMLWriter writer = null;
            try {
                writer = new XMLWriter(out, formater);
                writer.write(document);
            } finally {
                if (null != writer) {
                    writer.close();
                }
            }
            return out.toString().replace("\n\n", "");
        }
    }

    private static String formatInspect(final String inspect) {
        if (StringUtils.isEmpty(inspect)) {
            return "";
        }

        final JSONObject jsonObject = JSON.parseObject(inspect);
        final Integer status = jsonObject.getInteger("status");
        if (null != status && 200 != status) {
            return inspect.trim();
        }
        final String chain = jsonObject.getString("result");
        if (StringUtils.isEmpty(chain)) {
            return inspect;
        }

        final StringBuilder result = new StringBuilder();

        final List<Map<String, String>> content = new ArrayList<>();

        final String[] all = chain.split("->");

        for (String value : all) {
            final Map<String, String> map = new HashMap<>();
            final String[] split = value.split("]\\[");

            for (final String s : split) {
                String kv = SqlTool.removeQualifier(s.trim(), "[").trim();
                kv = SqlTool.removeQualifier(kv, "]");

                final String[] pair = kv.split("=");
                map.put(pair[0].trim(), pair[1].trim());
            }
            content.add(map);
        }

        for (final Map<String, String> map : content) {
            result.append(String.format("%s(%s)&nbsp;&nbsp;-&nbsp;&nbsp;%s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-><br/>",
                    map.get("ID"),
                    map.get("H"),
                    map.get("V")));
        }

        return SqlTool.removeQualifier(result.toString(), "-><br/>");
    }

    private static String toRouteStr(final GrayVo grayVo) {
        if (null == grayVo) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
        final JSONObject jsonObject = JSON.parseObject(grayVo.getValue());
        result.append(String.format("描述信息: %s\n", grayVo.getDescription()));
        for (Map.Entry<String, Object> pair : jsonObject.entrySet()) {
            result.append(String.format("服务名: %s;    版本号: %s\n", pair.getKey(), pair.getValue()));
        }
        result.append(String.format("流量比例: %s%%\n", grayVo.getWeight()));
        result.append("\n---------------------------\n\n");
        return result.toString();
    }
}