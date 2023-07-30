package jens.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptRequestParams {
    private String model = "gpt-3.5-turbo-16k";
    private double temperature = 1;
    private List<ChatGptMessage> messages = new ArrayList<>();
    //stream为true时，返回多个结果，false时，返回一个结果
    private boolean stream = true;

    public void addMessage(String role, String content) {
        messages.add(new ChatGptMessage(role, content));
    }
}
