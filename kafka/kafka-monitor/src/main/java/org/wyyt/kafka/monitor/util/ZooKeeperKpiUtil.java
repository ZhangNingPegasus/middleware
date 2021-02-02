package org.wyyt.kafka.monitor.util;


import lombok.extern.slf4j.Slf4j;
import org.wyyt.kafka.monitor.entity.po.ZooKeeperKpi;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.resource.ResourceTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * the utils class for ZooKeeper.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class ZooKeeperKpiUtil {
    private static final String zk_avg_latency = "zk_avg_latency";
    private static final String zk_packets_received = "zk_packets_received";
    private static final String zk_packets_sent = "zk_packets_sent";
    private static final String zk_num_alive_connections = "zk_num_alive_connections";
    private static final String zk_outstanding_requests = "zk_outstanding_requests";
    private static final String zk_open_file_descriptor_count = "zk_open_file_descriptor_count";
    private static final String zk_max_file_descriptor_count = "zk_max_file_descriptor_count";

    public static ZooKeeperKpi listKpi(final String ip,
                                       final int port) {
        final ZooKeeperKpi result = new ZooKeeperKpi();
        Socket sock;
        try {
            sock = new Socket(ip, port);
        } catch (Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            return result;
        }
        BufferedReader reader = null;
        OutputStream outstream = null;
        try {
            outstream = sock.getOutputStream();
            outstream.write("mntr".getBytes());
            outstream.flush();
            sock.shutdownOutput();

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] rs = line.split("\\s+");
                try {
                    switch (rs[0]) {
                        case zk_avg_latency:
                            result.setZkAvgLatency(rs[1]);
                            break;
                        case zk_packets_received:
                            result.setZkPacketsReceived(rs[1]);
                            break;
                        case zk_packets_sent:
                            result.setZkPacketsSent(rs[1]);
                            break;
                        case zk_num_alive_connections:
                            result.setZkNumAliveConnections(rs[1]);
                            break;
                        case zk_outstanding_requests:
                            result.setZkOutstandingRequests(rs[1]);
                            break;
                        case zk_open_file_descriptor_count:
                            result.setZkOpenFileDescriptorCount(rs[1]);
                            break;
                        case zk_max_file_descriptor_count:
                            result.setZkMaxFileDescriptorCount(rs[1]);
                            break;
                        default:
                            break;
                    }
                } catch (Exception ex) {
                    log.error(ExceptionTool.getRootCauseMessage(ex), ex);
                }
            }
        } catch (Exception ex) {
            log.error(ExceptionTool.getRootCauseMessage(ex), ex);
            return result;
        } finally {
            ResourceTool.closeQuietly(sock);
            ResourceTool.closeQuietly(reader);
            ResourceTool.closeQuietly(outstream);
        }
        return result;
    }
}