package cn.inkroom.study.cloud.eureka.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.netflix.appinfo.AmazonInfo;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Pair;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.cluster.PeerEurekaNode;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import com.netflix.eureka.resources.StatusResource;
import com.netflix.eureka.util.StatusInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author inkbox
 * @date 2021/7/20
 */
@Controller
@RequestMapping("${eureka.dashboard.path:/}")
public class ConsumeEurekaController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${eureka.dashboard.path:/}")
    private String dashboardPath = "";

    private ApplicationInfoManager applicationInfoManager;

    public ConsumeEurekaController(ApplicationInfoManager applicationInfoManager) {
        this.applicationInfoManager = applicationInfoManager;
    }

    @GetMapping("login")
    public String login(HttpServletRequest request) {
        request.setAttribute("error", request.getParameter("error") != null);
        return "login/login";
    }


    @RequestMapping(method = RequestMethod.GET)
    public String status(HttpServletRequest request, Map<String, Object> model) {
        populateBase(request, model);
        populateApps(model);
        StatusInfo statusInfo;
        try {
            statusInfo = new StatusResource().getStatusInfo();
        } catch (Exception e) {
            statusInfo = StatusInfo.Builder.newBuilder().isHealthy(false).build();
        }
        model.put("statusInfo", statusInfo);
        populateInstanceInfo(model, statusInfo);
        filterReplicas(model, statusInfo);
        return "eureka/status";
    }

    @RequestMapping(value = "/lastn", method = RequestMethod.GET)
    public String lastn(HttpServletRequest request, Map<String, Object> model) {
        populateBase(request, model);
        PeerAwareInstanceRegistryImpl registry = (PeerAwareInstanceRegistryImpl) getRegistry();
        ArrayList<Map<String, Object>> lastNCanceled = new ArrayList<>();
        List<Pair<Long, String>> list = registry.getLastNCanceledInstances();
        for (Pair<Long, String> entry : list) {
            lastNCanceled.add(registeredInstance(entry.second(), entry.first()));
        }
        model.put("lastNCanceled", lastNCanceled);
        list = registry.getLastNRegisteredInstances();
        ArrayList<Map<String, Object>> lastNRegistered = new ArrayList<>();
        for (Pair<Long, String> entry : list) {
            lastNRegistered.add(registeredInstance(entry.second(), entry.first()));
        }
        model.put("lastNRegistered", lastNRegistered);
        return "eureka/lastn";
    }

    private Map<String, Object> registeredInstance(String id, long date) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("date", new Date(date));
        return map;
    }

    protected void populateBase(HttpServletRequest request, Map<String, Object> model) {
        model.put("time", new Date());
        model.put("basePath", "/");
        model.put("dashboardPath",
                this.dashboardPath.equals("/") ? "" : this.dashboardPath);
        populateHeader(model);
        populateNavbar(request, model);
    }

    private void populateHeader(Map<String, Object> model) {
        model.put("currentTime", StatusResource.getCurrentTimeAsString());
        model.put("upTime", StatusInfo.getUpTime());
        model.put("environment",
                ConfigurationManager.getDeploymentContext().getDeploymentEnvironment());
        model.put("datacenter",
                ConfigurationManager.getDeploymentContext().getDeploymentDatacenter());
        PeerAwareInstanceRegistry registry = getRegistry();
        model.put("registry", registry);
        model.put("isBelowRenewThresold", registry.isBelowRenewThresold() == 1);
        DataCenterInfo info = applicationInfoManager.getInfo().getDataCenterInfo();
        if (info.getName() == DataCenterInfo.Name.Amazon) {
            AmazonInfo amazonInfo = (AmazonInfo) info;
            model.put("amazonInfo", amazonInfo);
            model.put("amiId", amazonInfo.get(AmazonInfo.MetaDataKey.amiId));
            model.put("availabilityZone",
                    amazonInfo.get(AmazonInfo.MetaDataKey.availabilityZone));
            model.put("instanceId", amazonInfo.get(AmazonInfo.MetaDataKey.instanceId));
        }
    }

    private PeerAwareInstanceRegistry getRegistry() {
        return getServerContext().getRegistry();
    }

    private EurekaServerContext getServerContext() {
        return EurekaServerContextHolder.getInstance().getServerContext();
    }

    private void populateNavbar(HttpServletRequest request, Map<String, Object> model) {
        Map<String, String> replicas = new LinkedHashMap<>();
        List<PeerEurekaNode> list = getServerContext().getPeerEurekaNodes()
                .getPeerNodesView();
        for (PeerEurekaNode node : list) {
            try {
                URI uri = new URI(node.getServiceUrl());
                String href = scrubBasicAuth(node.getServiceUrl());
                replicas.put(uri.getHost(), href);
            } catch (Exception ex) {
                // ignore?
            }
        }
        model.put("replicas", replicas.entrySet());
    }

    private void populateApps(Map<String, Object> model) {
        List<Application> sortedApplications = getRegistry().getSortedApplications();
        ArrayList<Map<String, Object>> apps = new ArrayList<>();
        for (Application app : sortedApplications) {
            LinkedHashMap<String, Object> appData = new LinkedHashMap<>();
            apps.add(appData);
            appData.put("name", app.getName());
            Map<String, Integer> amiCounts = new HashMap<>();
            Map<InstanceInfo.InstanceStatus, List<Map<String, Object>>> instancesByStatus = new HashMap<>();
            Map<String, Integer> zoneCounts = new HashMap<>();
            for (InstanceInfo info : app.getInstances()) {
                String id = info.getId();
                String url = info.getStatusPageUrl();
                InstanceInfo.InstanceStatus status = info.getStatus();
                String ami = "n/a";
                String zone = "";
                if (info.getDataCenterInfo().getName() == DataCenterInfo.Name.Amazon) {
                    AmazonInfo dcInfo = (AmazonInfo) info.getDataCenterInfo();
                    ami = dcInfo.get(AmazonInfo.MetaDataKey.amiId);
                    zone = dcInfo.get(AmazonInfo.MetaDataKey.availabilityZone);
                }
                Integer count = amiCounts.get(ami);
                if (count != null) {
                    amiCounts.put(ami, count + 1);
                } else {
                    amiCounts.put(ami, 1);
                }
                count = zoneCounts.get(zone);
                if (count != null) {
                    zoneCounts.put(zone, count + 1);
                } else {
                    zoneCounts.put(zone, 1);
                }
                List<Map<String, Object>> list = instancesByStatus
                        .computeIfAbsent(status, k -> new ArrayList<>());
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", id);
                map.put("url", url);
                map.put("metadata", info.getMetadata());
//                info.getMetadata().forEach((s, s2) -> map.put("m" + s, s2));
                list.add(map);
            }
            appData.put("amiCounts", amiCounts.entrySet());
            appData.put("zoneCounts", zoneCounts.entrySet());
            ArrayList<Map<String, Object>> instanceInfos = new ArrayList<>();
            appData.put("instanceInfos", instanceInfos);
            for (Map.Entry<InstanceInfo.InstanceStatus, List<Map<String, Object>>> entry : instancesByStatus
                    .entrySet()) {
                List<Map<String, Object>> value = entry.getValue();
                InstanceInfo.InstanceStatus status = entry.getKey();
                LinkedHashMap<String, Object> instanceData = new LinkedHashMap<>();
                instanceInfos.add(instanceData);
                instanceData.put("status", entry.getKey());
                ArrayList<Map<String, Object>> instances = new ArrayList<>();
                instanceData.put("instances", instances);
                instanceData.put("isNotUp", status != InstanceInfo.InstanceStatus.UP);

                // TODO

                /*
                 * if(status != InstanceInfo.InstanceStatus.UP){
                 * buf.append("<font color=red size=+1><b>"); }
                 * buf.append("<b>").append(status
                 * .name()).append("</b> (").append(value.size()).append(") - ");
                 * if(status != InstanceInfo.InstanceStatus.UP){
                 * buf.append("</font></b>"); }
                 */


                for (Map<String, Object> p : value) {

                    instances.add(p);

                    String url = ((String) p.getOrDefault("url", ""));
                    boolean isHref = url != null && url.startsWith("http");
                    p.put("isHref", isHref);
                    /*
                     * String id = p.first(); String url = p.second(); if(url != null &&
                     * url.startsWith("http")){
                     * buf.append("<a href=\"").append(url).append("\">"); }else { url =
                     * null; } buf.append(id); if(url != null){ buf.append("</a>"); }
                     * buf.append(", ");
                     */
                }
            }
            // out.println("<td>" + buf.toString() + "</td></tr>");
        }
        model.put("apps", apps);
    }

    private void populateInstanceInfo(Map<String, Object> model, StatusInfo statusInfo) {
        InstanceInfo instanceInfo = statusInfo.getInstanceInfo();
        Map<String, String> instanceMap = new HashMap<>();
        instanceMap.put("ipAddr", instanceInfo.getIPAddr());
        instanceMap.put("status", instanceInfo.getStatus().toString());
        if (instanceInfo.getDataCenterInfo().getName() == DataCenterInfo.Name.Amazon) {
            AmazonInfo info = (AmazonInfo) instanceInfo.getDataCenterInfo();
            instanceMap.put("availability-zone",
                    info.get(AmazonInfo.MetaDataKey.availabilityZone));
            instanceMap.put("public-ipv4", info.get(AmazonInfo.MetaDataKey.publicIpv4));
            instanceMap.put("instance-id", info.get(AmazonInfo.MetaDataKey.instanceId));
            instanceMap.put("public-hostname",
                    info.get(AmazonInfo.MetaDataKey.publicHostname));
            instanceMap.put("ami-id", info.get(AmazonInfo.MetaDataKey.amiId));
            instanceMap.put("instance-type",
                    info.get(AmazonInfo.MetaDataKey.instanceType));
        }
        model.put("instanceInfo", instanceMap);
    }

    protected void filterReplicas(Map<String, Object> model, StatusInfo statusInfo) {
        Map<String, String> applicationStats = statusInfo.getApplicationStats();
        if (applicationStats.get("registered-replicas").contains("@")) {
            applicationStats.put("registered-replicas",
                    scrubBasicAuth(applicationStats.get("registered-replicas")));
        }
        if (applicationStats.get("unavailable-replicas").contains("@")) {
            applicationStats.put("unavailable-replicas",
                    scrubBasicAuth(applicationStats.get("unavailable-replicas")));
        }
        if (applicationStats.get("available-replicas").contains("@")) {
            applicationStats.put("available-replicas",
                    scrubBasicAuth(applicationStats.get("available-replicas")));
        }
        model.put("applicationStats", applicationStats);
    }

    private String scrubBasicAuth(String urlList) {
        String[] urls = urlList.split(",");
        StringBuilder filteredUrls = new StringBuilder();
        for (String u : urls) {
            if (u.contains("@")) {
                filteredUrls.append(u, 0, u.indexOf("//") + 2)
                        .append(u.substring(u.indexOf("@") + 1)).append(",");
            } else {
                filteredUrls.append(u).append(",");
            }
        }
        return filteredUrls.substring(0, filteredUrls.length() - 1);
    }

}
