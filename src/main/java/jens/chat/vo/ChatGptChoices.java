package jens.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptChoices {
    private String index;
    private String finish_reason;
    private ChatGptMessage delta;
}
