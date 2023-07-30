package jens.chat.test;

import jens.chat.WxCloudRunApplication;
import jens.chat.model.ChatGptModel;
import jens.chat.vo.ChatGptRequestParams;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws InterruptedException{
        ConfigurableApplicationContext applicationContext = SpringApplication.run(WxCloudRunApplication.class, args);
        ChatGptModel chatModel = applicationContext.getBean("chatGptModel", ChatGptModel.class);
        ChatGptRequestParams chatRequestParameter = new ChatGptRequestParams();
        System.out.println("\n\n");

        while (true) {
            Thread.sleep(1000);
            System.out.print("\n请输入问题(q退出)：");
            String question = new Scanner(System.in).nextLine();
            if ("q".equals(question.trim())) break;
            chatModel.getAnswer(System.out::print, chatRequestParameter, question);
        }

        applicationContext.close();
    }
}
