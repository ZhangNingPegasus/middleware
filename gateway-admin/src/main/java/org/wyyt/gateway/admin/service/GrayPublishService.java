package org.wyyt.gateway.admin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nepxion.discovery.common.entity.RuleEntity;
import com.nepxion.discovery.common.entity.StrategyConditionGrayEntity;
import com.nepxion.discovery.common.entity.StrategyCustomizationEntity;
import com.nepxion.discovery.common.entity.StrategyRouteEntity;
import com.nepxion.discovery.plugin.framework.config.PluginConfigParser;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;
import org.wyyt.apollo.tool.ApolloTool;
import org.wyyt.gateway.admin.business.entity.Gray;
import org.wyyt.gateway.admin.business.service.GrayService;
import org.wyyt.gateway.admin.config.PropertyConfig;
import org.wyyt.gateway.admin.entity.EndpointVo;
import org.wyyt.gateway.admin.entity.GrayVo;
import org.wyyt.gateway.admin.entity.InspectVo;
import org.wyyt.tool.sql.SqlTool;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * The service of Gray publish
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
@Service
public class GrayPublishService {
    private final String grayKey;
    private final ApolloTool apolloTool;
    private final GrayService grayService;
    private final GatewayService gatewayService;
    private final PluginConfigParser pluginConfigParser;

    public GrayPublishService(final ApolloTool apolloTool,
                              final PropertyConfig propertyConfig,
                              final GrayService grayService,
                              final GatewayService gatewayService,
                              final PluginConfigParser pluginConfigParser) {
        this.apolloTool = apolloTool;
        this.grayKey = String.format("%s-%s", propertyConfig.getGatewayConsulGroup(), propertyConfig.getGatewayConsulGroup());
        this.grayService = grayService;
        this.gatewayService = gatewayService;
        this.pluginConfigParser = pluginConfigParser;
    }

    public List<GrayVo> listGrayVo() {
        final List<GrayVo> result = new ArrayList<>();
        final String grayConfig = this.getGrayConfig();
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
            grayVo.setId(strategyRouteEntity.getId());
            grayVo.setValue(strategyRouteEntity.getValue());
            grayVo.setWeight(versionWeight.get(strategyRouteEntity.getId()));
            grayVo.setDescription(grayMap.containsKey(grayVo.getId()) ? grayMap.get(grayVo.getId()).getDescription() : "");
            result.add(grayVo);
        }
        return result;
    }

    public void updateGrayConfig(final List<GrayVo> grayVoList) {
        this.updateGrayConfig("");
    }

    public String inspect(final List<InspectVo> inspectVos) throws Exception {
        if (null == inspectVos || inspectVos.isEmpty()) {
            return "";
        }

        final List<String> serviceIdList = new ArrayList<>(inspectVos.size());
        for (int i = 1; i < inspectVos.size(); i++) {
            serviceIdList.add(String.format("\"%s\"", inspectVos.get(i).getService().trim()));
        }

        final List<String> ndVersionList = new ArrayList<>(inspectVos.size());
        for (final InspectVo inspectVo : inspectVos) {
            ndVersionList.add(String.format("\"%s\":\"%s\"", inspectVo.getService(), inspectVo.getVersion()));
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("n-d-version", String.format("{%s}", org.apache.commons.lang3.StringUtils.join(ndVersionList, ",")));

        final EndpointVo gatewayUri = this.gatewayService.getGatewayUri();

        final String inspect = Unirest.post(String.format("http://%s:%s/%s/inspector/inspect", gatewayUri.getAddress(), gatewayUri.getPort(), inspectVos.get(0).getService()))
                .header("Content-Type", "application/json")
                .headers(headers)
                .body(String.format("{\"serviceIdList\":%s}", Arrays.toString(serviceIdList.toArray())))
                .asString()
                .getBody();

        return formatInspect(inspect);
    }

    public String toGlobalHeaderConfig(final List<GrayVo> grayVoList) throws IOException {
        final Document doc = DocumentHelper.createDocument();
        final Element rule = doc.addElement("rule");
        final Element customization = rule.addElement("strategy-customization");
        final Element conditions = customization.addElement("conditions").addAttribute("type", "gray");
        final Element routes = customization.addElement("routes");

        final List<String> conditionList = new ArrayList<>();
        for (final GrayVo grayVo : grayVoList) {
            conditionList.add(String.format("%s=%s", grayVo.getId(), grayVo.getWeight()));
            routes
                    .addElement("route")
                    .addAttribute("id", grayVo.getId())
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

    public void clearGrayConfig() {
        this.apolloTool.clearConfig(this.grayKey);
    }

    private String getGrayConfig() {
        try {
            return this.apolloTool.getConfig(this.grayKey);
        } catch (Exception exception) {
            return null;
        }
    }

    private static String formatXml(final Document document) throws IOException {
        try (final StringWriter out = new StringWriter()) {
            final OutputFormat formater = OutputFormat.createPrettyPrint();
            formater.setEncoding("UTF-8");
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

    public static String formatInspect(final String inspect) {
        if (StringUtils.isEmpty(inspect)) {
            return "";
        }

        final JSONObject jsonObject = JSON.parseObject(inspect);
        final String chain = jsonObject.getString("result");

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

        return result.toString();
    }
}