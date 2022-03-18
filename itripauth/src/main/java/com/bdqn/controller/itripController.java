package com.bdqn.controller;

import cn.itrip.dao.itripHotel.ItripHotelMapper;
import cn.itrip.dao.itripUser.ItripUserMapper;
import cn.itrip.pojo.ItripHotel;
import cn.itrip.pojo.ItripUser;
import cn.itrip.pojo.ItripUserVO;
import com.alibaba.fastjson.JSONArray;
import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import common.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


@Controller
public class itripController {
    @Resource
    RedisUtil redis;
 @Resource
 ItripHotelMapper dao;
 @Resource
   ItripUserMapper dao1;
 @Resource
    TokenBiz biz;
    @RequestMapping(value = "/api/dologin",produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object login(String name, String password, HttpServletRequest request) throws Exception {
        Map b =new HashMap();
        b.put("a",name);
        b.put("b",password);
        ItripUser user =dao1.getItripUserListByMap(b);
      /*  return JSONArray.toJSONString(user);*/
        if(user!=null) {

            String token = biz.generateToken(request.getHeader("User-Agent"), user);
            redis.setRedis(token, JSONArray.toJSONString(user));

            ItripTokenVO tokenVO = new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis() * 3600 * 2, Calendar.getInstance().getTimeInMillis());
            return DtoUtil.returnDataSuccess(tokenVO);

        }
       return DtoUtil.returnFail("用户登录失败","1000");
    }

  @RequestMapping(value = "clist",produces = "application/json; charset=utf-8")
    @ResponseBody
    public String glist() throws Exception {
        ItripHotel list =dao.getItripHotelById(new Long(56));
        return JSONArray.toJSONString(list);

    }
    @RequestMapping("/clist1")
    public String a(){

        return "clist1";
    }
    @Resource
    ItripUserMapper dao4;
    @RequestMapping(value = "/api/registerbyphone",produces = "application/json; charset=utf-8")
    @ResponseBody
    public Dto Register(@RequestBody ItripUserVO vo) throws Exception {
     ItripUser user =new ItripUser();
      user.setUserCode(vo.getUserCode());
      user.setUserPassword(vo.getUserPassword());
      user.setUserName(vo.getUserName());
      dao4.insertItripUser(user);
        Random Random =new Random(4);
        int a =Random.nextInt(9999);

        sendSms(vo.getUserCode(),""+a);
        redis.setRedis(vo.getUserCode(),""+a);
        return DtoUtil.returnSuccess();
    }

    @RequestMapping(value = "/api/validatephone",produces = "application/json; charset=utf-8")
    @ResponseBody
    public Dto validate(String user,String code) throws Exception {
        String  redisvalue =redis.geyser(user);
        System.out.println(redis.geyser(user));
        if(redisvalue!=null&&redisvalue.equals(code)){
          dao4.updateItripUser(user);
          return DtoUtil.returnSuccess("登录成功");
        }
        return DtoUtil.returnFail("登录失败请从新登录","10000");
    }
    public static void sendSms(String Phone,String message){

      //生产环境请求地址：app.cloopen.com
      String serverIp = "app.cloopen.com";
      //请求端口
      String serverPort = "8883";
      //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
      String accountSId = "8aaf07087f639e2b017f6c3ee8d20234";
      String accountToken = "fc459fdf6e1d4494bd30199864c5e388";
      //请使用管理控制台中已创建应用的APPID
      String appId = "8aaf07087f639e2b017f6c3eea01023b";
      CCPRestSmsSDK sdk = new CCPRestSmsSDK();
      sdk.init(serverIp, serverPort);
      sdk.setAccount(accountSId, accountToken);
      sdk.setAppId(appId);
      sdk.setBodyType(BodyType.Type_XML);
      String to = Phone;
      String templateId= "1";
      String[] datas = {message};
      //  String subAppend="1234";  //可选	扩展码，四位数字 0~9999
      //  String reqId="***";  //可选 第三方自定义消息id，最大支持32位英文数字，同账号下同一自然天内不允许重复
      HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas);
      //  HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas,subAppend,reqId);
      if("000000".equals(result.get("statusCode"))){
          //正常返回输出data包体信息（map）
          HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
          Set<String> keySet = data.keySet();
          for(String key:keySet){
              Object object = data.get(key);
              System.out.println(key +" = "+object);
          }
      }else{
          //异常返回输出错误码和错误信息
          System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
      }

  }

    @RequestMapping(value = "/api/ckusr",produces = "application/json; charset=utf-8")
    @ResponseBody
    public Dto maines() throws Exception {
        return DtoUtil.returnSuccess();
    }


    @RequestMapping(value = "/api/doregister",produces = "application/json; charset=utf-8")
    @ResponseBody
    public Dto Registermail(@RequestBody ItripUserVO vo) throws Exception {
        ItripUser user =new ItripUser();
        user.setUserCode(vo.getUserCode());
        user.setUserPassword(vo.getUserPassword());
        user.setUserName(vo.getUserName());
        dao4.insertItripUser(user);
        Random Random =new Random(4);
        int a =Random.nextInt(9999);
        App.SentSmail(user.getUserCode(),""+a);
        System.out.println("------------------aaaaaaaaaaaaaaaaaaaaaaaa");
        redis.setRedis(vo.getUserCode(),""+a);
        return DtoUtil.returnSuccess();
    }
    @RequestMapping(value = "/api/activate",produces = "application/json; charset=utf-8")
    @ResponseBody
    public Dto validatee(String user,String code) throws Exception {
        String  redisvalue =redis.geyser(user);
        System.out.println(redis.geyser(user));
        if(redisvalue!=null&&redisvalue.equals(code)){
            dao4.updateItripUser(user);
            return DtoUtil.returnSuccess("登录成功");
        }
        return DtoUtil.returnFail("登录失败请从新登录","10000");
    }
}
