package org.springcloud.sms.controller;

import com.montnets.mwgate.common.RPT;
import com.montnets.mwgate.smsutil.SmsSendConn;

import java.util.ArrayList;
import java.util.List;

public class RecvRptThread extends Thread {

    // 短信处理对象,在这个方法中调用获取状态报告功能
    private SmsSendConn smsSendConn = null;
    // 用户账号
    private String userid = null;
    // 每次请求想要获取状态报告的最大条数
    private int retsize = 0;

    /**
     * 构造函数
     *
     * @param smsSendConn 短信处理对象
     * @param userid      用户账号
     */
    public RecvRptThread(SmsSendConn smsSendConn, String userid, int retsize) {
        // 短信处理对象
        this.smsSendConn = smsSendConn;
        // 用户账号
        this.userid = userid;
        // 每次请求想要获取状态报告的最大条数
        this.retsize = retsize;
    }


    //状态报告记录:msgid:3160778174820862799,custid:3160778174820862799,pknum:1,pktotal:1,mobile:17337979610,spno:10690044604472370,exno:,stime:2021-02-23 23:21:28,rtime:2021-02-24 10:14:52,status:0,errcode:DELIVRD,exdata:,errdesc:success
    //状态报告记录:msgid:3182064991622704228,custid:20160929194950100001,pknum:1,pktotal:1,mobile:18207131101,spno:11,exno:11,stime:2021-02-24 10:15:29,rtime:2021-02-24 10:15:29,status:2,errcode:M2:0057,exdata:abcdef,errdesc:fail

    /**
     * 获取短信状态报告线程的方法
     */
    public void run() {
        // 状态报告集合 本集合临时存储状态报告，需要将收到的状态报告保存在一个队列中，由另外一个线程去处理
        List<RPT> rpts = new ArrayList<RPT>();
        // 返回值
        int result = -310099;
        //状态报告对象声明
        RPT rpt = null;
        // 循环调用获取状态报告的方法
        while (true) {
            try {
                // 初始化返回值
                result = -310099;
                // 清空状态报告集合中的对象
                rpts.clear();
                // 调用获取状态报告接口
                result = smsSendConn.getRpt(userid, retsize, rpts);
                // 如果获取状态报告成功，并且有状态报告
                if (result == 0 && rpts != null && rpts.size() > 0) {
                    // 有状态报告
                    // 添加到状态报告队列
                    // add rpt queue
                    // 这里不要做复杂的耗时的处理，将收到的状态报告保存在一个队列中，由另外一个线程去处理。

                    // 代码示例是将状态报告信息打印出来
                    System.out.println("获取状态报告成功！获取到的状态报告有" + rpts.size()
                            + "条记录。");
                    for (int i = 0; i < rpts.size(); i++) {
                        rpt = rpts.get(i);
                        System.out.println("状态报告记录:" + "msgid:"
                                + rpt.getMsgid() + ",custid:" + rpt.getCustid()
                                + ",pknum:" + rpt.getPknum() + ",pktotal:"
                                + rpt.getPktotal() + ",mobile:"
                                + rpt.getMobile() + ",spno:" + rpt.getSpno()
                                + ",exno:" + rpt.getExno() + ",stime:"
                                + rpt.getStime() + ",rtime:" + rpt.getRtime()
                                + ",status:" + rpt.getStatus() + ",errcode:"
                                + rpt.getErrcode() + ",exdata:"
                                + rpt.getExdata() + ",errdesc:"
                                + rpt.getErrdesc());
                    }

                    // 继续循环
                    continue;
                } else {
                    // 如果获取状态报告失败，则将错误码打印
                    if (result != 0) {
                        System.out.println("获取状态报告失败，错误码为:" + result);
                    }

                    // 没有状态报告，延时5秒以上
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 循环出现异常，暂停5秒
                try {
                    Thread.sleep(5000L);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}