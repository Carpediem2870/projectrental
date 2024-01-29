package com.team5.projrental.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.team5.projrental.chat.model.ChatMsgInsDto;
import com.team5.projrental.chat.model.ChatMsgPushVo;
import com.team5.projrental.chat.model.ChatSelDto;
import com.team5.projrental.chat.model.ChatSelVo;
import com.team5.projrental.common.SecurityProperties;
import com.team5.projrental.common.model.ResVo;
import com.team5.projrental.common.security.AuthenticationFacade;
import com.team5.projrental.common.utils.CookieUtils;
import com.team5.projrental.common.utils.KakaoAxisGenerator;
import com.team5.projrental.common.utils.MyFileUtils;
import com.team5.projrental.user.model.UserEntity;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;


/*
@Slf4j
@ExtendWith(SpringExtension.class)
@Import(ChatServiceTest.class)
class ChatServiceTest {

    @MockBean
    private ChatMapper mapper;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private SecurityProperties securityProperties;
    @MockBean
    private CookieUtils cookieUtils;
    @MockBean
    private AuthenticationFacade authenticationFacade;
    @MockBean
    private KakaoAxisGenerator axisGenerator;
    @MockBean
    private MyFileUtils myFileUtils;
    @MockBean
    private HttpServletResponse res;
    @Autowired
    private ChatService service;
    private ObjectMapper objectMapper;

    @Test
    void getChatAll() {
        ChatSelDto dto = new ChatSelDto();
        dto.setLoginedIuser(1);
        dto.setPage(1);
        dto.setRowCount(10);
        dto.setStartIdx(1);

        when(mapper.selChatAll(dto)).thenReturn(null);
        ChatSelVo vo = new ChatSelVo();
        vo.setIchat(2);
        vo.setIproduct(25);

        List<ChatSelVo> list = new ArrayList<>();
        list.add(vo);

        list = service.getChatAll(dto);
        verify(mapper).selChatAll(any());
        assertEquals(list.get(0).getIchat(),2);
        assertEquals(list.get(0).getIproduct(), 25);
    }

    @Test
    void postChatMsg() {
        ChatMsgInsDto dto = new ChatMsgInsDto();
        dto.setLoginedIuser(1);
        dto.setMsg("하이 테스트");
        dto.setIchat(2);
        dto.setSeq(7);

        int istatus = mapper.delBeforeChatIstatus(dto);

        assertEquals(0,istatus);

        int affectedRows = mapper.insChatMsg(dto);
        if (affectedRows == 1) {
            int updRows = mapper.updChatLastMsg(dto);

            assertEquals(1,updRows);
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 포멧 정의
        String createdAt = now.format(formatter);

        UserEntity otherPerson = mapper.selOtherPersonByLoginUser(dto);

        assertEquals(7,otherPerson.getIuser());

        try {
            if (otherPerson.getFirebaseToken() != null) {
                ChatMsgPushVo pushVo = new ChatMsgPushVo();
                pushVo.setIchat(dto.getIchat());
                pushVo.setSeq(dto.getSeq());
                pushVo.setWriterIuser(dto.getLoginedIuser());
                pushVo.setMsg(dto.getMsg());
                pushVo.setCreatedAt(createdAt);

                String body = objectMapper.writeValueAsString(pushVo);

                Notification noti = Notification.builder()
                        .setTitle("chat")
                        .setBody(body)
                        .build();

                Message message = Message.builder()
                        .putData("type", "chat")
                        .putData("json", body)
                        .setToken(otherPerson.getFirebaseToken())
                        .build();

                FirebaseMessaging.getInstance().sendAsync(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(dto.getSeq(),7);
    }

    @Test
    void getMsgAll() {
    }

    @Test
    void chatDelMsg() {
    }

    @Test
    void postChat() {
    }
}*/
