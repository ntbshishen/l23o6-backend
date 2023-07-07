package org.fffd.l23o6.util.strategy.payment;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;


import jakarta.servlet.http.HttpServletResponse;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AlipaymentStrategy extends PaymentStrategy {
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    //签名方式
    private static final String SIGN_TYPE = "RSA2";
    private static final String appId="9021000122697394";
    private static final String appPrivateKey="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCY5xKbVWcI3pFUM0o7BdZ0QksouS/cGOU4nrBBbaaEq/xD/Oql5XJ+pDIu4XjwK2YFCT+H01BwO57Xe6W+Il77UtIdEkZkPR85Xnkhe9DJuXZuxeZogyne/raqO4WkrZ7/3UCjKF23Y1qRF2nq6S+0j0kx/3QFd22Esu2ddFRwP9LE1HwHMqcNu9KgloOUyCp8+LNi/6ryM2lIfZuo6dtgzALiHILw2VKZ/eX8lhVVdZCl4QfYNk6eZmjajCo/4bUx7YDguXo8NLhJtMqi4sYtEQhhmBpYJT3uiHpmoZZhENQHBe3HaYtnm64z4r8xtyElRsFAO1xBGY6gAOI3ypqxAgMBAAECggEBAJGkYOPW2FrpbbKvXECWUezRjLfQlZbnm8dv2GGqKVfsXlEAYx38TS/2BNsE5+aU4AtiAMLG9LLDB40nePt/z8tC22LXmnc2hTxnEahkQL81Sms/VDYu4b5IDwRdx/HRc0cyn7QJ6iKZn5XeoIQi887gQQ8/zWq7lHU23gEB4sA7f+zufni/SwNmabF+LhaTVcFVQW/350TZqXiUfT1Sdju7qH2d5dvP6JKgAuyL0rHjIJf7zTvpUOAkCmaXub3uV+WopHK7Qal3hMWxR4OTqK70kqPZci66hEjz8vk39kWQx5XNxIrPQM57Syfkh7GxJXzacUAjal/65/qdPNIe83ECgYEA29v+Mcab8XqYKZZiEb7ldQ/kxdh/+zPqTUzlqsy7UMXiGLeztMMek+c07t8yQLs1L10p5xpCZGbpJwo8C3cnQT0s525JKThn+GzFflwSBMWj4WPJgs382sWcviCQx20HSslZ4zhUTKUA5LSJdgju8/N7boFuUxgaQ+BNDEs1vcUCgYEAsglxGBSk6nUYHadvSEUBqkoShKN2uqcbgoVvEElFiYBNpOM+mMT47w3jgRNhcmUWfs5d6VXsJxnsB2jvugrSkSvHx5jl/Jce3Hlq4NyeBql1VuNyKDUINiJHzjbr1R5vvAps7Cw+UUTKxPT8J9JvAFOKftT52ToZ3K2QLz6tw/0CgYBKRT/SmtA1O//JFLceXYlwCSV0PFXHi1scL9zp3O1uNNrTFMONTdi3iARWqWl1eohV7rfoDPPerPEYMhnkmaTWIg9YQuZOGXLt9filXf7sQ8O/sRDyshk34ke35pJh/A/ZkLlfy7iWh6sMl3xUNmJaYKcSxoB5a4v73/rHK2UFLQKBgQCQE6v+PNapFlGJzfH0nkOWt5L82/w9WPaeTbVvjJxFhhtPkMcbgmuhqa+uySxXEw99VywQpIQTsJqd9IHsIM3+xVtLX6TfILzM10Vy65bG9WQMCMxdVW1rwZoye6emNJdfqWA0UCmuQumFMmzNHTLPJMwzj88RyD8doyfH1AVzUQKBgFsP+7Y3reCuU5upgI4MUImoNOXjpxc60zYgXOX7DDA+pHRESKKg3OpMfVBNiZ8JhrzzcAF/d16xlgkdkWwOHFLqH5ggO2xopk07d8GTCAHN9QxkzLlvsWzbqzzWMezNjncCPJ7AB9DzB8ImXvDTxN8jeDW04k5axr8wj1S4JUXB\n";
    private static final String alipayPublicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArAKbESSPTlhXuGaAm6ERGuGvzIk5FtZ7Fv+1NxWes/1K0ocLG8geld2iCKg/i4qhleFPLWaW2hllyCjY90XSi150QPNhKEgg1z4M0RX3CYa3KI+NP3lzUQIM6DXHypn0uCunNA56J8kovDcfuuNRMcZ9RnMUj3Pj2r9OdO2tuUBG9ICqvdVDSUXHYZLld1wUE5pjZaXiDEH5RpWylehTqUxBaoWlqPF9AzrdLPFlwFBbHQfyTP1cR9LgWoRq8lCHb5OsNlPXGKhevNA7l+5bpFC3OkgQnmbUHRPMvJfV0jq09V4OEOMqrdo/zkd9jcwEPSsCUbZF8NMYZ6QLmnlQAQIDAQAB";
    private static final String notifyUrl="http://ijtvz6.natappfree.ccc/alipay/notify";



    public static final AlipaymentStrategy INSTANCE = new AlipaymentStrategy();
    long[] checkListRight = {1000, 3000, 10000, 50000, Long.MAX_VALUE};
    long[] checkListLeft = {0, 1000, 3000, 10000, 50000};
    double[] creditCount = {0.001, 0.0015, 0.002, 0.0025, 0.003};
    public long[] calOrderTruePrice(long userCredit, long price) {
        // res[0] is the price, res[1] is the loseCredit, res[2] is the userCredit
        long loseCredit = 0;
        for(int i = 4; i >= 0; i--){
            if(userCredit > checkListRight[i]){
                long tem = userCredit - checkListLeft[i];
                userCredit = userCredit - tem;
                price = price - (long)(tem * creditCount[i]);
                loseCredit = loseCredit + tem;
            }
        }

        long []res = new long[3];

        res[0] = price;
        res[1] = loseCredit;
        res[2] = userCredit;
        return res;
    }
    @Override
    public String pay(final OrderEntity order,boolean credit,Long creditPrice) throws AlipayApiException {
       // System.out.println();

        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL,appId,appPrivateKey,
                FORMAT,CHARSET, alipayPublicKey,SIGN_TYPE);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(notifyUrl);
        //同步跳转地址，仅支持http/https
        request.setReturnUrl("");

        JSONObject bizContent = new JSONObject();
        //int[] res=new int[2];
       // if(credit){
        //    res=calOrderTruePrice(long userCredit, long price);
      //  }
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", order.getId().toString());
        //支付金额，最小值0.01元
        if(credit){
            bizContent.put("total_amount", creditPrice);
        }
       else{ bizContent.put("total_amount", order.getTruePrice());}
        //订单标题，不可使用特殊符号
        bizContent.put("subject", "车票");
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(bizContent.toString());
        request.setNotifyUrl(notifyUrl);
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
       // System.out.println(response.getBody());
        return response.getBody();
    }

    @Override
    public String refund(OrderEntity order) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL,appId,appPrivateKey,
                FORMAT,CHARSET, alipayPublicKey,SIGN_TYPE);
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getId().toString());
        bizContent.put("refund_amount", order.getTruePrice());
       // bizContent.put("out_request_no", order.getId().toString());

        request.setBizContent(bizContent.toString());

        AlipayTradeRefundResponse response = alipayClient.pageExecute(request);
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        System.out.println(response.getBody());
        return response.getBody();
    }



}
