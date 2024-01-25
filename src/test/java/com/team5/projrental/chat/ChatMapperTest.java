package com.team5.projrental.chat;

import com.team5.projrental.chat.model.*;
import com.team5.projrental.user.model.SigninDto;
import com.team5.projrental.user.model.SigninVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@MybatisTest // xml파일(Dao)만 전부다 빈등록 됨.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatMapperTest {

    @Autowired
    private ChatMapper mapper;

        /*selVo.setIchat(1);
        selVo.setIproduct(1);
        selVo.setIstatus(1);
        selVo.setLastMsg("하이용");
        selVo.setOtherPersonIuser(2);
        selVo.setOtherPersonNm("바보현빈");
        selVo.setOtherPersonPic("aaa.jpg");
        selVo.setLastMsgAt("2024-01-25");
        selVo.setProdPic("ggg.jpg");*/


    @Test
    void insChat() { // Iproduct가 있는 pk번호로 만들어야 함
        ChatInsDto dto = new ChatInsDto();
        dto.setIproduct(25);
        int iChat = mapper.insChat(dto);
        log.info("result : {}", iChat);
        assertEquals(1, iChat);

    }

    @Test
    void selChatAll() {
        ChatSelDto selDto = new ChatSelDto();
        selDto.setLoginedIuser(1);
        selDto.setPage(1);
        selDto.setRowCount(10);
        selDto.setStartIdx(1);

        List<ChatSelVo> result = mapper.selChatAll(selDto);
        log.info("resultsize : {}", result.size());
        assertEquals(1, result.size());
    }

    @Test
    void insChatUser() { // 채팅방이 미리 생성되어있는곳에 없는 유저를 넣어야 성공함
        ChatUserInsDto dto = ChatUserInsDto.builder()
                .ichat(2)
                .iuser(1)
                .build();


        int insChatUserSize = mapper.insChatUser(dto);

        log.info("resultsize : {}", insChatUserSize);
        assertEquals(1, insChatUserSize);
    }

    @Test
    void selChatUserCheck() {
        ChatInsDto dto = new ChatInsDto();
        dto.setLoginedIuser(1);
        dto.setOtherPersonIuser(7);
        dto.setIchat(2);
        //dto.setIproduct(25);

        Integer ichat = mapper.selChatUserCheck(dto);

        assertEquals(2,ichat);

    }

    @Test
    void insChatMsg() {
        ChatMsgInsDto dto = new ChatMsgInsDto();
        dto.setMsg("하이");
        dto.setIchat(2);
        dto.setLoginedIuser(1);

        int chatMsg = mapper.insChatMsg(dto);

        assertEquals(1, chatMsg);

    }

    @Test
    void selChatMsgAll() {
        ChatMsgSelDto dto = new ChatMsgSelDto();
        dto.setIchat(2);
        dto.setPage(1);
        dto.setStartIdx(1);
        dto.setRowCount(10);
        dto.setLoginedIuser(1);

        List<ChatMsgSelVo> list = mapper.selChatMsgAll(dto);
        assertEquals(6, list.size());

    }

    @Test
    void updChatLastMsgAfterDelByLastMsg() {
        ChatMsgDelDto dto = new ChatMsgDelDto();
        dto.setIuser(1);
        dto.setIchat(2);
        int affectedDelChat = mapper.updChatLastMsgAfterDelByLastMsg(dto);
        assertEquals(1, affectedDelChat);
    }

    @Test
    void updChatLastMsg() {
    }

    @Test
    void selOtherPersonByLoginUser() {
    }

    @Test
    void selChatUser() {
    }

    @Test
    void delBeforeChatIstatus() {
    }
}