package me.kuku.controller;

import me.kuku.entity.PhoneLa;
import me.kuku.entity.Prize;
import me.kuku.repository.PhoneRepository;
import me.kuku.repository.PrizeRepository;
import me.kuku.service.FlowService;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class FlowController {
    @Autowired
    PhoneRepository phoneRepository;
    @Autowired
    FlowService flowService;
    @Autowired
    PrizeRepository prizeRepository;

    @RequestMapping("/add")
    @ResponseBody
    public PhoneLa addPhone(PhoneLa phoneLa){
        PhoneLa phoneObj = checkPhone(phoneLa);
        if (!flowService.checkUnicom(phoneLa.getPhone())){
            return null;
        }
        if (phoneObj != null){
            return null;
        }
        PhoneLa save = phoneRepository.save(phoneLa);
        return save;
    }

    @RequestMapping("/query")
    @ResponseBody
    public List<Prize> queryPhone(@RequestParam("phone") String phone){
        List<Prize> prizeList = prizeRepository.findAll();
        for (Prize prize : prizeList){
            if (!prize.getPhone().equals(phone)){
                prizeList.remove(prize);
            }
        }
        return prizeList;
    }

    @RequestMapping("/delete")
    @ResponseBody
    public String deletePhone(@RequestParam("phone") String phone){
        List<PhoneLa> list = phoneRepository.findAll();
        for (PhoneLa phoneLa : list){
            if (phoneLa.getPhone().equals(phone)){
                phoneRepository.delete(phoneLa);
                break;
            }
        }
        return "删除成功";
    }

    @RequestMapping("/get")
    @ResponseBody
    public Integer getCaptcha(@RequestParam("phone") String phone, HttpServletRequest request){
        //不知道联通识别验证码是不是需要cookie来识别的。。所以把httpClient存在session中
        FlowService flow = new FlowService();
        Integer i = flow.getCaptcha(phone);
        if (i == 1){
            HttpSession session = request.getSession();
            session.setMaxInactiveInterval(60 * 5);
            session.setAttribute("client", flow.getHttpClient());
        }
        return i;
    }

    @RequestMapping("/verify")
    @ResponseBody
    public boolean verifyCaptcha(@RequestParam("phone") String phone, @RequestParam("captcha") String captcha, HttpServletRequest request){
        HttpSession session = request.getSession();
        CloseableHttpClient client = (CloseableHttpClient) session.getAttribute("client");
        if (client == null){
            //过期了
            return false;
        }else{
            FlowService flow = new FlowService();
            flow.setHttpClient(client);
            boolean b = flow.receiveFlow(phone, captcha);
            if (!b) {
                session.invalidate();
            }
            return b;
        }
    }


    public PhoneLa checkPhone(PhoneLa phoneLa){
        List<PhoneLa> allPhoto = phoneRepository.findAll();
        for (PhoneLa phoneLaObj : allPhoto){
            if (phoneLaObj.getPhone().equals(phoneLa.getPhone())){
                return phoneLaObj;
            }
        }
        return null;
    }

}
