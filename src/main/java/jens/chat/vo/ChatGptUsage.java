package jens.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGptUsage {
    private Integer prompt_tokens;
    private Integer completion_tokens;
    private Integer total_tokens;
}
