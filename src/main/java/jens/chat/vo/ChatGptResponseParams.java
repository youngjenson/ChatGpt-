package jens.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGptResponseParams {
    private String id;
    private String object;
    private String created;
    private String model;
    private List<ChatGptChoices> choices;
    private ChatGptUsage usage;
}
